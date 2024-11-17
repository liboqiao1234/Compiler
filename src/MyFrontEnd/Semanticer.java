package MyFrontEnd;

import AST.Nodes.*;
import MyError.MyError;
import MyError.MyErrorType;
import Symbol.FuncSymbol;
import Symbol.Symbol;
import Symbol.SymbolTable;
import Symbol.SymbolType;
import Symbol.VarSymbol;

import java.util.ArrayList;

public class Semanticer {
    private CompUnit root;
    private SymbolTable<Symbol> table;
    private ArrayList<MyError> errors;
    private int curId = 1;
    private int maxId = 1;
    private SymbolTable<Symbol> curTable;
    private int hasReturn = 0; // 1: return;  2: return [something];
    private int returnLine = 0;
    
    public Semanticer(CompUnit root) {
        this.root = root;
        this.table = new SymbolTable<Symbol>(null, 1);
        this.errors = new ArrayList<>();
        curTable = table;
    }
    
    
    private SymbolType getType(BType bType, boolean isConst, boolean isArray, boolean isFunc) {
        if (isFunc) {
            if (bType.equals("int")) {
                return SymbolType.IntFunc;
            } else if (bType.equals("char")) {
                return SymbolType.CharFunc;
            } else {
                return SymbolType.VoidFunc;
            }
        }
        
        if (isConst) {
            if (bType.equals("int")) {
                if (isArray) return SymbolType.ConstIntArray;
                return SymbolType.ConstInt;
            } else if (bType.equals("char")) {
                if (isArray) return SymbolType.ConstCharArray;
                return SymbolType.ConstChar;
            }
        }
        
        if (bType.equals("int")) {
            if (isArray) return SymbolType.IntArray;
            return SymbolType.Int;
        } else if (bType.equals("char")) {
            if (isArray) return SymbolType.CharArray;
            return SymbolType.Char;
        }
        System.err.println("Unknown type in getting type!");
        return SymbolType.UNKNOWN;
    }
    
    private SymbolType getType(FuncType funcType) {
        if (funcType.equals("int")) {
            return SymbolType.IntFunc;
        } else if (funcType.equals("char")) {
            return SymbolType.CharFunc;
        } else {
            return SymbolType.VoidFunc;
        }
    }
    
    public void check() {
        for (Decl i : root.getDecls()) {
            checkDecl(i);
        }
        for (FuncDef i : root.getFuncDefs()) {
            checkFuncDef(i);
        }
        checkMainFuncDef(root.getMainFuncDef());
    }
    
    private void checkFuncDef(FuncDef funcDef) {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        String name = funcDef.getIdent().getContent();
        if (curTable.contains(name)) {
            errors.add(new MyError(funcDef.getIdent().getLineno(), MyErrorType.b));
            return;
        }
        FuncType type = funcDef.getFuncType();
        FuncSymbol funcSymbol = new FuncSymbol(name, funcDef.getLineno(), curId, getType(type));
        curTable.insert(name, funcSymbol);
        curTable = curTable.createSon(!type.equals("void"), ++maxId);
        curId = maxId;
        if (funcDef.getFuncFParams() != null) {
            checkFuncFParams(funcDef.getFuncFParams(), funcSymbol);
        }
        hasReturn = 0;
        checkBlock(funcDef.getBlock(), type.equals("void") ? 1 : 2, false, false, true);
        curTable = curTable.getFather();
    }
    
    private void checkFuncFParams(FuncFParams funcFParams, FuncSymbol funcSymbol) {
        for (FuncFParam i : funcFParams.getFuncFParams()) {
            checkFuncFParam(i, funcSymbol);
        }
    }
    
