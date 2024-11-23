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
import java.util.Objects;

public class IRgenerator {
    private static IRgenerator instance = new IRgenerator();
    private int reg_num = -1;
    private LLVMModule module = new LLVMModule();
    private SymbolTable<Value> curSymTable = new SymbolTable<Value>(null, 1);
    private SymbolTable<ConstValue> constSymTable = new SymbolTable<ConstValue>(null, 1);

    public static IRgenerator getInstance() {
        return instance;
    }

    private Value testValueNeedToBeDeleted = new Value("ERROR!", new IRType());

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

    private Value Convert(Value value, IRType type, BasicBlock bb) {
        if (value == null) {
            return null;
        }
        if (value.getType().equals(type)) {
            return value;
        }
        // func call : para is array[]
        if (type.isPointer() && value.getType().isPointer() && ((PointerType) value.getType()).getPointeeType().isArray()) {
            System.out.println("func call : para is array[]");
            GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), value, new ConstInt(0), bb);
            bb.addInstr(ptrInstr);
            return ptrInstr;
        }

        if (value.getType() == IntType.I32) {
            // then type must be I8
            TruncInstr truncInstr = new TruncInstr("%" + (++reg_num), value, new Value("", type), bb);
            bb.addInstr(truncInstr);
            return truncInstr;
        } else {
            // then type must be I32
            ZextInstr zextInstr = new ZextInstr("%" + (++reg_num), value, new Value("", type), bb);
            bb.addInstr(zextInstr);
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
            visitDecl(decl, null);
        }

        for (FuncDef funcDef : root.getFuncDefs()) {
            visitFuncDef(funcDef);
        }
        visitMainFuncDef(root.getMainFuncDef());
    }

    private void visitDecl(Decl decl, BasicBlock bb) {
        // ConstDecl | VarDecl;
        if (decl.getConstDecl() != null) {
            visitConstDecl(decl.getConstDecl(), bb);
        } else {
            visitVarDecl(decl.getVarDecl(), bb);
        }
    }

    private void visitVarDecl(VarDecl varDecl, BasicBlock bb) {
        //  VarDecl → BType VarDef { ',' VarDef } ';'
        boolean isGlobal = bb == null; // 如果bb是空，说明不在块内，全局；否则一定为局部。
        IRType type = BType2IRType(varDecl.getBType());
        for (VarDef varDef : varDecl.getVarDefs()) {
            visitVarDef(varDef, type, isGlobal, bb);
        }
    }

    private void visitVarDef(VarDef varDef, IRType type, boolean isGlobal, BasicBlock bb) {
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
                    ArrayList<Value> initVals = visitInitVal(varDef.getInitVal(), null, type);
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
                    ConstValue initVal = (ConstValue) visitInitVal(varDef.getInitVal(), null, type).get(0);
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
                AllocaInstr alloca = new AllocaInstr("%" + (++reg_num), arrayType, bb);//得到一个指向数组的指针
                bb.addInstr(alloca);
                curSymTable.insert(name, alloca);

                if (varDef.getInitVal() != null) {
                    // 有初值，在这里赋值
                    ArrayList<Value> initVals = visitInitVal(varDef.getInitVal(), bb, type);
                    for (int i = 0; i < initVals.size(); i++) {
                        GetelementptrInstr ptr = new GetelementptrInstr("%" + (++reg_num), alloca, new ConstInt(i), bb);
                        StoreInstr storeInstr = new StoreInstr(initVals.get(i), ptr, bb);
                        bb.addInstr(ptr);
                        bb.addInstr(storeInstr);
                    }
//                    dflaskj;
                }
            } else {
                // 局部变量
                AllocaInstr alloca = new AllocaInstr("%" + (++reg_num), type, bb);
                bb.addInstr(alloca);
                curSymTable.insert(name, alloca);
                if (varDef.getInitVal() != null) {
                    Value initVal = visitInitVal(varDef.getInitVal(), bb, type).get(0);
                    StoreInstr storeInstr = new StoreInstr(initVal, alloca, bb);
                    bb.addInstr(storeInstr);
                }
            }
        }

    }

    private ArrayList<Value> visitInitVal(InitVal initVal, BasicBlock bb) {
        return visitInitVal(initVal, bb, IntType.I32);
    }

    private ArrayList<Value> visitInitVal(InitVal initVal, BasicBlock bb, IRType needType) {
        // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
        // 返回值为最终计算好初值的%i 的list，如果是单个变量，则取0号位
        ArrayList<Value> initVals = new ArrayList<>();
        boolean isConst = bb == null;

        if (initVal.getExp() != null) {
            // single variable
            initVals.add(visitExp(initVal.getExp(), isConst, bb, needType));
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
                initVals.add(visitExp(exp, isConst, bb, needType));
            }
        }
        return initVals;
    }

    private void visitConstDecl(ConstDecl constDecl, BasicBlock bb) {
        // 'const' BType ConstDef { ',' ConstDef } ';'
        BType type = constDecl.getBType();
        for (ConstDef constDef : constDecl.getConstDefs()) {
            visitConstDef(constDef, type, bb);
        }
    }

    private void visitConstDef(ConstDef constDef, BType type, BasicBlock bb) {
        //  ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal, 变量 or 数组
        boolean isGlobal = bb == null;
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
                AllocaInstr allocaInst = new AllocaInstr("%" + (++reg_num), arrayType, bb);
                // curSymTable.insert(allocaInst.getName(), allocaInst);
                bb.addInstr(allocaInst);
                ArrayList<ConstValue> initVals = ((ConstArray) initVal).getElements();
                assert elementType.equals(initVals.get(0).getType());
                //TODO: C&D
                for (int i = 0; i < initVals.size(); i++) {
                    GetelementptrInstr ptr = new GetelementptrInstr("%" + (++reg_num), allocaInst, new ConstInt(i, elementType), bb);
                    StoreInstr storeInstr = new StoreInstr(initVals.get(i), ptr, bb);
                    bb.addInstr(ptr);
                    bb.addInstr(storeInstr);
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
                AllocaInstr allocaInst = new AllocaInstr("%" + (++reg_num), BType2IRType(type), bb);
                StoreInstr storeInst = new StoreInstr(initVal, allocaInst, bb);
                bb.addInstr(allocaInst);
                bb.addInstr(storeInst);
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
        return ((ConstInt) visitAddExp(constExp.getAddExp(), true, null, needType)).getValue();
    }

    private Value visitAddExp(AddExp addExp, boolean isConst, BasicBlock bb, IRType needType) {
        // 如果是const，返回ConstInt代表数值，否则返回value, TODO check
        // AddExp → MulExp | AddExp ('+' | '−') MulExp
        if (isConst) {
            int res = 0;
            ArrayList<MulExp> mulExps = addExp.getMulExps();
            ArrayList<Token> ops = addExp.getAddOps();
            for (int i = 0; i < mulExps.size(); i++) {
                if (i == 0) {
                    res = ((ConstInt) visitMulExp(mulExps.get(i), true, bb)).getValue();
                } else {
                    res = calc(ops.get(i - 1), res, ((ConstInt) visitMulExp(mulExps.get(i), true, bb)).getValue());
                }
            }
            return new ConstInt(res, needType);
        } else {
            Value res = visitMulExp(addExp.getMulExps().get(0), false, bb, needType);
            for (int i = 1; i < addExp.getMulExps().size(); i++) {
                Value mulExpVal = visitMulExp(addExp.getMulExps().get(i), false, bb, needType);
                if (needType != IntType.I32) {
                    res = Convert(res, IntType.I32, bb);
                    mulExpVal = Convert(mulExpVal, IntType.I32, bb);
                }
                CalcInstr calcInstr = new CalcInstr(Op2Op(addExp.getAddOps().get(i - 1).getContent()), "%" + (++reg_num), res, mulExpVal, bb);
                bb.addInstr(calcInstr);
                res = calcInstr;
            }
            return Convert(res, needType, bb);
        }
    }

    private Value visitMulExp(MulExp mulExp, boolean isConst, BasicBlock bb) {
        return visitMulExp(mulExp, isConst, bb, IntType.I32);
    }

    private Value visitMulExp(MulExp mulExp, boolean isConst, BasicBlock bb, IRType needType) {
        // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        ArrayList<UnaryExp> unaryExps = mulExp.getUnaryExps();
        ArrayList<Token> ops = mulExp.getOperators();
        if (isConst) {
            int res = 0;

            for (int i = 0; i < unaryExps.size(); i++) {
                if (i == 0) {
                    res = ((ConstInt) visitUnaryExp(unaryExps.get(i), true, bb)).getValue();
                } else {
                    res = calc(ops.get(i - 1), res, ((ConstInt) visitUnaryExp(unaryExps.get(i), true, bb)).getValue());
                }
            }
            return new ConstInt(res, needType);
        } else {
            Value res = visitUnaryExp(unaryExps.get(0), false, bb, needType);
            for (int i = 1; i < unaryExps.size(); i++) {
                Value unaryExpVal = visitUnaryExp(unaryExps.get(i), false, bb, needType);
                if (needType != IntType.I32) {
                    res = Convert(res, IntType.I32, bb);
                    unaryExpVal = Convert(unaryExpVal, IntType.I32, bb);
                }
                CalcInstr calcInstr = new CalcInstr(Op2Op(ops.get(i - 1).getContent()), "%" + (++reg_num), res, unaryExpVal, bb);
                bb.addInstr(calcInstr);
                res = calcInstr;
            }
            return Convert(res, needType, bb);
        }
    }

    private Value visitUnaryExp(UnaryExp unaryExp, boolean isConst, BasicBlock bb) {
        return visitUnaryExp(unaryExp, isConst, bb, IntType.I32);
    }

    private Value visitUnaryExp(UnaryExp unaryExp, boolean isConst, BasicBlock bb, IRType needType) {
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
                res = ((ConstInt) visitPrimaryExp(unaryExp.getPrimaryExp(), true, bb)).getValue();
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
                res = visitPrimaryExp(unaryExp.getPrimaryExp(), false, bb, needType);
//                if (res.getType() == IntType.I8) {
//                    res = new ZextInstr("%" + (++reg_num), res, new Value("",IntType.I32), bb);
//                    bb.addInstr((ZextInstr) res);
//                }
            } else if (unaryExp.getIdent() != null) {
                // function call; func call
                Function func = (Function) curSymTable.find(unaryExp.getIdent().getContent());
                IRType retType = ((FunctionType) func.getType()).getRetType();
                if (unaryExp.getFuncRParams() != null) {
                    ArrayList<Value> params = new ArrayList<>();
                    for (int i = 0; i < unaryExp.getFuncRParams().getExps().size(); i++) {
                        Exp funcRParam = unaryExp.getFuncRParams().getExps().get(i);
                        params.add(visitExp(funcRParam, false, bb, func.getParaList().get(i)));
                    }
                    if (retType == IntType.VOID) {
                        CallInstr callInstr = new CallInstr(func, bb, params); // Void没返回值，直接return
                        bb.addInstr(callInstr);
                        return null;
                    } else {
                        CallInstr callInstr = new CallInstr("%" + (++reg_num), func, bb, params);
                        bb.addInstr(callInstr);
                        res = callInstr;
                    }
                } else {
                    if (retType == IntType.VOID) {
                        CallInstr callInstr = new CallInstr(func, bb, null); // Void没返回值，直接return
                        bb.addInstr(callInstr);
                        return null;
                    } else {
                        res = new CallInstr("%" + (++reg_num), func, bb, null);
                        bb.addInstr((CallInstr) res);
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
                    IcmpInstr icmpInstr = new IcmpInstr(Operator.ne, "%" + (++reg_num), res, new ConstInt(1, IntType.I1), bb);
                    bb.addInstr(icmpInstr);
                    return icmpInstr;
                } else {
                    IcmpInstr icmpInstr = new IcmpInstr(Operator.ne, "%" + (++reg_num), res, new ConstInt(0, IntType.I1), bb);
                    bb.addInstr(icmpInstr);
                    return icmpInstr;
                }
            } else {
                if (minus == 1) {
                    return Convert(res, needType, bb);
                }
                if (needType != IntType.I32) {
                    res = Convert(res, IntType.I32, bb);
                }
                CalcInstr calcInstr = new CalcInstr(Operator.sub, "%" + (++reg_num), new ConstInt(0, IntType.I32), res, bb);
                bb.addInstr(calcInstr);
                return Convert(calcInstr, needType, bb);
            }
        }
    }

    private Value visitPrimaryExp(PrimaryExp primaryExp, boolean isConst, BasicBlock bb) {
        return visitPrimaryExp(primaryExp, isConst, bb, IntType.I32);
        //停在这里，因为char函数会返回i8，没必要转到32再转回来，因此设置needType，遇到i32计算再放大。
    }

    private Value visitPrimaryExp(PrimaryExp primaryExp, boolean isConst, BasicBlock bb, IRType needType) {
        // PrimaryExp → '(' Exp ')' | LVal | Number | Character
        if (isConst) {
            int res = 0;
            if (primaryExp.getExp() != null) {
                res = ((ConstInt) visitExp(primaryExp.getExp(), true, bb)).getValue();
            } else if (primaryExp.getLVal() != null) {
                //理论上不包括数组元素
                res = ((ConstInt) visitLVal(primaryExp.getLVal(), true, bb)).getValue();
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
                return visitLVal(primaryExp.getLVal(), false, bb, needType);
            } else if (primaryExp.getExp() != null) {
                return visitExp(primaryExp.getExp(), false, bb, needType);
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

    private Value visitExp(Exp exp, boolean isConst, BasicBlock bb) {
        return visitExp(exp, isConst, bb, IntType.I32);
    }

    private Value visitExp(Exp exp, boolean isConst, BasicBlock bb, IRType needType) {
        return visitAddExp(exp.getAddExp(), isConst, bb, needType);
    }

    private Value visitLVal(LVal lVal, boolean isConst, BasicBlock bb) {
        return visitLVal(lVal, isConst, bb, IntType.I32);
    }

    private Value visitLVal(LVal lVal, boolean isConst, BasicBlock bb, IRType needType) {
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
                    Value expval = visitExp(lVal.getExp(), false, bb);
                    ptrInstr = new GetelementptrInstr("%" + (++reg_num), res, expval, bb);
                    bb.addInstr(ptrInstr);
                } else if (((PointerType) res.getType()).getPointeeType() instanceof PointerType) {
                    // i32**;
                    LoadInstr loadInstr1 = new LoadInstr("%" + (++reg_num), res, bb);
                    bb.addInstr(loadInstr1);
                    Value expval = visitExp(lVal.getExp(), false, bb);
                    ptrInstr = new GetelementptrInstr("%" + (++reg_num), loadInstr1, expval, bb, true);
                    bb.addInstr(ptrInstr);
                } else {
                    System.err.println("error in visitLVal !");
                    ptrInstr = null;
                }
//                LoadInstr loadInstr1 = new LoadInstr("%" + (++reg_num), res, bb);
//                bb.addInstr(loadInstr1);
//                Value expval = visitExp(lVal.getExp(), false, bb);
//                GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), loadInstr1, expval, bb);
//                bb.addInstr(ptrInstr);
                // TODO check
                LoadInstr loadInstr = new LoadInstr("%" + (++reg_num), ptrInstr, bb);
                bb.addInstr(loadInstr);
                return Convert(loadInstr, needType, bb);
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
                    GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), res, new ConstInt(0), bb);
                    bb.addInstr(ptrInstr);
                    return Convert(ptrInstr, needType, bb);
                } else {
                    LoadInstr loadInstr = new LoadInstr("%" + (++reg_num), res, bb);
                    bb.addInstr(loadInstr);
                    return Convert(loadInstr, needType, bb);
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
        visitBlock(mainFuncDef.getBlock(), func, bb, false);
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
                } /*else {
                    // var
                    AllocaInstr para = new AllocaInstr("%"+reg_num, tmpType, bb);
                }*/
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
        curSymTable.insert(funcName, func);
        if (!paramTypes.isEmpty()) {
            for (int i = 0; i < paramTypes.size(); i++) {
                IRType paramType = paramTypes.get(i);
                String paraName = "%" + i;
                Argument paraArgument = new Argument(paraName, paramType);

                AllocaInstr para = new AllocaInstr("%" + (++reg_num), paramType, bb);
                curSymTable.insert(funcDef.getFuncFParams().getFuncFParams().get(i).getIdent().getContent(), para);
                bb.addInstr(para);
                StoreInstr store = new StoreInstr(paraArgument, para, bb);
                bb.addInstr(store);
            }
        }

        visitBlock(funcDef.getBlock(), func, bb, false);
        if (bb.getTerminator() == null) {
            ReturnInstr retInstr = new ReturnInstr(bb);
            bb.addInstr(retInstr);
        }
        module.addFunction(func);
        exitSonTable();
    }

    private void visitBlock(Block block, Function func, BasicBlock bb, boolean needCreate) {
        if (needCreate) {
            createSonTable();
        }
        for (BlockItem blockItem : block.getBlockItems()) {
            if (blockItem.getDecl() != null) {
                visitDecl(blockItem.getDecl(), bb);
            } else {
                visitStmt(blockItem.getStmt(), func, bb);
            }
        }
        if (needCreate) {
            exitSonTable();
        }
    }

    private Value getLeftLval(LVal lVal, BasicBlock bb) {
        if (lVal.getExp() == null) {
            return curSymTable.find(lVal.getIdent().getContent());
        } else {
            Value index = visitExp(lVal.getExp(), false, bb);
            Value res = curSymTable.find(lVal.getIdent().getContent());
            if (((PointerType) res.getType()).getPointeeType() instanceof PointerType) {
                // i32**; means para[] in func
                LoadInstr loadInstr1 = new LoadInstr("%" + (++reg_num), res, bb);
                bb.addInstr(loadInstr1);
                res = loadInstr1;
                GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), res, index, bb, true);
                bb.addInstr(ptrInstr);
                return ptrInstr;
            }
            GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), res, index, bb);
            bb.addInstr(ptrInstr);
            return ptrInstr;
        }
    }

    private void visitStmt(Stmt stmt, Function func, BasicBlock bb) {
        if (stmt.getAssignStmt() != null) {
            Value left = getLeftLval(stmt.getAssignStmt().getlVal(), bb);
            PointerType leftType = (PointerType) left.getType();
            IRType needType = leftType.getPointeeType();
            Value exp = visitExp(stmt.getAssignStmt().getExp(), false, bb, needType);
            StoreInstr storeInstr = new StoreInstr(exp, left, bb);
            bb.addInstr(storeInstr);
        } else if (stmt.getBlock() != null) {
            visitBlock(stmt.getBlock(), func, bb, true);
        } else if (stmt.getIfStmt() != null) {

        } else if (stmt.getForStmts() != null) {

        } else if (stmt.getBreak() != null) {

        } else if (stmt.getContinue() != null) {

        } else if (stmt.getReturn() != null) {
            if (stmt.getReturn().getExp() != null) {
                // return [exp];
                Value ret = visitExp(stmt.getReturn().getExp(), false, bb, ((FunctionType) func.getType()).getRetType()); // i32
                /*if (ret.getType() != ((FunctionType)func.getType()).getRetType()) {
                    TruncInstr truncInstr = new TruncInstr("%" + (++reg_num), ret, new Value("",IntType.I8), bb);
                    bb.addInstr(truncInstr);
                    ret = truncInstr;
                }*/
                ReturnInstr retInstr = new ReturnInstr(ret, bb);
                bb.addInstr(retInstr);
                bb.setTerminator(retInstr);
            } else {
                ReturnInstr retInstr = new ReturnInstr(bb);
                bb.addInstr(retInstr);
                bb.setTerminator(retInstr);

                // return;
            }
        } else if (stmt.getInputChar() != null) {
            CallInstr callInstr = new CallInstr("%" + (++reg_num), (Function) curSymTable.find("getchar"), bb, null);
            bb.addInstr(callInstr);
            TruncInstr truncInstr = new TruncInstr("%" + (++reg_num), callInstr, new Value("", IntType.I8), bb);
            bb.addInstr(truncInstr);
            Value lval = getLeftLval(stmt.getInputChar().getLVal(), bb);
            StoreInstr storeInstr = new StoreInstr(truncInstr, lval, bb);
            bb.addInstr(storeInstr);
        } else if (stmt.getInputInt() != null) {
            CallInstr callInstr = new CallInstr("%" + (++reg_num), (Function) curSymTable.find("getint"), bb, null);
            bb.addInstr(callInstr);
            Value lval = getLeftLval(stmt.getInputInt().getLVal(), bb);
            StoreInstr storeInstr = new StoreInstr(callInstr, lval, bb);
            bb.addInstr(storeInstr);
        } else if (stmt.getPrintf() != null) {
            visitPrintf(stmt.getPrintf(), bb);
        } else if (stmt.getExp() != null) {
            visitExp(stmt.getExp(), false, bb);
        } else if (stmt.getSemicn() != null) {
            // should be the else one
            ;
        } else {
            System.out.println("stmt error");
        }
    }

    private void visitPrintf(Printf printf, BasicBlock bb) {
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
                        GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), stringLiteral, new ConstInt(0), bb);
                        bb.addInstr(ptrInstr);
                        CallInstr callInstr = new CallInstr((Function) curSymTable.find("putstr"), bb, new ArrayList<Value>(Collections.singletonList(ptrInstr)));
                        bb.addInstr(callInstr);
                    }
                    begin = i + 2;
                    if (str.charAt(i + 1) == 'd') {
                        Value expvalue = visitExp(printf.getExps().get(exp_cnt), false, bb);
                        CallInstr callInstr = new CallInstr((Function) curSymTable.find("putint"), bb, new ArrayList<Value>(Collections.singletonList(expvalue)));
                        bb.addInstr(callInstr);
                    } else {
                        Value expvalue = visitExp(printf.getExps().get(exp_cnt), false, bb, IntType.I32);
//                        ZextInstr zextInstr = new ZextInstr("%" + (++reg_num), expvalue, new Value("", IntType.I32), bb);
//                        bb.addInstr(zextInstr);
                        CallInstr callInstr = new CallInstr((Function) curSymTable.find("putch"), bb, new ArrayList<Value>(Collections.singletonList(expvalue)));
                        bb.addInstr(callInstr);
                    }
                    exp_cnt++;
                }
            }
        }
        if (begin < str.length()) {
            String strLit = str.substring(begin);
            StringLiteral stringLiteral = new StringLiteral(strLit);
            module.addStringLiteral(stringLiteral);
            GetelementptrInstr ptrInstr = new GetelementptrInstr("%" + (++reg_num), stringLiteral, new ConstInt(0), bb);
            bb.addInstr(ptrInstr);
            CallInstr callInstr = new CallInstr((Function) curSymTable.find("putstr"), bb, new ArrayList<Value>(Collections.singletonList(ptrInstr)));
            bb.addInstr(callInstr);
        }

    }

}
