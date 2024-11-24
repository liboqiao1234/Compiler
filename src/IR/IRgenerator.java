package IR;

import AST.Nodes.*;
import IR.Instr.AllocaInstr;
import IR.Instr.CallInstr;
import IR.Instr.GetelementptrInstr;
import IR.Instr.IcmpInstr;
import IR.Instr.LoadInstr;
import IR.Instr.Operator;
import IR.Instr.StoreInstr;
import IR.Instr.CalcInstr;
import IR.Instr.Terminators.BrInstr;
import IR.Instr.Terminators.ReturnInstr;
import IR.Instr.TruncInstr;
import IR.Instr.ZextInstr;
import IR.Type.*;
import IR.Value.*;
import MyToken.Token;
import Symbol.SymbolTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IRgenerator {
    private static IRgenerator instance = new IRgenerator();
    private int reg_num = -1;
    private LLVMModule module = new LLVMModule();
    private SymbolTable<Value> curSymTable = new SymbolTable<Value>(null, 1);
    private SymbolTable<ConstValue> constSymTable = new SymbolTable<ConstValue>(null, 1);
    private BasicBlock ForCheck = null;
    private BasicBlock ForInside = null;
    private BasicBlock ForAfter = null;
    private BasicBlock afterForNext = null;

    public static IRgenerator getInstance() {
        return instance;
    }

    private Value testValueNeedToBeDeleted = new Value("ERROR!", new IRType());
    private BasicBlock curbb;

    private IRgenerator() {

    }

    private void createSonTable() {
        curSymTable = curSymTable.createSon();
        constSymTable = constSymTable.createSon();
    }

    private void exitSonTable() {
        curSymTable = curSymTable.getFather();
        constSymTable = constSymTable.getFather();
    }

    private int calc(Token op, int a, int b) {
        switch (op.getType()) {
            case PLUS:
                return a + b;
            case MINU:
                return a - b;
            case MULT:
                return a * b;
            case DIV:
                return a / b;
            case MOD:
                return a % b;
            default:
                return 0;
        }
    }

    private List<Value> ConvertWhenIcmp(Value value1, Value value2) {
        IRType type1 = value1.getType();
        IRType type2 = value2.getType();
        if (type1.equals(type2)) {
            return Arrays.asList(value1, value2);
        }
        assert type1 instanceof IntType && type2 instanceof IntType;
        int maxbit = Math.max(((IntType) type1).getBits(), ((IntType) type2).getBits());
        IRType targetType = new IntType(maxbit);
        return Arrays.asList(Convert(value1, targetType), Convert(value2, targetType));
    }

    private Value Convert(Value value, IRType type) {
        if (value == null) {
            return null;
        }
        if (value.getType().equals(type)) {
            return value;
        }
        // func call : para is array[]
        if (type.isPointer() && value.getType().isPointer() && ((PointerType) value.getType()).getPointeeType().isArray()) {
            System.out.println("func call : para is array[]");
            GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), value, new ConstInt(0), curbb);
            curbb.addInstr(ptrInstr);
            return ptrInstr;
        }
        if (type == IntType.I1) {
            IcmpInstr icmpInstr = new IcmpInstr(Operator.ne,"%" + (++reg_num), value, new ConstInt(0), curbb);
            curbb.addInstr(icmpInstr);
            return icmpInstr;
        }

        if (value.getType() == IntType.I32) {
            // then type must be I8
            TruncInstr truncInstr = new TruncInstr("%" + (++reg_num), value, new Value("", type), curbb);
            curbb.addInstr(truncInstr);
            return truncInstr;
        } else {
            // then type must be I32
            ZextInstr zextInstr = new ZextInstr("%" + (++reg_num), value, new Value("", type), curbb);
            curbb.addInstr(zextInstr);
            return zextInstr;
        }
    }

    private IRType BType2IRType(BType type) {
        if (type.equals("int")) {
            return IntType.I32;
        } else {
            return IntType.I8;
        }
    }

    private Operator Op2Op(String op) {
        switch (op) {
            case "+":
                return Operator.add;
            case "-":
                return Operator.sub;
            case "*":
                return Operator.mul;
            case "/":
                return Operator.sdiv;
            case "%":
                return Operator.srem;
            case "==":
                return Operator.eq;
            case "!=":
                return Operator.ne;
            case ">":
                return Operator.sgt;
            case ">=":
                return Operator.sge;
            case "<":
                return Operator.slt;
            case "<=":
                return Operator.sle;
            default:
                return null;
        }
    }

    public void generateIR(CompUnit root) {
        visitCompUnit(root);
    }

    public String output() {
        return module.toString();
    }

    private void visitCompUnit(CompUnit root) {
        // 1. add 5 lib func.
        // 2. Decl, ;
        Function tmp = new Function("@getint", IntType.I32, true);
        curSymTable.insert("getint", tmp);
        module.addDeclare(tmp);
        tmp = new Function("@getchar", IntType.I32, true);
        curSymTable.insert("getchar", tmp);
        module.addDeclare(tmp);
        tmp = new Function("@putint", IntType.VOID, new ArrayList<>(Arrays.asList(IntType.I32)), true);
        curSymTable.insert("putint", tmp);
        module.addDeclare(tmp);
        tmp = new Function("@putch", IntType.VOID, new ArrayList<>(Arrays.asList(IntType.I32)), true);
        curSymTable.insert("putch", tmp);
        module.addDeclare(tmp);
        tmp = new Function("@putstr", IntType.VOID, new ArrayList<>(Arrays.asList(new PointerType(IntType.I8))), true);
        curSymTable.insert("putstr", tmp);
        module.addDeclare(tmp);


        for (Decl decl : root.getDecls()) {
            visitDecl(decl);
        }

        for (FuncDef funcDef : root.getFuncDefs()) {
            visitFuncDef(funcDef);
        }
        visitMainFuncDef(root.getMainFuncDef());
    }

    private void visitDecl(Decl decl) {
        // ConstDecl | VarDecl;
        if (decl.getConstDecl() != null) {
            visitConstDecl(decl.getConstDecl());
        } else {
            visitVarDecl(decl.getVarDecl());
        }
    }

    private void visitVarDecl(VarDecl varDecl) {
        //  VarDecl → BType VarDef { ',' VarDef } ';'
        boolean isGlobal = curbb == null; // 如果bb是空，说明不在块内，全局；否则一定为局部。
        IRType type = BType2IRType(varDecl.getBType());
        for (VarDef varDef : varDecl.getVarDefs()) {
            visitVarDef(varDef, type, isGlobal);
        }
    }

    private void visitVarDef(VarDef varDef, IRType type, boolean isGlobal) {
        //  VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        String name = varDef.getIdent().getContent();
        if (isGlobal) {
            //TODO
            if (varDef.getConstExp() != null) {
                // 全局数组
                int size = visitConstExp(varDef.getConstExp());
                ArrayType arrayType = new ArrayType(type, size);
                GlobalVariable gv;
                if (varDef.getInitVal() != null) {
                    ArrayList<Value> initVals = visitInitVal(varDef.getInitVal(), type);
                    ArrayList<ConstValue> constInits = new ArrayList<>();
                    for (Value i : initVals) {
                        int val = ((ConstInt) i).getValue();
                        constInits.add(new ConstInt(val, type));
//                         constInits.add((ConstValue) i);
                    }
                    gv = new GlobalVariable(name, arrayType, new ConstArray(constInits, size));
                } else {
                    gv = new GlobalVariable(name, arrayType, new Zeroinitializer(arrayType));
                }

                //...
                module.addGlobalVariable(gv);
                curSymTable.insert(name, gv);
            } else {
                // 全局变量
                GlobalVariable gv;
                if (varDef.getInitVal() != null) {
                    ConstValue initVal = (ConstValue) visitInitVal(varDef.getInitVal(), type).get(0);
                    gv = new GlobalVariable(name, type, initVal);
                } else {
                    gv = new GlobalVariable(name, type, new ConstInt(0, type));
                }
                curSymTable.insert(name, gv);
                module.addGlobalVariable(gv);
            }
        } else {
            if (varDef.getConstExp() != null) {
                // 局部数组
                int size = visitConstExp(varDef.getConstExp());
                ArrayType arrayType = new ArrayType(type, size);
                AllocaInstr alloca = new AllocaInstr("%" + (++reg_num), arrayType, curbb);//得到一个指向数组的指针
                curbb.addInstr(alloca);
                curSymTable.insert(name, alloca);

                if (varDef.getInitVal() != null) {
                    // 有初值，在这里赋值
                    ArrayList<Value> initVals = visitInitVal(varDef.getInitVal(), type);
                    for (int i = 0; i < initVals.size(); i++) {
                        GetelementptrInstr ptr = new GetelementptrInstr("%" + (++reg_num), alloca, new ConstInt(i), curbb);
                        StoreInstr storeInstr = new StoreInstr(initVals.get(i), ptr, curbb);
                        curbb.addInstr(ptr);
                        curbb.addInstr(storeInstr);
                    }
//                    dflaskj;
                }
            } else {
                // 局部变量
                AllocaInstr alloca = new AllocaInstr("%" + (++reg_num), type, curbb);
                curbb.addInstr(alloca);
                curSymTable.insert(name, alloca);
                if (varDef.getInitVal() != null) {
                    Value initVal = visitInitVal(varDef.getInitVal(), type).get(0);
                    StoreInstr storeInstr = new StoreInstr(initVal, alloca, curbb);
                    curbb.addInstr(storeInstr);
                }
            }
        }

    }

    private ArrayList<Value> visitInitVal(InitVal initVal) {
        return visitInitVal(initVal, IntType.I32);
    }

    private ArrayList<Value> visitInitVal(InitVal initVal, IRType needType) {
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        // 返回值为最终计算好初值的%i 的list，如果是单个变量，则取0号位
        ArrayList<Value> initVals = new ArrayList<>();
        boolean isConst = curbb == null;

        if (initVal.getExp() != null) {
            // single variable
            initVals.add(visitExp(initVal.getExp(), isConst, needType));
        } else if (initVal.getStringConst() != null) {
            String string = initVal.getStringConst().getContent();
            string = string.substring(1, string.length() - 1);
//            StringLiteral sl = new StringLiteral(string);
//            module.addStringLiteral(sl);
//            initVals.add(sl);
            for (int i = 0; i < string.length(); i++) {
                initVals.add(new ConstInt(string.charAt(i), IntType.I8));
            }
        } else {
            // exps for array
            for (Exp exp : initVal.getExpList()) {
                initVals.add(visitExp(exp, isConst, needType));
            }
        }
        return initVals;
    }

    private void visitConstDecl(ConstDecl constDecl) {
        // 'const' BType ConstDef { ',' ConstDef } ';'
        BType type = constDecl.getBType();
        for (ConstDef constDef : constDecl.getConstDefs()) {
            visitConstDef(constDef, type);
        }
    }

    private void visitConstDef(ConstDef constDef, BType type) {
        //  ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal, 变量 or 数组
        boolean isGlobal = curbb == null;
        String name = constDef.getIdent().getContent();
        if (constDef.getConstExp() != null) {
            // array
            int size = visitConstExp(constDef.getConstExp());
            IRType elementType = BType2IRType(type);
            ArrayType arrayType = new ArrayType(elementType, size);
            ConstValue initVal = visitConstInitVal(constDef.getConstInitVal(), size, isGlobal, elementType);
            if (isGlobal) {
                // 全局常数组
                GlobalVariable globalVar = new GlobalVariable(name, initVal, true);
                module.addGlobalVariable(globalVar);
                curSymTable.insert(name, globalVar);
                constSymTable.insert(name, globalVar.getInitValue());
            } else {
                // 局部常数组，局部变量在basicBlock中分配，直接给增加语句即可
                AllocaInstr allocaInst = new AllocaInstr("%" + (++reg_num), arrayType, curbb);
                // curSymTable.insert(allocaInst.getName(), allocaInst);
                curbb.addInstr(allocaInst);
                ArrayList<ConstValue> initVals = ((ConstArray) initVal).getElements();
                assert elementType.equals(initVals.get(0).getType());
                //TODO: C&D
                for (int i = 0; i < initVals.size(); i++) {
                    GetelementptrInstr ptr = new GetelementptrInstr("%" + (++reg_num), allocaInst, new ConstInt(i, elementType), curbb);
                    StoreInstr storeInstr = new StoreInstr(initVals.get(i), ptr, curbb);
                    curbb.addInstr(ptr);
                    curbb.addInstr(storeInstr);
                }
                curSymTable.insert(name, allocaInst);//局部变量放一个alloc语句，用来找是哪个name。
                constSymTable.insert(name, initVal);
            }
        } else {
            // const var
            IRType varType = BType2IRType(type);
            ConstValue initVal = new ConstInt(((ConstInt) visitConstInitVal(constDef.getConstInitVal(), 0, isGlobal, varType)).getValue(), varType);

            if (isGlobal) {
                GlobalVariable globalVar = new GlobalVariable(name, initVal, true);
                module.addGlobalVariable(globalVar);
                curSymTable.insert(name, globalVar);
                constSymTable.insert(name, globalVar.getInitValue());
            } else {
                // 局部常变量
                AllocaInstr allocaInst = new AllocaInstr("%" + (++reg_num), BType2IRType(type), curbb);
                StoreInstr storeInst = new StoreInstr(initVal, allocaInst, curbb);
                curbb.addInstr(allocaInst);
                curbb.addInstr(storeInst);
                curSymTable.insert(name, allocaInst);//局部变量放一个alloc语句，用来找是哪个name。
                constSymTable.insert(name, initVal);
            }
        }
    }

    private ConstValue visitConstInitVal(ConstInitVal constInitVal, int length, boolean isGlobal, IRType elementType) {
        // isGlobal : can/ can't use zeroInit
        //  ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        if (constInitVal.getConstExp() != null) {
            return new ConstInt(visitConstExp(constInitVal.getConstExp()), elementType);
            //虽然这里elementType没用，因为后面会getValue重新生成
        } else if (constInitVal.getStringConst() != null) {
            String str = constInitVal.getStringConst().getContent();
            str = str.substring(1, str.length() - 1);
            ArrayList<ConstValue> constValues = new ArrayList<>();
            // 这里其实就是一个i8的数组
            for (int i = 0; i < str.length(); i++) {
                //TODO: 转义字符
                ConstValue ch = new ConstInt(str.charAt(i));
                constValues.add(ch);
            }
            return new ConstArray(constValues, length);
        } else {
            // array
            ArrayList<ConstValue> constValues = new ArrayList<>();
            for (ConstExp constExp : constInitVal.getConstExps()) {
                int value = visitConstExp(constExp);
                ConstValue constValue = new ConstInt(value);
                constValues.add(constValue);
            }
            if (constValues.isEmpty()) {
                if (isGlobal)
                    return new Zeroinitializer(new ArrayType(elementType, length));
                else
                    return new ConstArray(elementType, length);
            }
            return new ConstArray(constValues, length);
        }
    }

    private int visitConstExp(ConstExp constExp) {
        return visitConstExp(constExp, IntType.I32);
    }

    private int visitConstExp(ConstExp constExp, IRType needType) {
        //  ConstExp → AddExp, no need to generate, BB = null
        return ((ConstInt) visitAddExp(constExp.getAddExp(), true, needType)).getValue();
    }

    private Value visitAddExp(AddExp addExp, boolean isConst, IRType needType) {
        // 如果是const，返回ConstInt代表数值，否则返回value, TODO check
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        if (isConst) {
            int res = 0;
            ArrayList<MulExp> mulExps = addExp.getMulExps();
            ArrayList<Token> ops = addExp.getAddOps();
            for (int i = 0; i < mulExps.size(); i++) {
                if (i == 0) {
                    res = ((ConstInt) visitMulExp(mulExps.get(i), true)).getValue();
                } else {
                    res = calc(ops.get(i - 1), res, ((ConstInt) visitMulExp(mulExps.get(i), true)).getValue());
                }
            }
            return new ConstInt(res, needType);
        } else {
            Value res = visitMulExp(addExp.getMulExps().get(0), false, needType);
            for (int i = 1; i < addExp.getMulExps().size(); i++) {
                Value mulExpVal = visitMulExp(addExp.getMulExps().get(i), false, needType);
                if (needType != IntType.I32) {
                    res = Convert(res, IntType.I32);
                    mulExpVal = Convert(mulExpVal, IntType.I32);
                }
                CalcInstr calcInstr = new CalcInstr(Op2Op(addExp.getAddOps().get(i - 1).getContent()), "%" + (++reg_num), res, mulExpVal, curbb);
                curbb.addInstr(calcInstr);
                res = calcInstr;
            }
            return Convert(res, needType);
        }
    }

    private Value visitMulExp(MulExp mulExp, boolean isConst) {
        return visitMulExp(mulExp, isConst, IntType.I32);
    }

    private Value visitMulExp(MulExp mulExp, boolean isConst, IRType needType) {
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        ArrayList<UnaryExp> unaryExps = mulExp.getUnaryExps();
        ArrayList<Token> ops = mulExp.getOperators();
        if (isConst) {
            int res = 0;

            for (int i = 0; i < unaryExps.size(); i++) {
                if (i == 0) {
                    res = ((ConstInt) visitUnaryExp(unaryExps.get(i), true)).getValue();
                } else {
                    res = calc(ops.get(i - 1), res, ((ConstInt) visitUnaryExp(unaryExps.get(i), true)).getValue());
                }
            }
            return new ConstInt(res, needType);
        } else {
            Value res = visitUnaryExp(unaryExps.get(0), false, needType);
            for (int i = 1; i < unaryExps.size(); i++) {
                Value unaryExpVal = visitUnaryExp(unaryExps.get(i), false, needType);
                if (needType != IntType.I32) {
                    res = Convert(res, IntType.I32);
                    unaryExpVal = Convert(unaryExpVal, IntType.I32);
                }
                CalcInstr calcInstr = new CalcInstr(Op2Op(ops.get(i - 1).getContent()), "%" + (++reg_num), res, unaryExpVal, curbb);
                curbb.addInstr(calcInstr);
                res = calcInstr;
            }
            return Convert(res, needType);
        }
    }

    private Value visitUnaryExp(UnaryExp unaryExp, boolean isConst) {
        return visitUnaryExp(unaryExp, isConst, IntType.I32);
    }

    private Value visitUnaryExp(UnaryExp unaryExp, boolean isConst, IRType needType) {
        //  UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        // {UnaryOp} (PrimaryExp | Ident ([FuncRParams]))
        boolean isCond = false;
        boolean reverseFlag = false;
        int minus = 1;
        if (unaryExp.getUnaryOps() != null) {
            for (UnaryOp unaryOp : unaryExp.getUnaryOps()) {
                String op = unaryOp.getOp().getContent();
                if (op.equals("!")) {
                    isCond = true;
                    reverseFlag = !reverseFlag;
                } else if (op.equals("+")) {
                    // no need.
                } else if (op.equals("-")) {
                    minus = -minus;
                } else {
                    System.err.println("error in checking unaryOPs!");
                }
            }
        }
        if (isConst) {
            int res = 0;
            if (unaryExp.getPrimaryExp() != null) {
                // PrimaryExp
                res = ((ConstInt) visitPrimaryExp(unaryExp.getPrimaryExp(), true)).getValue();
            } else {
                System.err.println("Error: func call in const mode");
                // 函数调用
                //res = visitFuncCall(unaryExp.getIdent(), unaryExp.getFuncRParams(), true, bb);
            }
            if (isCond) {
                assert (needType == IntType.I1);
                // TODO delete;
                System.out.println("Check needtype for cond");
                if (reverseFlag) {
                    // 虽然! + -在前面都是一元表达式，理论上对于+!1应该是+0最后是0
                    // 但是特殊性在于，有!意味着一定是条件表达式，如果是0，无论如何+-加上!都是true，反之亦然。

                    return new ConstInt((res != 0) ? 0 : 1, IntType.I1);
                }
                return new ConstInt((res != 0) ? 1 : 0, IntType.I1);
            }
            return new ConstInt(res * minus, needType);
        } else {
            Value res;
            if (unaryExp.getPrimaryExp() != null) {
                res = visitPrimaryExp(unaryExp.getPrimaryExp(), false, needType);
//                if (res.getType() == IntType.I8) {
//                    res = new ZextInstr("%" + (++reg_num), res, new Value("",IntType.I32), bb);
//                    curbb.addInstr((ZextInstr) res);
//                }
            } else if (unaryExp.getIdent() != null) {
                // function call; func call
                Function func = (Function) curSymTable.find(unaryExp.getIdent().getContent());
                IRType retType = ((FunctionType) func.getType()).getRetType();
                if (unaryExp.getFuncRParams() != null) {
                    ArrayList<Value> params = new ArrayList<>();
                    for (int i = 0; i < unaryExp.getFuncRParams().getExps().size(); i++) {
                        Exp funcRParam = unaryExp.getFuncRParams().getExps().get(i);
                        params.add(visitExp(funcRParam, false, func.getParaList().get(i)));
                    }
                    if (retType == IntType.VOID) {
                        CallInstr callInstr = new CallInstr(func, curbb, params); // Void没返回值，直接return
                        curbb.addInstr(callInstr);
                        return null;
                    } else {
                        CallInstr callInstr = new CallInstr("%" + (++reg_num), func, curbb, params);
                        curbb.addInstr(callInstr);
                        res = callInstr;
                    }
                } else {
                    if (retType == IntType.VOID) {
                        CallInstr callInstr = new CallInstr(func, curbb, null); // Void没返回值，直接return
                        curbb.addInstr(callInstr);
                        return null;
                    } else {
                        res = new CallInstr("%" + (++reg_num), func, curbb, null);
                        curbb.addInstr((CallInstr) res);
                    }
                }
            } else {
                System.err.println("error in unary Exp!");
                return new Value("error", new IRType());
            }
            if (isCond) {
                assert (needType == IntType.I1);
                System.out.println("Check needType for cond");
                if (reverseFlag) {
                    IcmpInstr icmpInstr = new IcmpInstr(Operator.eq, "%" + (++reg_num), res, new ConstInt(0, IntType.I1), curbb);
                    curbb.addInstr(icmpInstr);
                    return icmpInstr;
                } else {
                    IcmpInstr icmpInstr = new IcmpInstr(Operator.ne, "%" + (++reg_num), res, new ConstInt(0, IntType.I1), curbb);
                    curbb.addInstr(icmpInstr);
                    return icmpInstr;
                }
            } else {
                if (minus == 1) {
                    return Convert(res, needType);
                }
                if (needType != IntType.I32) {
                    res = Convert(res, IntType.I32);
                }
                CalcInstr calcInstr = new CalcInstr(Operator.sub, "%" + (++reg_num), new ConstInt(0, IntType.I32), res, curbb);
                curbb.addInstr(calcInstr);
                return Convert(calcInstr, needType);
            }
        }
    }

    private Value visitPrimaryExp(PrimaryExp primaryExp, boolean isConst) {
        return visitPrimaryExp(primaryExp, isConst, IntType.I32);
        //停在这里，因为char函数会返回i8，没必要转到32再转回来，因此设置needType，遇到i32计算再放大。
    }

    private Value visitPrimaryExp(PrimaryExp primaryExp, boolean isConst, IRType needType) {
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if (isConst) {
            int res = 0;
            if (primaryExp.getExp() != null) {
                res = ((ConstInt) visitExp(primaryExp.getExp(), true)).getValue();
            } else if (primaryExp.getLVal() != null) {
                //理论上不包括数组元素
                res = ((ConstInt) visitLVal(primaryExp.getLVal(), true)).getValue();
            } else if (primaryExp.getNumber() != null) {
                res = primaryExp.getNumber().getNum();
            } else if (primaryExp.getCharacter() != null) {
                res = primaryExp.getCharacter().getCharConst();
            } else {
                System.err.println("error in primaryExp const!");
            }
            return new ConstInt(res, needType);
        } else {
            if (primaryExp.getLVal() != null) {
                return visitLVal(primaryExp.getLVal(), false, needType);
            } else if (primaryExp.getExp() != null) {
                return visitExp(primaryExp.getExp(), false, needType);
            } else if (primaryExp.getNumber() != null) {
                return new ConstInt(primaryExp.getNumber().getNum(), needType);
            } else if (primaryExp.getCharacter() != null) {
                return new ConstInt(primaryExp.getCharacter().getCharConst(), needType);
            } else {
                System.err.println("error in primaryExp not const!");
                return new Value("error", new IRType());
            }
        }
    }

    private Value visitExp(Exp exp, boolean isConst) {
        return visitExp(exp, isConst, IntType.I32);
    }

    private Value visitExp(Exp exp, boolean isConst, IRType needType) {
        return visitAddExp(exp.getAddExp(), isConst, needType);
    }

    private Value visitLVal(LVal lVal, boolean isConst) {
        return visitLVal(lVal, isConst, IntType.I32);
    }

    private Value visitLVal(LVal lVal, boolean isConst, IRType needType) {
        // 这里返回的是读出来的值。
        //  LVal → Ident ['[' Exp ']']
        if (isConst) {
            if (lVal.getExp() != null) {
                System.err.println("Error! Array as lVal in const mode!");
                return new Value("Error in lVal", new IRType());
            } else {
                // TODO C&D
                //System.err.println(needType.toString()+ " " + Convert(constSymTable.find(lVal.getIdent().getContent()), needType, bb));
                return constSymTable.find(lVal.getIdent().getContent());
            }
        } else {
            String name = lVal.getIdent().getContent();
            Value res = curSymTable.find(name);

            if (lVal.getExp() != null) {
                // a[exp]; global, local
                // TODO: load !
                GetelementptrInstr ptrInstr;
                if (((PointerType) res.getType()).getPointeeType() instanceof ArrayType) {
                    Value expval = visitExp(lVal.getExp(), false);
                    ptrInstr = new GetelementptrInstr("%" + (++reg_num), res, expval, curbb);
                    curbb.addInstr(ptrInstr);
                } else if (((PointerType) res.getType()).getPointeeType() instanceof PointerType) {
                    // i32**;
                    LoadInstr loadInstr1 = new LoadInstr("%" + (++reg_num), res, curbb);
                    curbb.addInstr(loadInstr1);
                    Value expval = visitExp(lVal.getExp(), false);
                    ptrInstr = new GetelementptrInstr("%" + (++reg_num), loadInstr1, expval, curbb, true);
                    curbb.addInstr(ptrInstr);
                } else {
                    System.err.println("error in visitLVal !");
                    ptrInstr = null;
                }
//                LoadInstr loadInstr1 = new LoadInstr("%" + (++reg_num), res, bb);
//                curbb.addInstr(loadInstr1);
//                Value expval = visitExp(lVal.getExp(), false, bb);
//                GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), loadInstr1, expval, bb);
//                curbb.addInstr(ptrInstr);
                // TODO check
                LoadInstr loadInstr = new LoadInstr("%" + (++reg_num), ptrInstr, curbb);
                curbb.addInstr(loadInstr);
                return Convert(loadInstr, needType);
            } else {
                // global variable or local variable
                // array without index
                assert (res.getType().isPointer());
                if (((PointerType) res.getType()).getPointeeType().isArray()) {
                    // get the array i32*
                    {
                        IRType basicType = ((ArrayType) ((PointerType) res.getType()).getPointeeType()).getElementType();
                        assert (Objects.equals(needType, new PointerType(basicType)));
                    }
                    GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), res, new ConstInt(0), curbb);
                    curbb.addInstr(ptrInstr);
                    return Convert(ptrInstr, needType);
                } else {
                    LoadInstr loadInstr = new LoadInstr("%" + (++reg_num), res, curbb);
                    curbb.addInstr(loadInstr);
                    return Convert(loadInstr, needType);
                }

            }

        }
    }


    private void visitMainFuncDef(MainFuncDef mainFuncDef) {
        reg_num = 0;
        IRType retType = IntType.I32;
        String funcName = "@main";
        Function func = new Function(funcName, retType);
        curSymTable.insert("main", func);


        createSonTable();
        BasicBlock bb = func.getFirstBlock();
        curbb = bb;
        visitBlock(mainFuncDef.getBlock(), func, false);
        if (curbb != bb) {
            func.addBlock(curbb);
        }
        module.addFunction(func);
        exitSonTable();
    }

    private void visitFuncDef(FuncDef funcDef) {
        reg_num = -1;

        ArrayList<IRType> paramTypes = new ArrayList<>();
        if (funcDef.getFuncFParams() != null) {
            IRType tmpType;
            for (FuncFParam funcFParam : funcDef.getFuncFParams().getFuncFParams()) {
                tmpType = BType2IRType(funcFParam.getBType());
                if (funcFParam.getLeftBracket() != null) {
                    // array
                    tmpType = new PointerType(tmpType);
                    // 指向数组的指针？
                }
                paramTypes.add(tmpType);
            }
            reg_num = paramTypes.size();
        } else {
            reg_num = 0;
        }

        FuncType funcType = funcDef.getFuncType();
        IRType retType;
        if (funcType.equals("void")) {
            retType = IntType.VOID;
        } else if (funcType.equals("int")) {
            retType = IntType.I32;
        } else {
            retType = IntType.I8;
        }
        String funcName = funcDef.getIdent().getContent();
        Function func = new Function("@" + funcName, retType, paramTypes);

        curSymTable.insert(funcName, func);

        createSonTable();
        // alloca & store ; 符号表！
        BasicBlock bb = func.getFirstBlock();
        curbb = bb;
        curSymTable.insert(funcName, func);
        if (!paramTypes.isEmpty()) {
            for (int i = 0; i < paramTypes.size(); i++) {
                IRType paramType = paramTypes.get(i);
                String paraName = "%" + i;
                Argument paraArgument = new Argument(paraName, paramType);

                AllocaInstr para = new AllocaInstr("%" + (++reg_num), paramType, curbb);
                curSymTable.insert(funcDef.getFuncFParams().getFuncFParams().get(i).getIdent().getContent(), para);
                curbb.addInstr(para);
                StoreInstr store = new StoreInstr(paraArgument, para, curbb);
                curbb.addInstr(store);
            }
        }

        visitBlock(funcDef.getBlock(), func, false);
        if (curbb.getTerminator() == null) {
            ReturnInstr retInstr = new ReturnInstr(curbb);
            curbb.addInstr(retInstr);
            curbb.setTerminator(retInstr);
        }
        if (curbb != bb) {
            func.addBlock(curbb);
        }
        module.addFunction(func);
        exitSonTable();
    }

    private void visitBlock(Block block, Function func, boolean needCreate) {
        if (needCreate) {
            createSonTable();
        }
        for (BlockItem blockItem : block.getBlockItems()) {
            if (blockItem.getDecl() != null) {
                visitDecl(blockItem.getDecl());
            } else {
                visitStmt(blockItem.getStmt(), func);
            }
        }
        if (needCreate) {
            exitSonTable();
        }
    }

    private Value getLeftLval(LVal lVal) {
        if (lVal.getExp() == null) {
            return curSymTable.find(lVal.getIdent().getContent());
        } else {
            Value index = visitExp(lVal.getExp(), false);
            Value res = curSymTable.find(lVal.getIdent().getContent());
            if (((PointerType) res.getType()).getPointeeType() instanceof PointerType) {
                // i32**; means para[] in func
                LoadInstr loadInstr1 = new LoadInstr("%" + (++reg_num), res, curbb);
                curbb.addInstr(loadInstr1);
                res = loadInstr1;
                GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), res, index, curbb, true);
                curbb.addInstr(ptrInstr);
                return ptrInstr;
            }
            GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), res, index, curbb);
            curbb.addInstr(ptrInstr);
            return ptrInstr;
        }
    }

    private void visitStmt(Stmt stmt, Function func) {
        if (stmt.getAssignStmt() != null) {
            Value left = getLeftLval(stmt.getAssignStmt().getlVal());
            PointerType leftType = (PointerType) left.getType();
            IRType needType = leftType.getPointeeType();
            Value exp = visitExp(stmt.getAssignStmt().getExp(), false, needType);
            StoreInstr storeInstr = new StoreInstr(exp, left, curbb);
            curbb.addInstr(storeInstr);
        } else if (stmt.getBlock() != null) {
            visitBlock(stmt.getBlock(), func, true);
        } else if (stmt.getIfStmt() != null) {
            visitIfStmt(stmt.getIfStmt(), func);
        } else if (stmt.getForStmts() != null) {
            visitForStmts(stmt.getForStmts(), func);
        } else if (stmt.getBreak() != null) {
            assert afterForNext != null;
            BrInstr brInstr = new BrInstr(curbb, afterForNext);
            curbb.addInstr(brInstr);
            curbb.setTerminator(brInstr);
            func.addBlock(curbb);
            BasicBlock nextBB = new BasicBlock("" + (++reg_num), func);
            curbb = nextBB;
        } else if (stmt.getContinue() != null) {
            BrInstr brInstr;
            if (ForAfter != null) {
                brInstr = new BrInstr(curbb, ForAfter);
            } else if (ForCheck != null) {
                brInstr = new BrInstr(curbb, ForCheck);
            } else {
                brInstr = new BrInstr(curbb, ForInside);
            }
            curbb.addInstr(brInstr);
            curbb.setTerminator(brInstr);
            func.addBlock(curbb);
            BasicBlock nextBB = new BasicBlock("" + (++reg_num), func);
            curbb = nextBB;
        } else if (stmt.getReturn() != null) {
            if (stmt.getReturn().getExp() != null) {
                // return [exp];
                Value ret = visitExp(stmt.getReturn().getExp(), false, ((FunctionType) func.getType()).getRetType()); // i32
                ReturnInstr retInstr = new ReturnInstr(ret, curbb);
                curbb.addInstr(retInstr);
                curbb.setTerminator(retInstr);
            } else {
                ReturnInstr retInstr = new ReturnInstr(curbb);
                curbb.addInstr(retInstr);
                curbb.setTerminator(retInstr);

                // return;
            }
        } else if (stmt.getInputChar() != null) {
            CallInstr callInstr = new CallInstr("%" + (++reg_num), (Function) curSymTable.find("getchar"), curbb, null);
            curbb.addInstr(callInstr);
            TruncInstr truncInstr = new TruncInstr("%" + (++reg_num), callInstr, new Value("", IntType.I8), curbb);
            curbb.addInstr(truncInstr);
            Value lval = getLeftLval(stmt.getInputChar().getLVal());
            StoreInstr storeInstr = new StoreInstr(truncInstr, lval, curbb);
            curbb.addInstr(storeInstr);
        } else if (stmt.getInputInt() != null) {
            CallInstr callInstr = new CallInstr("%" + (++reg_num), (Function) curSymTable.find("getint"), curbb, null);
            curbb.addInstr(callInstr);
            Value lval = getLeftLval(stmt.getInputInt().getLVal());
            StoreInstr storeInstr = new StoreInstr(callInstr, lval, curbb);
            curbb.addInstr(storeInstr);
        } else if (stmt.getPrintf() != null) {
            visitPrintf(stmt.getPrintf());
        } else if (stmt.getExp() != null) {
            visitExp(stmt.getExp(), false);
        } else if (stmt.getSemicn() != null) {
            // should be the else one
            ;
        } else {
            System.out.println("stmt error");
        }
    }

    private void visitForStmt(ForStmt forStmt) {
        Value left = getLeftLval(forStmt.getlVal());
        PointerType leftType = (PointerType) left.getType();
        IRType needType = leftType.getPointeeType();
        Value exp = visitExp(forStmt.getExp(), false, needType);
        StoreInstr storeInstr = new StoreInstr(exp, left, curbb);
        curbb.addInstr(storeInstr);
    }

    private void visitForStmts(ForStmts forStmts, Function func) {
        if (forStmts.getForStmt1() != null) {
            visitForStmt(forStmts.getForStmt1());
        }
        // 先判断，然后循环体，然后 forStmt2
        BasicBlock inside = new BasicBlock("", func);
        BasicBlock nextBB = new BasicBlock("", func);
        BasicBlock checkFor = new BasicBlock("", func);
        if (forStmts.getCond() != null) { // 有checkfor
            checkFor.setName("" + (++reg_num));
            BrInstr brInstr = new BrInstr(curbb, checkFor);
            curbb.addInstr(brInstr);
            curbb.setTerminator(brInstr);
            func.addBlock(curbb);
            curbb = checkFor;
            ForCheck = checkFor;
            visitCond(forStmts.getCond(), inside, nextBB);
        } else { // 没有checkfor，直接回到循环体
            ForCheck = null;
            BrInstr brInstr = new BrInstr(curbb, inside);
            curbb.addInstr(brInstr);
            curbb.setTerminator(brInstr);
        }
        func.addBlock(curbb);
        BasicBlock after = null;
        if (forStmts.getForStmt2() != null) {
            after = new BasicBlock("", func);
            ForAfter = after;
        } else {
            ForAfter = null;
        }
        curbb = inside;
        inside.setName("" + (++reg_num));
        ForInside = inside;
        afterForNext = nextBB;
        visitStmt(forStmts.getStmt(), func);
        if (forStmts.getForStmt2() != null) { // 有after
            after.setName("" + (++reg_num));

            BrInstr brInstr = new BrInstr(curbb, after);
            curbb.addInstr(brInstr);
            curbb.setTerminator(brInstr);
            func.addBlock(curbb);
            curbb = after;
            visitForStmt(forStmts.getForStmt2());

            BrInstr brInstr2;
            if (forStmts.getCond() != null) {
                brInstr2 = new BrInstr(curbb, checkFor);
            } else {
                brInstr2 = new BrInstr(curbb, inside);
            }
            curbb.addInstr(brInstr2);
            curbb.setTerminator(brInstr2);
        } else {
            BrInstr brInstr2;
            if (forStmts.getCond() != null) {
                brInstr2 = new BrInstr(curbb, checkFor);
            } else {
                brInstr2 = new BrInstr(curbb, inside);
            }
            curbb.addInstr(brInstr2);
            curbb.setTerminator(brInstr2);
        }
        func.addBlock(curbb);
        curbb = nextBB;

        nextBB.setName("" + (++reg_num));
    }

    private Value visitRelExp(RelExp relExp) {
        ArrayList<AddExp> addExps = relExp.getAddExps();
        ArrayList<Token> ops = relExp.getRelOps();
        Value res = null;
        for (int i = 0; i < addExps.size(); i++) {
            AddExp addExp = addExps.get(i);
            Value value = visitAddExp(addExp, false, IntType.I32);// TODO: I32? null??
            if (i == 0) {
                res = value;
            } else {
                Token op = ops.get(i - 1);
                List<Value> tmp = ConvertWhenIcmp(res, value);
                res = tmp.get(0);
                value = tmp.get(1);
                IcmpInstr icmpInstr = new IcmpInstr(Op2Op(op.getContent()), "%" + (++reg_num), res, value, curbb);
                curbb.addInstr(icmpInstr);
                res = icmpInstr;
            }
        }
        return res;

    }

    private Value visitEqExp(EqExp eqExp) { // need to return I1
        ArrayList<RelExp> relExps = eqExp.getRelExps();
        ArrayList<Token> ops = eqExp.getEqOps();
        Value res = null;
        for (int i = 0; i < relExps.size(); i++) {
            RelExp relExp = relExps.get(i);
            Value value = visitRelExp(relExp);
            if (i == 0) {
                res = value;
            } else {
                Token op = ops.get(i - 1);
                List<Value> tmp = ConvertWhenIcmp(res, value);
                res = tmp.get(0);
                value = tmp.get(1);
                IcmpInstr icmpInstr = new IcmpInstr(Op2Op(op.getContent()), "%" + (++reg_num), res, value, curbb);
                curbb.addInstr(icmpInstr);
                res = icmpInstr;
            }
        }
        return Convert(res, IntType.I1);
    }

    private void visitLAndExp(LAndExp lAndExp, BasicBlock trueBB, BasicBlock falseBB) {
        // AND: 一旦失败直接到传进来的false，成功了也到next，直到最后一次：成功true，失败false
        Value res = null;
        for (int i = 0; i < lAndExp.getEqExps().size(); i++) {
            EqExp eqExp = lAndExp.getEqExps().get(i);
            Value value = visitEqExp(eqExp);
            if (i != lAndExp.getEqExps().size() - 1) {
                BasicBlock nextBB = new BasicBlock((++reg_num) + "", curbb.getFunction());
                BrInstr brInstr = new BrInstr(curbb, value, nextBB, falseBB);
                curbb.addInstr(brInstr);
                curbb.setTerminator(brInstr);
                curbb.getFunction().addBlock(curbb);
                curbb = nextBB;
            } else {
                BrInstr brInstr = new BrInstr(curbb, value, trueBB, falseBB);
                curbb.addInstr(brInstr);
                curbb.setTerminator(brInstr);
                //TODO check是否需要在这里更换 curbb
            }
        }
        //assert res != null;
        return;
    }

    private void visitLOrExp(LOrExp lorExp, BasicBlock trueBB, BasicBlock falseBB) {
        // OR: 失败了到next，一旦成功直接true
        // 如何做到 短路求值？ 假如 if (1-1)呢？ => 正常判断即可
        // or 为真（显式声明const能直接出 or 增加一个判断语句）时，直接停止   a || b && c ==>  if (a)
        for (int i = 0; i < lorExp.getLAndExps().size(); i++) {
            LAndExp lAndExp = lorExp.getLAndExps().get(i);
            BasicBlock nextBB = new BasicBlock("", curbb.getFunction());
            // 传参时应该注意： 前面几次and：成功true，失败next，最后一次and：成功true，失败false
            if (i != lorExp.getLAndExps().size() - 1) {
                visitLAndExp(lAndExp, trueBB, nextBB);
                nextBB.setName((++reg_num) + "");
                //BrInstr brInstr = new BrInstr(curbb, value, trueBB, nextBB); 这个相当于在最后一个and结束时候加了
                //curbb.addInstr(brInstr);
                //curbb.setTerminator(brInstr);
                curbb.getFunction().addBlock(curbb);
                curbb = nextBB;
            } else {
                visitLAndExp(lAndExp, trueBB, falseBB);
                //BrInstr brInstr = new BrInstr(curbb, value, trueBB, falseBB);
                //curbb.addInstr(brInstr);
                //curbb.setTerminator(brInstr);
                //TODO check是否需要在这里更换 curbb
            }

        }

    }

    private void visitCond(Cond cond, BasicBlock trueBB, BasicBlock falseBB) {
        // IRType needType = IntType.I1;
        LOrExp lorExp = cond.getLOrExp();
        visitLOrExp(lorExp, trueBB, falseBB);
        // IcmpInstr icmpInstr = new IcmpInstr(Operator.ne,"%" + (++reg_num), res, new ConstInt(0, needType), curbb);
        // curbb.addInstr(icmpInstr);

    }

    private void visitIfStmt(IfStmt ifStmt, Function func) {
        BasicBlock befBB = curbb; // backup
        BasicBlock trueBB = new BasicBlock("", func);
        BasicBlock nextBB;
        //Value cond ;
        if (ifStmt.getElseStmt() != null) {
            BasicBlock falseBB = new BasicBlock("", func); // TODO: need to reset name
            visitCond(ifStmt.getCond(), trueBB, falseBB);

            //BrInstr brInstr = new BrInstr(befBB, cond, trueBB, falseBB);
            //befBB.addInstr(brInstr);
            //befBB.setTerminator(brInstr);
            func.addBlock(curbb);
            curbb = trueBB;
            trueBB.setName((++reg_num) + "");
            visitStmt(ifStmt.getStmt(), func);// trueBB遍历
            nextBB = new BasicBlock("", func);
            BrInstr true2next = new BrInstr(curbb, nextBB);
            curbb.addInstr(true2next);
            curbb.setTerminator(true2next);
            func.addBlock(curbb);
            curbb = falseBB; // falseBB遍历
            falseBB.setName((++reg_num) + "");
            visitStmt(ifStmt.getElseStmt(), func);
            BrInstr false2next = new BrInstr(curbb, nextBB);
            curbb.addInstr(false2next);
            curbb.setTerminator(false2next);
            func.addBlock(curbb);
        } else {
            nextBB = new BasicBlock("", func);
            visitCond(ifStmt.getCond(), trueBB, nextBB);
            /*BrInstr brInstr = new BrInstr(befBB, cond, trueBB, nextBB);
            befBB.addInstr(brInstr);  从 befBB 到 trueBB or falseBB 在cond里生成
            befBB.setTerminator(brInstr);
            func.addBlock(befBB);*/
            func.addBlock(curbb);
            curbb = trueBB;
            trueBB.setName((++reg_num) + "");
            visitStmt(ifStmt.getStmt(), func);// trueBB遍历
            //BrInstr true2next = new BrInstr(trueBB, nextBB);
            BrInstr true2next = new BrInstr(curbb, nextBB);
            curbb.addInstr(true2next);
            curbb.setTerminator(true2next);
            func.addBlock(curbb);
        }
        curbb = nextBB; // nextBB继续
        nextBB.setName("" + (++reg_num));
    }

    private void visitPrintf(Printf printf) {
        // printf("aldfk %d %c");
        String str = printf.getStringConst();
        str = str.substring(1, str.length() - 1);
        int begin = 0;
        int exp_cnt = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '%' && i + 1 < str.length()) {
                if (str.charAt(i + 1) == 'd' || str.charAt(i + 1) == 'c') {
                    if (i > 0 && i > begin) {
                        String strLit = str.substring(begin, i);
                        StringLiteral stringLiteral = new StringLiteral(strLit);
                        module.addStringLiteral(stringLiteral);
                        GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), stringLiteral, new ConstInt(0), curbb);
                        curbb.addInstr(ptrInstr);
                        CallInstr callInstr = new CallInstr((Function) curSymTable.find("putstr"), curbb, new ArrayList<Value>(Collections.singletonList(ptrInstr)));
                        curbb.addInstr(callInstr);
                    }
                    begin = i + 2;
                    if (str.charAt(i + 1) == 'd') {
                        Value expvalue = visitExp(printf.getExps().get(exp_cnt), false);
                        CallInstr callInstr = new CallInstr((Function) curSymTable.find("putint"), curbb, new ArrayList<Value>(Collections.singletonList(expvalue)));
                        curbb.addInstr(callInstr);
                    } else {
                        Value expvalue = visitExp(printf.getExps().get(exp_cnt), false, IntType.I32);
                        CallInstr callInstr = new CallInstr((Function) curSymTable.find("putch"), curbb, new ArrayList<Value>(Collections.singletonList(expvalue)));
                        curbb.addInstr(callInstr);
                    }
                    exp_cnt++;
                }
            }
        }
        if (begin < str.length()) {
            String strLit = str.substring(begin);
            StringLiteral stringLiteral = new StringLiteral(strLit);
            module.addStringLiteral(stringLiteral);
            GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), stringLiteral, new ConstInt(0), curbb);
            curbb.addInstr(ptrInstr);
            CallInstr callInstr = new CallInstr((Function) curSymTable.find("putstr"), curbb, new ArrayList<Value>(Collections.singletonList(ptrInstr)));
            curbb.addInstr(callInstr);
        }

    }

}
