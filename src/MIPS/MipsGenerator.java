package MIPS;

import IR.Instr.AllocaInstr;
import IR.Instr.CalcInstr;
import IR.Instr.CallInstr;
import IR.Instr.GetelementptrInstr;
import IR.Instr.IcmpInstr;
import IR.Instr.LoadInstr;
import IR.Instr.Operator;
import IR.Instr.StoreInstr;
import IR.Instr.Terminators.BrInstr;
import IR.Instr.Terminators.ReturnInstr;
import IR.Instr.TruncInstr;
import IR.Instr.ZextInstr;
import IR.Type.ArrayType;
import IR.Type.IRType;
import IR.Type.IntType;
import IR.Type.PointerType;
import IR.Value.BasicBlock;
import IR.Value.ConstArray;
import IR.Value.ConstInt;
import IR.Value.ConstValue;
import IR.Value.Function;
import IR.Value.GlobalVariable;
import IR.Value.Instruction;
import IR.Value.LLVMModule;
import IR.Value.StringLiteral;
import IR.Value.Value;
import IR.Value.Zeroinitializer;
import MIPS.Instr.B;
import MIPS.Instr.Comment;
import MIPS.Instr.GlobalVarInstr;
import MIPS.Instr.HILO;
import MIPS.Instr.J;
import MIPS.Instr.La;
import MIPS.Instr.Label;
import MIPS.Instr.Li;
import MIPS.Instr.Lw;
import MIPS.Instr.MD;
import MIPS.Instr.MipsCalc;
import MIPS.Instr.MipsInstruction;
import MIPS.Instr.Sw;
import MIPS.Instr.SyscallInstr;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsGenerator {
    private LLVMModule module;
    private MipsModule mipsModule = MipsModule.getInstance();
    private String curFuncName = "";
    private int stackOffset = 0; // means the current offset in a stack frame, set to 0 when entering a function
    private HashMap<String, Integer> stackOffsetMap = new HashMap<>(); // key: name of value; value: offset in stack frame

    public MipsGenerator(LLVMModule module) {
        this.module = module;
    }

    private void clearWhenEnter() {
        stackOffset = 0;
        stackOffsetMap = new HashMap<>();
    }

    private ArrayList<Integer> ConstArray2IntArray(ArrayList<ConstValue> val) {
        if (val == null || val.isEmpty()) {
            return null;
        }
        assert val.get(0) instanceof ConstInt; // TODO: delete

        ArrayList<Integer> res = new ArrayList<>();
        for (ConstValue v : val) {
            res.add(((ConstInt) v).getValue());
        }
        return res;
    }


    public void generate() {
        for (GlobalVariable gv : module.getGlobalVariables()) {
            generateGV(gv);
        }

        for (StringLiteral sl : module.getStringLiterals()) {
            generateStringLiteral(sl);
        }
        mipsModule.addText(new J("jal", "BB_main"));

        mipsModule.addText(new J("j", "BB_exit"));

        for (Function func : module.getFunctions()) {
            if (func.isLibFunc()) continue;
            curFuncName = func.getName().substring(1);
            generateFunction(func);
        }

        mipsModule.addText(new Label("exit"));
        mipsModule.addText(new Li("$v0", 10));
        mipsModule.addText(new SyscallInstr(10));
        //System.err.println(mipsModule.toString());
    }

    private void generateFunction(Function func) {
        //分为两部分: 形参栈帧等维护， 函数体； 基本块也区分了第一块和后面。
        String funcName = func.getName();
        assert funcName.charAt(0) == '@';// TODO: delete
        funcName = funcName.substring(1);
        mipsModule.addText(new Label(funcName));
        //TODO: 栈帧    参数好像不用特别强调？
        //TODO: 参数这里，在函数手动判断，根据paraList数量
        clearWhenEnter(); // set stackOffset to 0, then pass the argument area(-4 to -(paraNum)*4)
        ArrayList<IRType> paramTypes = func.getParaList();
        if (paramTypes != null && !paramTypes.isEmpty()) {
            for (int i = 0; i < paramTypes.size(); i++) {
                stackOffset -= 4;
                stackOffsetMap.put("%" + i, stackOffset);
            }
        }

        ArrayList<BasicBlock> basicBlocks = func.getBlocks();
        for (int i = 0; i < basicBlocks.size(); i++) {
            if (i != 0) {
                mipsModule.addText(new Label(basicBlocks.get(i).getName()+"_"+func.getName().substring(1)));
            }
            generateBasicBlock(basicBlocks.get(i));
        }
    }

    private void generateBasicBlock(BasicBlock bb) {
        for (Instruction instr : bb.getInstructions()) {
            mipsModule.addText(new Comment(instr.toString()));
            translateInstr(instr);
        }
    }

    private void generateCalcInstr(CalcInstr instr) {
        // x = y + z
        if (instr.getArgument(0) instanceof ConstInt) {
            mipsModule.addText(new Li("$k0", ((ConstInt) instr.getArgument(0)).getValue()));
        } else {
            int offset = stackOffsetMap.get(instr.getArgument(0).getName());
            mipsModule.addText(new Lw("$k0", "$sp", offset));
        }

        if (instr.getArgument(1) instanceof ConstInt) {
            mipsModule.addText(new Li("$k1", ((ConstInt) instr.getArgument(1)).getValue()));
        } else {
            int offset = stackOffsetMap.get(instr.getArgument(1).getName());
            mipsModule.addText(new Lw("$k1", "$sp", offset));
        }

        switch (instr.getOp()) {
            case add:
                mipsModule.addText(new MipsCalc("addu", "$t0", "$k0", "$k1"));
                break;
            case sub:
                mipsModule.addText(new MipsCalc("subu", "$t0", "$k0", "$k1"));
                break;
            case mul:
                mipsModule.addText(new MD("mult", "$k0", "$k1"));
                mipsModule.addText(new HILO("MFLO", "$t0"));
                break;
            case sdiv:
                mipsModule.addText(new MD("div", "$k0", "$k1"));
                mipsModule.addText(new HILO("MFLO", "$t0"));
                break;
            case srem:
                mipsModule.addText(new MD("div", "$k0", "$k1"));
                mipsModule.addText(new HILO("MFHI", "$t0"));
                break;
            default:
                System.err.println("error: unsupported calc instr" + instr.getOp());
                break;
        }
        stackOffset -= 4;
        stackOffsetMap.put(instr.getName(), stackOffset);
        mipsModule.addText(new Sw("$t0", "$sp", stackOffset));
        //mipsModule.addText(new MipsCalc(instr.getOp(),"$k2", "$k0", "$k1"));
    }

    private void generateBrInstr(BrInstr instr) {
        if (instr.getCond() != null) {
            // bne
            if (instr.getCond() instanceof ConstInt) {
                mipsModule.addText(new Li("$k0", ((ConstInt) instr.getCond()).getValue()));
            } else {
                loadVar(instr.getCond(), "$k0");
//                int offset = stackOffsetMap.get(instr.getCond().getName());
//                mipsModule.addText(new Lw("$k0", "$sp", offset));
            }
            mipsModule.addText(new B("bne", "BB_" + instr.getIfTrue().getName()+"_"+curFuncName, "$k0", "$0"));
            mipsModule.addText(new J("j", "BB_"+ instr.getIfFalse().getName() +"_"+curFuncName));
        } else {
            mipsModule.addText(new J("j", "BB_" + instr.getNext().getName() +"_"+curFuncName));
        }
    }

    private void generateAllocaInstr(AllocaInstr instr) {
        // Alloca 相当于分配了一个指针，但是后面要是想用一定会store，所以我感觉这里只需要记录offset就可以了？ TODO:check
        // 12.13: 感觉是，alloca分配了一片空间，但是alloca变量本身存的值应该是这个地址
        IRType type = ((PointerType)instr.getType()).getPointeeType();
        if (type instanceof ArrayType) {
            stackOffset -= ((ArrayType) type).getLength() * 4;
        } else {
            stackOffset -= 4;
        }

        mipsModule.addText(new MipsCalc("addiu","$k0","$sp", stackOffset));
        // 现在的stackoffset是分配出来空间的地址，也应该是（alloca变量）指针里面存的值
        stackOffset -= 4;
        mipsModule.addText(new Sw("$k0","$sp",stackOffset));
        stackOffsetMap.put(instr.getName(), stackOffset);
    }

    private void generateLoadInstr(LoadInstr instr) { // TODO: check global!
        loadVar(instr.getArgument(0), "$k0");
//        int offset = stackOffsetMap.get(instr.getArgument(0).getName());
//        mipsModule.addText(new Lw("$k0", "$sp", offset));// save the address to k0
        mipsModule.addText(new Lw("$k1", "$k0", 0)); // save the value from address to k1
        stackOffset -= 4;
        stackOffsetMap.put(instr.getName(), stackOffset); // TODO: check name!
        mipsModule.addText(new Sw("$k1", "$sp", stackOffset));

    }

    private void generateStoreInstr(StoreInstr instr) {
        loadVar(instr.getArgument(1), "$k0");
//        int offset = stackOffsetMap.get(instr.getArgument(1).getName());
//        mipsModule.addText(new Lw("$k0", "$sp", offset)); // load the address to k0
        if (instr.getArgument(0) instanceof ConstInt) {
            mipsModule.addText(new Li("$k1", ((ConstInt) instr.getArgument(0)).getValue()));
        } else {
            int offset2 = stackOffsetMap.get(instr.getArgument(0).getName());
            mipsModule.addText(new Lw("$k1", "$sp", offset2));
        }
        mipsModule.addText(new Sw("$k1", "$k0", 0)); // store the value to address
        // TODO:check
    }

    private void generateReturnInstr(ReturnInstr instr) {

        if (instr.getArgument(0) instanceof ConstInt) {
            mipsModule.addText(new Li("$v0", ((ConstInt) instr.getArgument(0)).getValue()));
        } else {
            if (!instr.getArguments().isEmpty()){
                int offset = stackOffsetMap.get(instr.getArgument(0).getName());
                mipsModule.addText(new Lw("$v0", "$sp", offset));
            }
        }
        mipsModule.addText(new J("jr", "$ra"));
    }

    private void generateCallInstr(CallInstr instr) {
        if (instr.getFunction().isLibFunc()) {
            switch (instr.getFunction().getName().substring(1)) {
                case "getint":
                    mipsModule.addText(new Li("$v0", 5));
                    mipsModule.addText(new SyscallInstr(5));
                    break;
                case "putint":
                    if (instr.getArgument(1) instanceof ConstInt) {
                        mipsModule.addText(new Li("$a0", ((ConstInt) instr.getArgument(1)).getValue()));
                    } else {
                        loadVar(instr.getArgument(1), "$a0");
                    }
                    mipsModule.addText(new Li("$v0", 1));
                    mipsModule.addText(new SyscallInstr(1));
                    break;
                case "putch":
                    if (instr.getArgument(1) instanceof ConstInt) {
                        mipsModule.addText(new Li("$a0", ((ConstInt) instr.getArgument(1)).getValue()));
                    } else {
                        loadVar(instr.getArgument(1), "$a0");
                    }
                    mipsModule.addText(new Li("$v0", 11));
                    mipsModule.addText(new SyscallInstr(11));
                    break;
                case "getchar":
                    mipsModule.addText(new Li("$v0", 12));
                    mipsModule.addText(new SyscallInstr(12));
                    break;
                case "putstr":
                    // i8*
                    if (instr.getArgument(1) instanceof StringLiteral) {
                        mipsModule.addText(new La("$a0", instr.getName().substring(1)));
                    } else if (instr.getArgument(1) instanceof GlobalVariable) {
                        mipsModule.addText(new La("$a0", instr.getName().substring(1)));
                        //mipsModule.addText(new Sw(""));
                    } else {
                        // 局部变量
                        int offset = stackOffsetMap.get(instr.getArgument(1).getName());
                        mipsModule.addText(new MipsCalc("addiu","$k0", "$sp", offset));
                        mipsModule.addText(new Lw("$a0", "$k0", 0));
                    }
                    mipsModule.addText(new Li("$v0", 4));
                    mipsModule.addText(new SyscallInstr(4));
                    break;
                default:
                    System.err.println("Unknown lib function: " + instr.getFunction().getName());
                    break;

            }

            if (!instr.getName().isEmpty()) {
                // return value
                stackOffset -= 4;
                stackOffsetMap.put(instr.getName(), stackOffset);
                mipsModule.addText(new Sw("$v0", "$sp", stackOffset));
            }
            return;
//            mipsModule.addText(new J("jal", instr.getArgument(0).getName().substring(1)));
        }
        // CallInstr: arg[0] funcName, arg[1~n]: para
        // 参数压栈
        for (int i = 1; i < instr.getArguments().size(); i++) {
            if (instr.getArguments().get(i) instanceof ConstInt) {
                mipsModule.addText(new Li("$k0", ((ConstInt) instr.getArguments().get(i)).getValue()));
            } else {
                int offset = stackOffsetMap.get(instr.getArguments().get(i).getName());
                mipsModule.addText(new Lw("$k0", "$sp", offset));
            }
            //stackOffset -= 4;
            mipsModule.addText(new Sw("$k0", "$sp", stackOffset - 8 - 4 * i));
            // 这里的8，分别是要保存起来的sp和ra。所以再过了8才是真正函数栈的开始。
        }
        // 设置函数栈
        mipsModule.addText(new Sw("$sp", "$sp", stackOffset - 4));// 先存sp，再存ra
        mipsModule.addText(new Sw("$ra", "$sp", stackOffset - 8));
        mipsModule.addText(new MipsCalc("addiu", "$sp", "$sp", stackOffset-8));

        // TODO: 函数调用的跳转j指令会自动设置 $ra 和 $sp吗？

        mipsModule.addText(new J("jal", "BB_" + instr.getArgument(0).getName().substring(1)));

        mipsModule.addText(new Lw("$ra", "$sp", 0));
        mipsModule.addText(new Lw( "$sp", "$sp", 4)); // TODO: check: lw $sp, 4($sp)
        if (!instr.getName().isEmpty()) {
            // return value
            stackOffset -= 4;
            stackOffsetMap.put(instr.getName(), stackOffset);
            mipsModule.addText(new Sw("$v0", "$sp", stackOffset));
        }
    }

    private void loadVar(Value var, String reg) {
        if (var instanceof GlobalVariable) {
            mipsModule.addText(new La(reg, var.getName().substring(1)));
        } else if (var instanceof StringLiteral) {
            mipsModule.addText(new La(reg, var.getName().substring(1)));
        } else {
            int offset = stackOffsetMap.get(var.getName());
            mipsModule.addText(new Lw(reg, "$sp", offset));
        }
    }

    private void generateGetelementptrInstr(GetelementptrInstr instr) {
        // if global!!!
        loadVar(instr.getArgument(0), "$k0");
//        if (instr.getArgument(0) instanceof GlobalVariable) {
//            mipsModule.addText(new La("$k0", instr.getArgument(0).getName()));
//        }else {
//            mipsModule.addText(new Lw("$k0", "$sp", stackOffsetMap.get(instr.getArgument(0).getName())));
//        }

        if (instr.getArgument(1) instanceof ConstInt) {
            mipsModule.addText(new MipsCalc("addiu", "$k0", "$k0", ((ConstInt) instr.getArgument(1)).getValue()*4));
        } else {
            loadVar(instr.getArgument(1), "$k1");
//            int offset = stackOffsetMap.get(instr.getArgument(1).getName());
//            // TODO: check type! char may only x1 !!!
//            mipsModule.addText(new Lw("$k1", "$sp", offset));
            IRType type = instr.getType();
            System.out.println(type);
            if (type.equals(IntType.I32)) {
                System.err.println("error: type not supported");
                //mipsModule.addText(new MipsCalc("sll", "$k1", "$k1", 2));
            } else if (type.equals(IntType.I8)) {
                System.err.println("error: type not supported");
                //mipsModule.addText(new MipsCalc("sll", "$k1", "$k1", 2)); // TODO: check
            } else if (type.isPointer()){
                mipsModule.addText(new MipsCalc("sll", "$k1", "$k1", 2));
                //System.err.println("error: type not supported");
            } else {
                System.err.println("error: type not supported");
            }
            mipsModule.addText(new MipsCalc("addu", "$k0", "$k0", "$k1"));
        }
        stackOffset -= 4;
        stackOffsetMap.put(instr.getName(), stackOffset);
        mipsModule.addText(new Sw("$k0", "$sp", stackOffset));
    }

    private void generateIcmpInstr(IcmpInstr instr) {
        //  %4 = icmp eq i32 %3, 0
        //
        Operator op = instr.getCmp();
        if (instr.getArgument(0) instanceof ConstInt) {
//            if (((ConstInt) instr.getArgument(0)).getValue() == 0) {
//
//            } //TODO: 常数优化，提前运算
            mipsModule.addText(new Li("$k0", ((ConstInt) instr.getArgument(0)).getValue()));
        } else {
            mipsModule.addText(new Lw("$k0", "$sp", stackOffsetMap.get(instr.getArgument(0).getName())));
        }
        if (instr.getArgument(1) instanceof ConstInt) {
            mipsModule.addText(new Li("$k1", ((ConstInt) instr.getArgument(1)).getValue()));
        } else {
            mipsModule.addText(new Lw("$k1", "$sp", stackOffsetMap.get(instr.getArgument(1).getName())));
        }
        switch (op) {
            case eq:
                mipsModule.addText(new MipsCalc("seq", "$t0", "$k0", "$k1"));
                //TODO: 看起来v0只会在函数调用结束后赋值，占用一下呢？
                break;
            case ne:
                mipsModule.addText(new MipsCalc("sne", "$t0", "$k0", "$k1"));
                break;
            case sge:
                mipsModule.addText(new MipsCalc("sge", "$t0", "$k0", "$k1"));
                break;
            case sgt:
                mipsModule.addText(new MipsCalc("sgt", "$t0", "$k0", "$k1"));
                break;
            case sle:
                mipsModule.addText(new MipsCalc("sle", "$t0", "$k0", "$k1"));
                break;
            case slt:
                mipsModule.addText(new MipsCalc("slt", "$t0", "$k0", "$k1"));
                break;
            default:
                System.err.println("error: icmp op not supported");
                break;
        }
        stackOffset -= 4;
        stackOffsetMap.put(instr.getName(), stackOffset);
        mipsModule.addText(new Sw("$t0", "$sp", stackOffset));
    }

    private void generateTruncInstr(TruncInstr instr) {
        //  %5 = trunc i32 %4 to i16 暂时都用4位存，不需要浪费指令，直接加表即可？
        if (instr.getArgument(0) instanceof ConstInt) {
            mipsModule.addText(new Li("$k0", ((ConstInt) instr.getArgument(0)).getValue()));
            stackOffset-=4;
            stackOffsetMap.put(instr.getName(), stackOffset);
            mipsModule.addText(new Sw("$k0", "$sp", stackOffset));
        } else {
            stackOffsetMap.put(instr.getName(), stackOffsetMap.get(instr.getArgument(0).getName()));
        }
    }

    private void generateZextInstr(ZextInstr instr) {
        if (instr.getArgument(0) instanceof ConstInt) {
            mipsModule.addText(new Li("$k0", ((ConstInt) instr.getArgument(0)).getValue()));
            stackOffset-=4;
            stackOffsetMap.put(instr.getName(), stackOffset);
            mipsModule.addText(new Sw("$k0", "$sp", stackOffset));
        } else {

            stackOffsetMap.put(instr.getName(), stackOffsetMap.get(instr.getArgument(0).getName()));
        }
    }

    private void translateInstr(Instruction instr) {
        // TODO: instance of ...
        if (instr instanceof CalcInstr) {
            generateCalcInstr((CalcInstr) instr);
        } else if (instr instanceof BrInstr) {
            generateBrInstr((BrInstr) instr);
        } else if (instr instanceof AllocaInstr) {
            generateAllocaInstr((AllocaInstr) instr);
        } else if (instr instanceof LoadInstr) {
            generateLoadInstr((LoadInstr) instr);
        } else if (instr instanceof StoreInstr) {
            generateStoreInstr((StoreInstr) instr);
        } else if (instr instanceof CallInstr) {
            generateCallInstr((CallInstr) instr);
        } else if (instr instanceof GetelementptrInstr) {
            generateGetelementptrInstr((GetelementptrInstr) instr);
        } else if (instr instanceof IcmpInstr) {
            generateIcmpInstr((IcmpInstr) instr);
        } else if (instr instanceof TruncInstr) {
            generateTruncInstr((TruncInstr) instr);
        } else if (instr instanceof ZextInstr) {
            generateZextInstr((ZextInstr) instr);
        } else if (instr instanceof ReturnInstr) {
            generateReturnInstr((ReturnInstr) instr);
        }
    }

    private void generateStringLiteral(StringLiteral sl) {
        String name = sl.getName();
        if (name.charAt(0) == '@') {
            name = name.substring(1);
        }
        GlobalVarInstr gvInstr = new GlobalVarInstr(name, sl.getNoConvert());
        mipsModule.addData(gvInstr);
    }

    private void generateGV(GlobalVariable gv) {
        String name = gv.getName();
        if (name.charAt(0) == '@') {
            name = name.substring(1);
        }
        GlobalVarInstr gvInstr;// GV all pointer!
        IRType realType = ((PointerType) gv.getType()).getPointeeType();
        ConstValue initVal = gv.getInitValue(); // ConstInt, ConstArr, ZeroInit, (StringLit is not in GV!)
        if (realType.isI32()) {
            // word
            assert (initVal instanceof ConstInt);// TODO: delete
            gvInstr = new GlobalVarInstr(name, ((ConstInt) initVal).getValue(), "word");
        } else if (realType.isI8()) {
            // byte
            assert (initVal instanceof ConstInt);// TODO: delete
            gvInstr = new GlobalVarInstr(name, ((ConstInt) initVal).getValue(), "word");
        } else if (realType.isArray()) {
            // ConstArr || ZeroInit
            int len = ((ArrayType) realType).getLength();
            String type = ((ArrayType) realType).getElementType().isI32() ? "word" : "word";
            // TODO: checkType

            if (initVal instanceof Zeroinitializer) {
                gvInstr = new GlobalVarInstr(name, null, type, len);
            } else if (initVal instanceof ConstArray) {
                gvInstr = new GlobalVarInstr(name, ConstArray2IntArray(((ConstArray) initVal).getElements()), type, len);
            } else {
                System.err.println("Error: unsupported init value for global variable Array");
                gvInstr = null;
            }
        } else {
            gvInstr = null;
            System.err.println("Error: unsupported type for global variable");
            System.err.println(realType);
        }
        mipsModule.addData(gvInstr);
    }
}
