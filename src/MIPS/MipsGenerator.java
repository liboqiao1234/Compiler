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
import MIPS.Instr.Move;
import MIPS.Instr.Sw;
import MIPS.Instr.SyscallInstr;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsGenerator {
    private LLVMModule module;
    private MipsModule mipsModule = MipsModule.getInstance();
    private String curFuncName = "";
    private Instruction beforeInstr = null;
    private int stackOffset = 0; // means the current offset in a stack frame, set to 0 when entering a function
    private HashMap<String, Integer> stackOffsetMap = new HashMap<>(); // key: name of value; value: offset in stack frame

    private HashMap<String, Value> regFor = new HashMap<>();// reg存了谁
    private HashMap<Value, String> varAt = new HashMap<>();// var在哪个Reg
    private boolean useReg = false;

    public MipsGenerator(LLVMModule module) {
        this.module = module;
        for (int i = 0; i <= 9; i++) {
            regFor.put("$t" + i, null);
        }
    }

    private void releaseAllRegister(boolean clear){
        // 弃用寄存器，不保存
        if (clear == false){
            for (int i = 0; i <= 9; i++) {
                Value v = regFor.get("$t" + i);
                if (v != null) {
                    varAt.remove(v);
                    regFor.put("$t" + i, null);
                }
//                regFor.put(reg, null);
//                varAt.remove(v);
            }
        } else{
            releaseAllRegister();
        }
    }

    private void releaseAllRegister() {
        for (int i = 0; i <= 9; i++) {
            releaseReg("$t" + i);
//            Value v = regFor.get("$t" + i);
//            if (v != null) {
//                stackOffset -= 4;
//                stackOffsetMap.put(v.getName(), stackOffset);
//                mipsModule.addText(new Sw("$t" + i, "$sp", stackOffset));
//            }
//            varAt.remove(v);
//            regFor.put("$t" + i, null);
        }
    }

    private void releaseReg(String reg) {
        // saveToStack
        Value v = regFor.get(reg);
        if (v == null) return;
        if (!stackOffsetMap.containsKey(v.getName())) {
            stackOffset -= 4;
            stackOffsetMap.put(v.getName(), stackOffset);
            mipsModule.addText(new Sw(reg, "$sp", stackOffset));
            regFor.put(reg, null);
            varAt.remove(v);
        } else {
            int offset = stackOffsetMap.get(v.getName());
            mipsModule.addText(new Sw(reg, "$sp", offset));
            regFor.put(reg, null);
            varAt.remove(v);

        }
    }

    private int nextFreeNum = 0;

    private String AllocReg(Value v) {
        if (!useReg) return null;
        for (int i = 0; i <= 9; i++) {
            if (regFor.get("$t" + i) == null) {
                regFor.put("$t" + i, v);
                varAt.put(v, "$t" + i);
                return "$t" + i;
            }
        }

        //TODO: 按照use释放寄存器
        String nextFree = "$t" + nextFreeNum;
        nextFreeNum = (nextFreeNum + 1) % 10;
        releaseReg(nextFree);
        regFor.put(nextFree, v);
        varAt.put(v, nextFree);
        return nextFree;
//        return null;
    }

    private String getValue(Value value) {
        if (value instanceof ConstInt) {
            System.err.println("should not be here! imm???");
            return null;
        }
        if (varAt.containsKey(value)) {
            return varAt.get(value);
        }
        return null;
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

    public void generateNew1() {
//        System.err.println("mips generator with Allocator!");
        useReg = true;
        generate();
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
//        for (int i = 0; i < basicBlocks.size(); i++) {
//            if ()
//        }

        for (int i = 0; i < basicBlocks.size(); i++) {
            if (i != 0) {
                //每次到下一个BB时候判断，窥孔优化掉j
                if (mipsModule.getLast() instanceof J) {
                    J jInstr = (J) mipsModule.getLast();
                    if (jInstr.getType().equals("j") && jInstr.getLabel().equals("BB_" + basicBlocks.get(i).getName() + "_" + funcName)) {
                        mipsModule.deleteLast();
                    }
                }
                mipsModule.addText(new Label(basicBlocks.get(i).getName() + "_" + func.getName().substring(1)));

            }
            releaseAllRegister(false);
            generateBasicBlock(basicBlocks.get(i));

        }
    }

    private void generateBasicBlock(BasicBlock bb) {
        for (int i = 0; i < bb.getInstructions().size(); i++) {
            Instruction instr = bb.getInstructions().get(i);
            mipsModule.addText(new Comment(instr.toString()));
            beforeInstr = i == 0 ? null : bb.getInstructions().get(i - 1);
            translateInstr(instr);
        }
    }

    private void generateCalcInstr(CalcInstr instr) {
        // x = y + z
        String reg1 = "$k0";
        String reg2 = "$k1";
        if (instr.getArgument(0) instanceof ConstInt) {
            mipsModule.addText(new Li("$k0", ((ConstInt) instr.getArgument(0)).getValue()));
        } else {
            if (getValue(instr.getArgument(0)) != null) {
                reg1 = getValue(instr.getArgument(0));
            } else {
                loadVar(instr.getArgument(0), "$k0");
            }
//                int offset = stackOffsetMap.get(instr.getArgument(0).getName());
//                mipsModule.addText(new Lw("$k0", "$sp", offset));
        }

        if (instr.getArgument(1) instanceof ConstInt) {
            mipsModule.addText(new Li("$k1", ((ConstInt) instr.getArgument(1)).getValue()));
        } else {
            if (getValue(instr.getArgument(1)) != null) {
                reg2 = getValue(instr.getArgument(1));
            } else {
                loadVar(instr.getArgument(1), "$k1");
            }
//            int offset = stackOffsetMap.get(instr.getArgument(1).getName());
//            mipsModule.addText(new Lw("$k1", "$sp", offset));
        }
        String reg3 = AllocReg(instr);
        if (reg3 == null) {
            reg3 = "$k0";
        }
        switch (instr.getOp()) {
            case add:
                mipsModule.addText(new MipsCalc("addu", reg3, reg1, reg2));
                break;
            case sub:
                mipsModule.addText(new MipsCalc("subu", reg3, reg1, reg2));
                break;
            case mul:
                mipsModule.addText(new MD("mult", reg1, reg2));
                mipsModule.addText(new HILO("MFLO", reg3));
                break;
            case sdiv:
                mipsModule.addText(new MD("div", reg1, reg2));
                mipsModule.addText(new HILO("MFLO", reg3));
                break;
            case srem:
                mipsModule.addText(new MD("div", reg1, reg2));
                mipsModule.addText(new HILO("MFHI", reg3));
                break;
            default:
                System.err.println("error: unsupported calc instr" + instr.getOp());
                break;
        }
        if (reg3.equals("$k0")) {//TODO:check this
            stackOffset -= 4;
            stackOffsetMap.put(instr.getName(), stackOffset);
            mipsModule.addText(new Sw(reg3, "$sp", stackOffset));
        }
        //mipsModule.addText(new MipsCalc(instr.getOp(),"$k2", "$k0", "$k1"));
    }

    private void generateBrInstr(BrInstr instr) {
        if (instr.getCond() != null) {
            // bne
            String reg = "$k0";
            if (instr.getCond() instanceof ConstInt) {
                mipsModule.addText(new Li("$k0", ((ConstInt) instr.getCond()).getValue()));
            } else {
                reg = loadVar(instr.getCond(), "$k0");
            }
            mipsModule.addText(new B("bne", "BB_" + instr.getIfTrue().getName() + "_" + curFuncName, reg, "$0"));
            mipsModule.addText(new J("j", "BB_" + instr.getIfFalse().getName() + "_" + curFuncName));
        } else {
            mipsModule.addText(new J("j", "BB_" + instr.getNext().getName() + "_" + curFuncName));
        }
    }

    private void generateAllocaInstr(AllocaInstr instr) {
        // Alloca 相当于分配了一个指针，但是后面要是想用一定会store，所以我感觉这里只需要记录offset就可以了？ TODO:check
        // 12.13: 感觉是，alloca分配了一片空间，但是alloca变量本身存的值应该是这个地址
        IRType type = ((PointerType) instr.getType()).getPointeeType();
        if (type instanceof ArrayType) {
            stackOffset -= ((ArrayType) type).getLength() * 4;
        } else {
            stackOffset -= 4;
        }

        mipsModule.addText(new MipsCalc("addiu", "$k0", "$sp", stackOffset));
        // 现在的stackoffset是分配出来空间的地址，也应该是（alloca变量）指针里面存的值

        stackOffset -= 4;
        mipsModule.addText(new Sw("$k0", "$sp", stackOffset));
        stackOffsetMap.put(instr.getName(), stackOffset);
    }

    private void generateLoadInstr(LoadInstr instr) {
        // TODO check new version 窥孔优化掉相邻 store 和 load : give up
        String reg2 = AllocReg(instr);
        if (reg2 == null) {
            reg2 = "$k1";
        }
//        if (beforeInstr instanceof StoreInstr) {
//            StoreInstr storeInstr = (StoreInstr) beforeInstr;
//            if (storeInstr.getArgument(1).equals(instr.getArgument(0))) {
//                // load 和 store 的地址相同，可以省略load
////                  System.err.println("[Opt] Store: " + storeInstr.getArgument(1) + " Load:" + instr.getArgument(0));
//            } else {
//                String reg = "$k0";
//                if (getValue(instr.getArgument(0)) != null) {
//                    reg = getValue(instr.getArgument(0));
//                } else {
//                    loadVar(instr.getArgument(0), "$k0");
//                }
//                mipsModule.addText(new Lw(reg2, reg, 0)); // save the value from address to k1
//            }
//        } else {
        String reg = "$k0";
        if (getValue(instr.getArgument(0)) != null) {
            reg = getValue(instr.getArgument(0));
        } else {
            reg = "$k0";
            loadVar(instr.getArgument(0), "$k0");
        }
        mipsModule.addText(new Lw(reg2, reg, 0)); // save the value from address to k1
        //mipsModule.addText(new Move(reg2, reg)); // save the value from address to k1
//        }

        if (reg2.equals("$k1")) {
            stackOffset -= 4;
            stackOffsetMap.put(instr.getName(), stackOffset); // TODO: check name!
            mipsModule.addText(new Sw("$k1", "$sp", stackOffset));
        }

    }

    private void generateStoreInstr(StoreInstr instr) {
        String reg = "$k0";
        String reg2 = "$k1";
        if (getValue(instr.getArgument(1)) != null) {
            reg = getValue(instr.getArgument(1));
        } else {
            loadVar(instr.getArgument(1), "$k0");
        }
        if (instr.getArgument(0) instanceof ConstInt) {
            mipsModule.addText(new Li(reg2, ((ConstInt) instr.getArgument(0)).getValue()));
        } else {
            if (getValue(instr.getArgument(0)) != null) {
                reg2 = getValue(instr.getArgument(0));
            } else {
                loadVar(instr.getArgument(0), "$k1");
            }
//            int offset2 = stackOffsetMap.get(instr.getArgument(0).getName());
//            mipsModule.addText(new Lw("$k1", "$sp", offset2));
        }
        mipsModule.addText(new Sw(reg2, reg, 0)); // store the value to address
        // TODO:check
    }

    private void generateReturnInstr(ReturnInstr instr) {

        if (instr.getArgument(0) instanceof ConstInt) {
            mipsModule.addText(new Li("$v0", ((ConstInt) instr.getArgument(0)).getValue()));
        } else {
            if (!instr.getArguments().isEmpty()) {
                if (getValue(instr.getArgument(0)) != null) {
                    mipsModule.addText(new Move("$v0", getValue(instr.getArgument(0))));
                } else {
                    loadVar(instr.getArgument(0), "$v0");
                }
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
                        String reg = getValue(instr.getArgument(1));
                        if (reg != null) {
                            mipsModule.addText(new Move("$a0", reg));// move reg's value to k0
                        } else {
                            loadVar(instr.getArgument(1), "$a0");
                        }
//                        loadVar(instr.getArgument(1), "$a0");
                    }
                    mipsModule.addText(new Li("$v0", 1));
                    mipsModule.addText(new SyscallInstr(1));
                    break;
                case "putch":
                    if (instr.getArgument(1) instanceof ConstInt) {
                        mipsModule.addText(new Li("$a0", ((ConstInt) instr.getArgument(1)).getValue()));
                    } else {
                        String reg = getValue(instr.getArgument(1));
                        if (reg != null) {
                            mipsModule.addText(new Move("$a0", reg));// move reg's value to k0
                        } else {
                            loadVar(instr.getArgument(1), "$a0");
                        }
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
                        mipsModule.addText(new MipsCalc("addiu", "$k0", "$sp", offset));
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
        releaseAllRegister();
        for (int i = 1; i < instr.getArguments().size(); i++) {
            String reg = "$k0";
            if (instr.getArguments().get(i) instanceof ConstInt) {
                mipsModule.addText(new Li("$k0", ((ConstInt) instr.getArguments().get(i)).getValue()));
            } else {
                reg = getValue(instr.getArguments().get(i));
                if (reg == null) {
                    reg = "$k0";
                    loadVar(instr.getArguments().get(i), "$k0");
                }
            }
            //stackOffset -= 4;
            mipsModule.addText(new Sw(reg, "$sp", stackOffset - 8 - 4 * i));
            // 这里的8，分别是要保存起来的sp和ra。所以再过了8才是真正函数栈的开始。
        }
        // 设置函数栈
        mipsModule.addText(new Sw("$sp", "$sp", stackOffset - 4));// 先存sp，再存ra
        mipsModule.addText(new Sw("$ra", "$sp", stackOffset - 8));
        mipsModule.addText(new MipsCalc("addiu", "$sp", "$sp", stackOffset - 8));

        // TODO: 函数调用的跳转j指令会自动设置 $ra 和 $sp吗？

        mipsModule.addText(new J("jal", "BB_" + instr.getArgument(0).getName().substring(1)));

        mipsModule.addText(new Lw("$ra", "$sp", 0));
        mipsModule.addText(new Lw("$sp", "$sp", 4)); // TODO: check: lw $sp, 4($sp)
        if (!instr.getName().isEmpty()) {
            // return value
            String resReg = AllocReg(instr);
            if (resReg != null) {
                mipsModule.addText(new Move(resReg, "$v0"));
            } else {
                stackOffset -= 4;
                stackOffsetMap.put(instr.getName(), stackOffset);
                mipsModule.addText(new Sw("$v0", "$sp", stackOffset));
            }
        }
    }

//    private void loadVar(Value var, String reg) {
//        loadVar(var, reg, false);
//    }

    private String loadVar(Value var, String reg) {
        if (getValue(var) != null) {
            return getValue(var);
        }
        if (beforeInstr != null && beforeInstr instanceof StoreInstr) {
            StoreInstr storeInstr = (StoreInstr) beforeInstr;
            if (storeInstr.getArgument(1).equals(var)) { // pointer === var
//                System.err.println("[Opt] Store: " + storeInstr.getArgument(1).getName() + " get:" + var.getName());
                return reg;
            }
        }
        // set var's value into reg
        if (var instanceof GlobalVariable) {
            mipsModule.addText(new La(reg, var.getName().substring(1)));
        } else if (var instanceof StringLiteral) {
            mipsModule.addText(new La(reg, var.getName().substring(1)));
        } else {
            int offset = stackOffsetMap.get(var.getName());
            mipsModule.addText(new Lw(reg, "$sp", offset));
        }
        return reg;
    }

    private void generateGetelementptrInstr(GetelementptrInstr instr) {
        // if global!!!
        String reg = "$k0";
        if (getValue(instr.getArgument(0)) != null) {
            reg = getValue(instr.getArgument(0));
            mipsModule.addText(new Move("$k0", reg));// move reg's value to k0
        } else {
            loadVar(instr.getArgument(0), "$k0");
        }

        if (instr.getArgument(1) instanceof ConstInt) {
            mipsModule.addText(new MipsCalc("addiu", "$k0", "$k0", ((ConstInt) instr.getArgument(1)).getValue() * 4));
        } else {
            if (getValue(instr.getArgument(1)) != null) {
                reg = getValue(instr.getArgument(1));
                mipsModule.addText(new Move("$k1", reg));// move reg's value to k0
            } else {
                loadVar(instr.getArgument(1), "$k1");
            }
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
            } else if (type.isPointer()) {
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
        String reg1 = "$k0", reg2 = "$k1";
        Operator op = instr.getCmp();
        if (instr.getArgument(0) instanceof ConstInt) {
//            if (((ConstInt) instr.getArgument(0)).getValue() == 0) {
//
//            } //TODO: 常数优化，提前运算
            mipsModule.addText(new Li("$k0", ((ConstInt) instr.getArgument(0)).getValue()));
        } else {
            reg1 = loadVar(instr.getArgument(0), "$k0");
            //mipsModule.addText(new Lw("$k0", "$sp", stackOffsetMap.get(instr.getArgument(0).getName())));
        }
        if (instr.getArgument(1) instanceof ConstInt) {
            mipsModule.addText(new Li("$k1", ((ConstInt) instr.getArgument(1)).getValue()));
        } else {
            reg2 = loadVar(instr.getArgument(1), "$k1");
            //mipsModule.addText(new Lw("$k1", "$sp", stackOffsetMap.get(instr.getArgument(1).getName())));
        }
        String reg3 = AllocReg(instr);
        if (reg3 == null) {
            reg3 = "$k0";
        }
        switch (op) {
            case eq:
                mipsModule.addText(new MipsCalc("seq", reg3, reg1, reg2));
                break;
            case ne:
                mipsModule.addText(new MipsCalc("sne", reg3, reg1, reg2));
                break;
            case sge:
                mipsModule.addText(new MipsCalc("sge", reg3, reg1, reg2));
                break;
            case sgt:
                mipsModule.addText(new MipsCalc("sgt", reg3, reg1, reg2));
                break;
            case sle:
                mipsModule.addText(new MipsCalc("sle", reg3, reg1, reg2));
                break;
            case slt:
                mipsModule.addText(new MipsCalc("slt", reg3, reg1, reg2));
                break;
            default:
                System.err.println("error: icmp op not supported");
                break;
        }
        if (reg3.equals("$k0")) {
            stackOffset -= 4;
            stackOffsetMap.put(instr.getName(), stackOffset);
            mipsModule.addText(new Sw("$k0", "$sp", stackOffset));
        }
    }

    private void generateTruncInstr(TruncInstr instr) {
        //  %5 = trunc i32 %4 to i16 暂时都用4位存，不需要浪费指令，直接加表即可？
        if (instr.getArgument(0) instanceof ConstInt) {
            mipsModule.addText(new Li("$k0", ((ConstInt) instr.getArgument(0)).getValue()));
            stackOffset -= 4;
            stackOffsetMap.put(instr.getName(), stackOffset);
            mipsModule.addText(new Sw("$k0", "$sp", stackOffset));
        } else {
            if (getValue(instr.getArgument(0)) != null) {
                varAt.put(instr, getValue(instr.getArgument(0)));
            } else {
                stackOffsetMap.put(instr.getName(), stackOffsetMap.get(instr.getArgument(0).getName()));
            }
        }
    }

    private void generateZextInstr(ZextInstr instr) {
        if (instr.getArgument(0) instanceof ConstInt) {
            mipsModule.addText(new Li("$k0", ((ConstInt) instr.getArgument(0)).getValue()));
            stackOffset -= 4;
            stackOffsetMap.put(instr.getName(), stackOffset);
            mipsModule.addText(new Sw("$k0", "$sp", stackOffset));
        } else {
            if (getValue(instr.getArgument(0)) != null) {
                varAt.put(instr, getValue(instr.getArgument(0)));
            } else {
                stackOffsetMap.put(instr.getName(), stackOffsetMap.get(instr.getArgument(0).getName()));
            }
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