    private void checkBlock(Block block, int isReturn, boolean needCreate, boolean inCircle, boolean isFunc) {
        if (needCreate) {
            curTable = curTable.createSon(isReturn > 0, ++maxId);
            curId = maxId;
        }
        for (BlockItem i : block.getBlockItems()) {
            if (hasReturn != 0) {
                checkBlockItem(i, inCircle, isReturn);
            } else {
                hasReturn = checkBlockItem(i, inCircle, isReturn);
            }
        }
        if (needCreate) {
            curTable = curTable.getFather();
        }
        if (isFunc && isReturn == 2 && hasReturn == 0) { // if not function, must not be TRUE isReturn, safe.
            errors.add(new MyError(block.getRightBracket().getLineno(), MyErrorType.g));
            return;
        } /*else if (isFunc && isReturn != 2 && hasReturn == 2) { // if not function, hasReturn is wrong! safe.
            errors.add(new MyError(block.getLineno(), MyErrorType.f));
            return; // only at function block!!!!
        }*/
    }
    
    private int checkBlockItem(BlockItem blockItem, boolean inCircle, int isReturn) {
        
        if (blockItem.getDecl() != null) {
            checkDecl(blockItem.getDecl());
        } else {
            return checkStmt(blockItem.getStmt(), inCircle, isReturn);
        }
        return 0;
    }
    
    private int checkStmt(Stmt stmt, boolean inCircle, int isReturn) {
        if (stmt.getAssignStmt() != null) {
            checkAssignStmt(stmt.getAssignStmt());
        } else if (stmt.getReturn() != null) {
             int res = checkReturn(stmt.getReturn());
            if (res > isReturn) {
                errors.add(new MyError(stmt.getReturn().getLineno(), MyErrorType.f));
                return res;
            }
            return res;
        } else if (stmt.getIfStmt() != null) {
            checkIfStmt(stmt.getIfStmt(), inCircle, isReturn);
        } else if (stmt.getForStmts() != null) {
            checkForStmts(stmt.getForStmts(), isReturn);
        } else if ((stmt.getBreak() != null || stmt.getContinue() != null) && !inCircle) {
            if (stmt.getBreak() == null) {
                errors.add(new MyError(stmt.getContinue().getLineno(), MyErrorType.m));
            }else {
                errors.add(new MyError(stmt.getBreak().getLineno(), MyErrorType.m));
            }
        } else if (stmt.getBreak() != null || stmt.getContinue() != null) {
            ;
        } else if (stmt.getPrintf() != null) {
            checkPrintf(stmt.getPrintf());
        } else if (stmt.getInputInt() != null) {
            // lval not const!
            checkInputInt(stmt.getInputInt());
        } else if (stmt.getInputChar() != null) {
            // lval not const!
            checkInputChar(stmt.getInputChar());
        } else if (stmt.getBlock() != null) {
            checkBlock(stmt.getBlock(), isReturn, true, inCircle, false);
        } else if (stmt.getExp() != null) {
            checkExp(stmt.getExp());
        } else if (stmt.getSemicn() != null) {
            ;
        } else {
            System.err.println("checkStmt: unknown stmt"); // TODO: delete
        }
        
        return 0;
    }
    
    private void checkPrintf(Printf printf) {
        String strConst = printf.getStringConst();
        int size = 0, num = 0;
        if (printf.getExps() != null) {
            size = printf.getExps().size();
        }
        for (int i = 0; i < strConst.length(); i++) {
            if (strConst.charAt(i) == '%' && i + 1 < strConst.length() &&
                    (strConst.charAt(i + 1) == 'd' || strConst.charAt(i + 1) == 'c')) {
                num++;
            }
        }
        if (num != size) {
            errors.add(new MyError(printf.getLineno(), MyErrorType.l));
        }
        if (printf.getExps() != null) {
            for (Exp exp : printf.getExps()) {
                checkExp(exp);
            }
        }
    }
    
    private void checkInputInt(InputInt inputInt) {
        LVal lval = inputInt.getLVal();
        checkLVal(lval);
        if (checkConstAssign(lval)) return;
    }
    
    private void checkInputChar(InputChar inputChar) {
        LVal lval = inputChar.getLVal();
        checkLVal(lval);
        if (checkConstAssign(lval)) return;
        
    }
    
    private void checkMainFuncDef(MainFuncDef mainFuncDef) {
        curTable = curTable.createSon(true, ++maxId);
        curId = maxId;
        hasReturn = 0;
        checkBlock(mainFuncDef.getBlock(), 2, false, false, true);
        curTable = curTable.getFather();
    }
    
    private boolean checkConstAssign(LVal lval) {
        if (curTable.find(lval.getIdent().getContent()) == null) {
            return true;
        }
        if (!(curTable.find(lval.getIdent().getContent()) instanceof VarSymbol)) {
            System.err.println("Error in LVal assignment! Wrong Symbol type.");
        }
        VarSymbol lvalSym = (VarSymbol) curTable.find(lval.getIdent().getContent());
        if (lvalSym.isConst()) {
            errors.add(new MyError(lval.getIdent().getLineno(), MyErrorType.h));
            return true;
        }
        return false;
    }
    
    private void checkIfStmt(IfStmt ifStmt, boolean inCircle, int isReturn) {
        Cond cond = ifStmt.getCond();
        checkCond(cond);
        checkStmt(ifStmt.getStmt(), inCircle, isReturn);
        if (ifStmt.getElseStmt() != null) {
            checkStmt(ifStmt.getElseStmt(), inCircle, isReturn);
        }
    }
    
    private void checkForStmts(ForStmts forStmts, int isReturn) {
        if (forStmts.getForStmt1() != null) {
            checkForStmt(forStmts.getForStmt1());
        }
        if (forStmts.getCond() != null) {
            checkCond(forStmts.getCond());
        }
        if (forStmts.getForStmt2() != null) {
            checkForStmt(forStmts.getForStmt2());
        }
        
        checkStmt(forStmts.getStmt(), true, isReturn);
    }
    
    private void checkForStmt(ForStmt forStmt) {
        LVal lval = forStmt.getlVal();
        checkLVal(lval);
        if (checkConstAssign(lval)) return;
        checkExp(forStmt.getExp());
    }
    
    private void checkCond(Cond cond) {
        checkLOrExp(cond.getLOrExp());
    }
    
    private void checkLOrExp(LOrExp lorExp) {
        for (LAndExp i : lorExp.getLAndExps()) {
            checkLAndExp(i);
        }
    }
    
    private void checkLAndExp(LAndExp landExp) {
        for (EqExp i : landExp.getEqExps()) {
            checkEqExp(i);
        }
    }
    
    private void checkEqExp(EqExp eqExp) {
        for (RelExp i : eqExp.getRelExps()) {
            checkRelExp(i);
        }
    }
    
    private void checkRelExp(RelExp relExp) {
        for (AddExp i : relExp.getAddExps()) {
            checkAddExp(i);
        }
    }
    
    private int checkReturn(Return returnStmt) {
        if (returnStmt.getExp() != null) {
            checkExp(returnStmt.getExp());
            return 2;
        }
        return 1;
    }
    
    private void checkAssignStmt(AssignStmt assignStmt) {
        LVal lval = assignStmt.getlVal();
        checkLVal(lval);
        if (checkConstAssign(lval)) return;
        
        
        checkExp(assignStmt.getExp());
    }
    
    private void checkFuncFParam(FuncFParam funcFParam, FuncSymbol funcSymbol) {
        // FuncFParam → BType Ident ['[' ']']
        BType type = funcFParam.getBType();
        
        String name = funcFParam.getIdent().getContent();
        if (curTable.contains(name)) {
            errors.add(new MyError(funcFParam.getIdent().getLineno(), MyErrorType.b));
            return;
        }
        boolean isArray = funcFParam.getLeftBracket() != null;
        VarSymbol para = new VarSymbol(name, funcFParam.getLineno(), curId,
                getType(type, false, isArray, false));
        curTable.insert(name, para);
        funcSymbol.addParam(para);
    }
    
    private void checkDecl(Decl decl) {
        //声明 Decl → ConstDecl | VarDecl
        if (decl.getConstDecl() != null) {
            checkConstDecl(decl.getConstDecl());
        } else {
            checkVarDecl(decl.getVarDecl());
        }
    }
    
    private void checkVarDecl(VarDecl varDecl) {
        // VarDecl → BType VarDef { ',' VarDef } ';'
        BType type = varDecl.getBType();
        for (VarDef i : varDecl.getVarDefs()) {
            checkVarDef(i, type);
        }
    }
    
    private void checkVarDef(VarDef varDef, BType type) {
        String name = varDef.getIdent().getContent();
        if (curTable.contains(name)) {
            errors.add(new MyError(varDef.getIdent().getLineno(), MyErrorType.b));
            return; //TODO: only one error in one line?
        }
        if (varDef.getConstExp() != null) {
            checkConstExp(varDef.getConstExp());
        }
        curTable.insert(name, new VarSymbol(name, varDef.getLineno(), curId,
                getType(type, false, varDef.getConstExp() != null, false)));
        if (varDef.getInitVal() != null) {
            checkInitVal(varDef.getInitVal());
        }
    }
    
    private void checkInitVal(InitVal initVal) {
        if (initVal.getExp() != null) {
            checkExp(initVal.getExp());
        } else if (initVal.getExpList() != null) {
            for (Exp i : initVal.getExpList()) {
                checkExp(i);
            }
        } else {
            // string : nothing to check.
        }
    }
    
    private void checkConstDecl(ConstDecl constDecl) {
        // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        BType type = constDecl.getBType(); // push the symbol in each constDef, with type & const as para   .
        for (ConstDef i : constDecl.getConstDefs()) {
            checkConstDef(i, type);
        }
    }
    
    private void checkConstDef(ConstDef constDef, BType type) {
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        String name = constDef.getIdent().getContent();
        if (curTable.contains(name)) {
            errors.add(new MyError(constDef.getIdent().getLineno(), MyErrorType.b));
            return; //TODO: only one error in one line?
        }
        SymbolType type1 = getType(type, true, constDef.getConstExp() != null, false); // TODO: check "isArray"
        curTable.insert(name, new VarSymbol(name, constDef.getLineno(), curId, type1));
        if (constDef.getConstExp() != null) {
            checkConstExp(constDef.getConstExp());
        }
        checkConstInitVal(constDef.getConstInitVal());
    }
    
    private void checkConstExp(ConstExp constExp) {
        // ConstExp → AddExp
        checkAddExp(constExp.getAddExp());
    }
    
    private void checkExp(Exp exp) {
        checkAddExp(exp.getAddExp());
    }
    
    private void checkAddExp(AddExp addExp) {
        for (MulExp i : addExp.getMulExps()) {
            checkMulExp(i);
        }
    }
    
    private void checkMulExp(MulExp mulExp) {
        for (UnaryExp i : mulExp.getUnaryExps()) {
            checkUnaryExp(i);
        }
    }
    
    private void checkUnaryExp(UnaryExp unaryExp) {
        // UnaryExp → {UnaryOp}     PrimaryExp | Ident '(' [FuncRParams] ')'
        if (unaryExp.getPrimaryExp() != null) {
            checkPrimaryExp(unaryExp.getPrimaryExp());
        } else if (unaryExp.getIdent() != null) { // function call
            if (table.find(unaryExp.getIdent().getContent()) == null) {
                errors.add(new MyError(unaryExp.getIdent().getLineno(), MyErrorType.c));
                return;
            }
            FuncSymbol func = (FuncSymbol) table.find(unaryExp.getIdent().getContent()); // TODO: dangerous
            FuncRParams funcRParams = unaryExp.getFuncRParams();
            if (funcRParams != null) {
                if (funcRParams.getExps().size() != func.getParams().size()) {
                    errors.add(new MyError(unaryExp.getIdent().getLineno(), MyErrorType.d)); // TODO: check lino definition.
                    return;
                }
                ArrayList<Exp> exps = funcRParams.getExps();
                for (int i = 0; i < exps.size(); i++) {
                    checkExp(exps.get(i));
                    // type&num check
                    SymbolType type = getFuncRParamType(exps.get(i)); // function overload.
                    if (typeIsArray(type) != typeIsArray(func.getParams().get(i).getDataType())
                            || typeIsArray(type) && (type != func.getParams().get(i).getDataType())) {
                        errors.add(new MyError(unaryExp.getIdent().getLineno(), MyErrorType.e));
                        return;
                    }
                    // TODO: check here
                }
            }
        } else {
            System.err.println("Error in checkUnaryExp"); //TODO: delete
        }
    }
    
    private void checkPrimaryExp(PrimaryExp primaryExp) {
        // PrimaryExp → '(' Exp ')' | LVal | Number | char
        if (primaryExp.getExp() != null) {
            checkExp(primaryExp.getExp());
        } else if (primaryExp.getLVal() != null) {
            checkLVal(primaryExp.getLVal());
        } else if (primaryExp.getNumber() != null) {
            // do nothing
        } else if (primaryExp.getCharacter() != null) {
            // do nothing
        } else {
            System.err.println("Error in checkPrimaryExp"); //TODO: delete
        }
    }
    
    private void checkLVal(LVal lVal) {
        if (curTable.find(lVal.getIdent().getContent()) == null) {
            errors.add(new MyError(lVal.getIdent().getLineno(), MyErrorType.c));
            return;
        }
        if (lVal.getExp() != null) {
            checkExp(lVal.getExp());
        }
    }
    
    private SymbolType func2retType(SymbolType funcType) {
        switch (funcType) {
            case IntFunc:
                return SymbolType.Int;
            case CharFunc:
                return SymbolType.Char;
            default:
                return null;//TODO: what if the FuncRParam (a function call's return) is void function????
        }
    }
    
    private boolean typeIsFunc(SymbolType type) {
        return type == SymbolType.IntFunc || type == SymbolType.CharFunc || type == SymbolType.VoidFunc;
    }
    
    private boolean typeIsArray(SymbolType type) {
        return type == SymbolType.IntArray || type == SymbolType.CharArray
                || type == SymbolType.ConstIntArray || type == SymbolType.ConstCharArray;
    }
    
    
    private SymbolType getFuncRParamType(Exp exp) {
        // TODO: consider int s[10],  param like : 4 + s, 4 + s[1]
        // Temporary: consider s + 4 as 1-d array
        AddExp addExp = exp.getAddExp();
        boolean isArray = false;
        for (MulExp i : addExp.getMulExps()) {
            for (UnaryExp j : i.getUnaryExps()) {
                if (j.getPrimaryExp() != null) {
                    SymbolType res = getFuncRParamType(j.getPrimaryExp());
                    if (typeIsArray(res)) {
                        return res;
                        // isArray = true;
                        // break;
                    }
                } else if (j.getIdent() != null) {
                    // only 0-dimension;
                    continue;
                }
                /* if (isArray) {
                    break;
                }*/
            }
        }
        return SymbolType.Int; // TODO: check whether it is eligible to regard char as int. (2)
    }
    
    private SymbolType getFuncRParamType(PrimaryExp primaryExp) {
        if (primaryExp.getNumber() != null) {
            return SymbolType.Int;
        } else if (primaryExp.getCharacter() != null) {
            return SymbolType.Char;
        } else if (primaryExp.getLVal() != null) {
            SymbolType type =((Symbol) curTable.find(primaryExp.getLVal().getIdent().getContent())).getDataType();
            if (primaryExp.getLVal().getExp() == null && typeIsArray(type)) {
                return type;
            } else {
                return SymbolType.Int; // TODO: check whether it is eligible to regard char as int.
            }
            
        }
        return getFuncRParamType(primaryExp.getExp());
    }
    
    
    private void checkFuncRParams(FuncRParams funcRParams) {
        for (Exp i : funcRParams.getExps()) {
            checkExp(i);
        }
    }
    
    private void checkConstInitVal(ConstInitVal constInitVal) {
        if (constInitVal.getConstExps() != null) {
            for (ConstExp i : constInitVal.getConstExps()) {
                checkConstExp(i);
            }
        } else if (constInitVal.getConstExp() != null) {
            checkConstExp(constInitVal.getConstExp());
        } else {
            // stringConst nothing;
        }
    }
    
    public SymbolTable<Symbol> getSymbolTable() {
        return table;
    }
    
    public ArrayList<MyError> getErrors() {
        return errors;
    }
    
}
