package MyFrontEnd;

import AST.Nodes.AddExp;
import AST.Nodes.AssignStmt;
import AST.Nodes.BType;
import AST.Nodes.Block;
import AST.Nodes.BlockItem;
import AST.Nodes.Break;
import AST.Nodes.Character;
import AST.Nodes.CompUnit;
import AST.Nodes.Cond;
import AST.Nodes.ConstDecl;
import AST.Nodes.ConstDef;
import AST.Nodes.ConstExp;
import AST.Nodes.ConstInitVal;
import AST.Nodes.Continue;
import AST.Nodes.Decl;
import AST.Nodes.EqExp;
import AST.Nodes.Exp;
import AST.Nodes.ForStmt;
import AST.Nodes.ForStmts;
import AST.Nodes.FuncDef;
import AST.Nodes.FuncFParam;
import AST.Nodes.FuncFParams;
import AST.Nodes.FuncRParams;
import AST.Nodes.FuncType;
import AST.Nodes.IfStmt;
import AST.Nodes.InitVal;
import AST.Nodes.InputChar;
import AST.Nodes.InputInt;
import AST.Nodes.LAndExp;
import AST.Nodes.LOrExp;
import AST.Nodes.LVal;
import AST.Nodes.MainFuncDef;
import AST.Nodes.MulExp;
import AST.Nodes.Number;
import AST.Nodes.PrimaryExp;
import AST.Nodes.Printf;
import AST.Nodes.RelExp;
import AST.Nodes.Return;
import AST.Nodes.Stmt;
import AST.Nodes.UnaryExp;
import AST.Nodes.UnaryOp;
import AST.Nodes.VarDecl;
import AST.Nodes.VarDef;
import MyError.MyError;
import MyError.MyErrorType;
import MyToken.Token;
import MyToken.TokenType;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static MyToken.TokenType.BADOR;
import static MyToken.TokenType.CHRCON;
import static MyToken.TokenType.CONSTTK;
import static MyToken.TokenType.INTCON;
import static MyToken.TokenType.STRCON;
import static MyToken.TokenType.*;

public class Parser {
    private ArrayList<Token> tokens;
    private int pos;
    private Token curToken;
    private ArrayList<MyError> errors;
//    private final ArrayList<TokenType> debugArray;
    
    
    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        pos = 0;
        curToken = tokens.get(0); // initialize as first token;
        errors = new ArrayList<>();
//        debugArray = new ArrayList<>(Arrays.asList(new TokenType[]{
//
//                GETINTTK,
//
//        }));
    }
    
    
    private int getLino() {
        return tokens.get(pos).getLineno();
    }
    
    private boolean notEnd() {
        return pos < tokens.size();
    }
    
    private void read() {
        if (pos < tokens.size() - 1) {
            pos++;
            curToken = tokens.get(pos);
        }
    }
    
    private Token checkAndAssign(TokenType type) {
        if (curToken.getType() == type || curToken.getType() == TokenType.BADAND || curToken.getType() == BADOR) {
            Token ret = curToken;
            read();
            return ret;
        } else {
            MyErrorType errorType;
            String content = curToken.getContent();
            switch (type) {
                case SEMICN:
                    errorType = MyErrorType.i;
                    content = ";";
                    break;
                case RPARENT:
                    errorType = MyErrorType.j;
                    content = ")";
                    break;
                case RBRACK:
                    errorType = MyErrorType.k;
                    content = "]";
                    break;
                default:
                    errorType = null;
            }
            if (errorType == null) {
                System.out.println("Unknown Error Type in CheckAndAssign, should be:" + type.toString() +
                        "now at " + curToken + " at line " + getLino());
//                if (type == IDENFR && debugArray.contains(curToken.getType())){
//                    try {
//                        PrintStream out = new PrintStream(new FileOutputStream("symbol.txt"));
//                        System.setOut(out);
//                        System.out.println("123");
//                        out.close();
//                        out = new PrintStream(new FileOutputStream("error.txt"));
//                        System.out.println("123");
//                        out.close();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                    System.exit(0);
//                }
            }
//            TODO: checkAndAssign错误处理
            errors.add(new MyError(Objects.requireNonNull(preview(-1)).getLineno(), errorType));
            return new Token(type, content, getLino());
        }
    }
    
    private Token checkAndAssign(TokenType type, boolean raiseError) {
        if (curToken.getType() == type || curToken.getType() == TokenType.BADAND || curToken.getType() == BADOR) {
            Token ret = curToken;
            read();
            return ret;
        } else {
            String content = curToken.getContent();
            switch (type) {
                case SEMICN:
                    content = ";";
                    break;
                case RPARENT:
                    content = ")";
                    break;
                case RBRACK:
                    content = "]";
                    break;
            }
            return new Token(type, content, getLino());
        }
        
    }
    
    private Token preview(int num) {
        if (pos + num < tokens.size()) {
            return tokens.get(pos + num);
        } else
            return null;
    }
    
    public ArrayList<MyError> getErrors() {
        return errors;
    }
    
    public CompUnit parseCompUnit() {
        ArrayList<Decl> decls = new ArrayList<>();
        ArrayList<FuncDef> funcDefs = new ArrayList<>();
        while (notEnd() && Objects.requireNonNull(preview(2)).getType() != TokenType.LPARENT) {
            Decl decl = parseDecl();
            if (decl != null) {
                decls.add(decl);
            } else {
                System.out.println("parseDecl failed, breaking");
            }
        }
        
        while (notEnd() && Objects.requireNonNull(preview(1)).getType() != TokenType.MAINTK) {
            FuncDef funcDef = parseFuncDef();
            if (funcDef != null) {
                funcDefs.add(funcDef);
            } else {
                System.out.println("funcDef failed, breaking");
            }
        }
        MainFuncDef mainFuncDef = parseMainFuncDef();
        return new CompUnit(getLino(), decls, funcDefs, mainFuncDef);
    }
    
    
    private Decl parseDecl() {
        Decl res = null;
        if (curToken.getType() == CONSTTK) {
            ConstDecl child = parseConstDecl();
            res = new Decl(getLino(), child);
        } else {
            VarDecl child = parseVarDecl();
            res = new Decl(getLino(), child);
        }
        return res;
    }
    
    private ConstDecl parseConstDecl() {
        Token _const = checkAndAssign(CONSTTK);
        BType bType = parseBType();
        ArrayList<ConstDef> constDefs = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        constDefs.add(parseConstDef());
        while (curToken.getType() == TokenType.COMMA) {
            commas.add(checkAndAssign(TokenType.COMMA));
            constDefs.add(parseConstDef());
        }
        Token semicn = checkAndAssign(TokenType.SEMICN);
        return new ConstDecl(getLino(), _const, bType, constDefs, commas, semicn);
    }
    
    private BType parseBType() {
        if (curToken.getType() != TokenType.INTTK && curToken.getType() != TokenType.CHARTK) {
            System.out.println("parseBType failed");
            return null;
        }
        BType res = new BType(getLino(), curToken);
        read();
        return res;
    }
    
    private ConstDef parseConstDef() {
        Token id = checkAndAssign(TokenType.IDENFR);
        Token lBrack = null;
        Token rBrack = null;
        ConstExp constExp = null;
        if (curToken.getType() == TokenType.LBRACK) { // array
            lBrack = checkAndAssign(TokenType.LBRACK);
            constExp = parseConstExp();
            rBrack = checkAndAssign(TokenType.RBRACK);
        }
        Token assign = checkAndAssign(TokenType.ASSIGN);
        ConstInitVal constInitVal = parseConstInitVal();
        if (lBrack != null && rBrack != null) {
            return new ConstDef(getLino(), id, lBrack, constExp, rBrack, assign, constInitVal);
        }
        return new ConstDef(getLino(), id, assign, constInitVal);
    }
    
    private ConstExp parseConstExp() {
        return new ConstExp(getLino(), parseAddExp());
    }
    
    private ConstInitVal parseConstInitVal() {
        ConstInitVal res;
        if (curToken.getType() == TokenType.LBRACE) { // array
            Token lBrace = checkAndAssign(TokenType.LBRACE);
            ArrayList<ConstExp> constExps = new ArrayList<>();
            ArrayList<Token> commas = new ArrayList<>();
            if (curToken.getType() != TokenType.RBRACE) {
                constExps.add(parseConstExp());
                while (curToken.getType() == TokenType.COMMA) {
                    commas.add(checkAndAssign(TokenType.COMMA));
                    constExps.add(parseConstExp());
                }
            }
            Token rBrace = checkAndAssign(TokenType.RBRACE);
            res = new ConstInitVal(getLino(), lBrace, constExps, commas, rBrace);
        } else if (curToken.getType() == STRCON) { // stringConst
            Token str = checkAndAssign(STRCON);
            res = new ConstInitVal(getLino(), str);
        } else {
            res = new ConstInitVal(getLino(), parseConstExp());
        }
        
        return res;
    }
    
    private VarDecl parseVarDecl() {
        BType bType = parseBType();
        ArrayList<VarDef> varDefs = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        varDefs.add(parseVarDef());
        while (curToken.getType() == TokenType.COMMA) {
            commas.add(checkAndAssign(TokenType.COMMA));
            varDefs.add(parseVarDef());
        }
        Token semicn = checkAndAssign(TokenType.SEMICN);
        return new VarDecl(getLino(), bType, varDefs, commas, semicn);
    }
    
    private VarDef parseVarDef() {
        Token id = checkAndAssign(TokenType.IDENFR);
        Token assign = null;
        Token lBrack = null;
        Token rBrack = null;
        ConstExp constExp = null;
        if (curToken.getType() == TokenType.LBRACK) {
            lBrack = checkAndAssign(TokenType.LBRACK);
            constExp = parseConstExp();
            rBrack = checkAndAssign(TokenType.RBRACK);
        }
        InitVal initVal = null;
        if (curToken.getType() == TokenType.ASSIGN) {
            assign = checkAndAssign(TokenType.ASSIGN);
            initVal = parseInitVal();
        }
        return new VarDef(getLino(), id, lBrack, constExp, rBrack, assign, initVal);
    }
    
    private InitVal parseInitVal() {
        InitVal res = null;
        if (curToken.getType() == TokenType.LBRACE) {
            Token lBrace = checkAndAssign(TokenType.LBRACE);
            ArrayList<Exp> exps = new ArrayList<>();
            ArrayList<Token> commas = new ArrayList<>();
            Exp exp = parseExp();
            exps.add(exp);
            while (curToken.getType() == TokenType.COMMA) {
                commas.add(checkAndAssign(TokenType.COMMA));
                exps.add(parseExp());
            }
            Token rBrace = checkAndAssign(TokenType.RBRACE);
            res = new InitVal(getLino(), lBrace, exps, commas, rBrace);
        } else if (curToken.getType() == STRCON) {
            res = new InitVal(getLino(), curToken);
            read();
        } else {
            res = new InitVal(getLino(), parseExp());
        }
        return res;
    }
    
    private Exp parseExp() {
        return new Exp(getLino(), parseAddExp());
    }
    
    private AddExp parseAddExp() {
        ArrayList<MulExp> mulExps = new ArrayList<>();
        ArrayList<Token> ops = new ArrayList<>();
        MulExp mulExp = parseMulExp();
        mulExps.add(mulExp);
        while (curToken.getType() == TokenType.PLUS || curToken.getType() == TokenType.MINU) {
            ops.add(checkAndAssign(curToken.getType()));
            mulExp = parseMulExp();
            mulExps.add(mulExp);
        }
        return new AddExp(getLino(), mulExps, ops);
    }
    
    private MulExp parseMulExp() {
        ArrayList<UnaryExp> unaryExps = new ArrayList<>();
        ArrayList<Token> ops = new ArrayList<>();
        
        UnaryExp unaryExp = parseUnaryExp();
        unaryExps.add(unaryExp);
        while (curToken.getType() == TokenType.MULT || curToken.getType() == TokenType.DIV || curToken.getType() == TokenType.MOD) {
            ops.add(checkAndAssign(curToken.getType()));
            unaryExps.add(parseUnaryExp());
        }
        return new MulExp(getLino(), unaryExps, ops);
    }
    
    private UnaryExp parseUnaryExp() {
        ArrayList<UnaryOp> unaryOps = new ArrayList<>();
        while (curToken.getType() == TokenType.PLUS || curToken.getType() == TokenType.MINU
                || curToken.getType() == TokenType.NOT) {
            unaryOps.add(new UnaryOp(getLino(), curToken));
            read();
        }
        if (curToken.getType() == TokenType.IDENFR &&
                Objects.requireNonNull(preview(1)).getType() == TokenType.LPARENT) {
            Token ident = checkAndAssign(TokenType.IDENFR);
            Token lParen = checkAndAssign(TokenType.LPARENT);
            FuncRParams funcRParams = null;
            if (curToken.getType() != TokenType.RPARENT && isExp()) {
                funcRParams = parseFuncRParams();
            }
            Token rParen = checkAndAssign(TokenType.RPARENT);
            return new UnaryExp(getLino(), ident, lParen, funcRParams, rParen, unaryOps);
        } else {
            return new UnaryExp(getLino(), parsePrimaryExp(), unaryOps);
        }
    }
    
    private boolean isExp() {
        return curToken.getType() == TokenType.PLUS || curToken.getType() == TokenType.MINU
                || curToken.getType() == TokenType.IDENFR || curToken.getType() == INTCON
                || curToken.getType() == TokenType.LPARENT || curToken.getType() == CHRCON;
    }
    
    private PrimaryExp parsePrimaryExp() {
        if (curToken.getType() == TokenType.LPARENT) {
            Token lParen = checkAndAssign(TokenType.LPARENT);
            Exp exp = parseExp();
            Token rParen = checkAndAssign(TokenType.RPARENT);
            return new PrimaryExp(getLino(), lParen, exp, rParen);
        } else if (curToken.getType() == INTCON) {
            return new PrimaryExp(getLino(), new Number(getLino(), checkAndAssign(INTCON)));
        } else if (curToken.getType() == CHRCON) {
            return new PrimaryExp(getLino(), new Character(getLino(), checkAndAssign(CHRCON)));
        } else {
            return new PrimaryExp(getLino(), parseLVal());
        }
    }
    
    private LVal parseLVal() {
        Token ident = checkAndAssign(TokenType.IDENFR);
        if (curToken.getType() == TokenType.LBRACK) {
            Token lBracket = checkAndAssign(TokenType.LBRACK);
            Exp exp = parseExp();
            Token rBracket = checkAndAssign(TokenType.RBRACK);
            return new LVal(getLino(), ident, lBracket, exp, rBracket);
        } else {
            return new LVal(getLino(), ident);
        }
    }
    
    private void preReadLVal() {
        Token ident = checkAndAssign(TokenType.IDENFR, false);
        if (curToken.getType() == TokenType.LBRACK) {
            Token lBracket = checkAndAssign(TokenType.LBRACK, false);
            Exp exp = parseExp();
            
            Token rBracket = checkAndAssign(TokenType.RBRACK, false);
        }
    }
    
    private FuncRParams parseFuncRParams() {
        ArrayList<Exp> exps = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        exps.add(parseExp());
        while (curToken.getType() == TokenType.COMMA) {
            commas.add(checkAndAssign(TokenType.COMMA));
            exps.add(parseExp());
        }
        return new FuncRParams(getLino(), exps, commas);
    }
    
    private FuncDef parseFuncDef() {
        FuncType funcType;
        if (curToken.getType() == TokenType.VOIDTK || curToken.getType() == TokenType.INTTK || curToken.getType() == TokenType.CHARTK) {
            funcType = new FuncType(getLino(), curToken);
            read();
        } else {
            System.out.println("Error in checking FuncType");
            return null;
        }
        Token ident = checkAndAssign(TokenType.IDENFR);
        Token lParen = checkAndAssign(TokenType.LPARENT);
        FuncFParams funcFParams = null;
        if (curToken.getType() != TokenType.RPARENT && curToken.getType() != TokenType.LBRACE)
            funcFParams = parseFuncFParams();
        Token rParen = checkAndAssign(TokenType.RPARENT);
        Block block = parseBlock();
        return new FuncDef(getLino(), funcType, ident, lParen, funcFParams, rParen, block);
    }
    
    private FuncFParams parseFuncFParams() {
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        funcFParams.add(parseFuncFParam());
        while (curToken.getType() == TokenType.COMMA) {
            commas.add(checkAndAssign(TokenType.COMMA));
            funcFParams.add(parseFuncFParam());
        }
        return new FuncFParams(getLino(), funcFParams, commas);
    }
    
    private FuncFParam parseFuncFParam() {
        BType bType = parseBType();
        Token ident = checkAndAssign(TokenType.IDENFR);
        if (curToken.getType() == TokenType.LBRACK) {
            Token lBracket = checkAndAssign(TokenType.LBRACK);
            Token rBracket = checkAndAssign(TokenType.RBRACK);
            return new FuncFParam(getLino(), bType, ident, lBracket, rBracket);
        } else
            return new FuncFParam(getLino(), bType, ident);
    }
    
    private Block parseBlock() {
        Token leftBracket = checkAndAssign(TokenType.LBRACE);
        
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        while (curToken.getType() != TokenType.RBRACE) {
            blockItems.add(parseBlockItem());
        }
        Token rightBracket = checkAndAssign(TokenType.RBRACE);
        return new Block(getLino(), leftBracket, blockItems, rightBracket);
    }
    
    private BlockItem parseBlockItem() {
        if (curToken.getType() == TokenType.INTTK || curToken.getType() == TokenType.CHARTK
                || curToken.getType() == TokenType.VOIDTK || curToken.getType() == CONSTTK) {
            return new BlockItem(getLino(), parseDecl());
        } else {
            return new BlockItem(getLino(), parseStmt());
        }
    }
    
    private Stmt parseStmt() {
        if (curToken.getType() == TokenType.IFTK) {
            return new Stmt(getLino(), parseIfStmt());
        } else if (curToken.getType() == TokenType.LBRACE) {
            return new Stmt(getLino(), parseBlock());
        } else if (curToken.getType() == TokenType.RETURNTK) {
            return new Stmt(getLino(), parseReturnStmt());
        } else if (curToken.getType() == TokenType.BREAKTK) {
            return new Stmt(getLino(), parseBreakStmt());
        } else if (curToken.getType() == TokenType.CONTINUETK) {
            return new Stmt(getLino(), parseContinueStmt());
        } else if (curToken.getType() == TokenType.FORTK) {
            return new Stmt(getLino(), parseForStmts());
        } else if (curToken.getType() == TokenType.PRINTFTK) {
            return new Stmt(getLino(), parsePrintfStmt());
        } else if (curToken.getType() != TokenType.IDENFR
                || Objects.requireNonNull(preview(1)).getType() == TokenType.LPARENT) {
            if (curToken.getType() == TokenType.SEMICN) {
                return new Stmt(getLino(), checkAndAssign(TokenType.SEMICN));
            }
            return new Stmt(getLino(), parseExp(), checkAndAssign(TokenType.SEMICN));
        } /*else if (curToken.getType() == TokenType.IDENFR
                && Objects.requireNonNull(preview(2)).getType() == TokenType.GETINTTK
        ) {
            return new Stmt(getLino(), parseInputInt());
        } else if (curToken.getType() == TokenType.IDENFR && Objects.requireNonNull(preview(2)).getType() == TokenType.GETCHARTK) {
            return new Stmt(getLino(), parseInputChar());
        }*/ else if (curToken.getType() == TokenType.IDENFR) {
            int oldPos = pos;
            //parseLVal();
            preReadLVal();
            if (curToken.getType() == TokenType.ASSIGN) {
                if (Objects.requireNonNull(preview(1)).getType() == GETINTTK) {
                    pos = oldPos;
                    curToken = tokens.get(pos);
                    return new Stmt(getLino(), parseInputInt());
                } else if (Objects.requireNonNull(preview(1)).getType() == GETCHARTK) {
                    pos = oldPos;
                    curToken = tokens.get(pos);
                    return new Stmt(getLino(), parseInputChar());
                }
                pos = oldPos;
                curToken = tokens.get(pos);
                return new Stmt(getLino(), parseAssignStmt());
            }
            pos = oldPos;
            curToken = tokens.get(pos);
            return new Stmt(getLino(), parseExp(), checkAndAssign(TokenType.SEMICN));
        } else {
            // return new Stmt(getLino(), parseExp(), checkAndAssign(TokenType.SEMICN));
            System.out.println("Error in parsing Statement!");
            return null;
        }
    }
    
    private IfStmt parseIfStmt() {
        Token _if = checkAndAssign(TokenType.IFTK);
        Token leftParen = checkAndAssign(TokenType.LPARENT);
        Cond cond = parseCond();
        Token rightParen = checkAndAssign(TokenType.RPARENT);
        Stmt stmt1 = parseStmt();
        if (curToken.getType() == TokenType.ELSETK) {
            Token _else = checkAndAssign(TokenType.ELSETK);
            Stmt stmt2 = parseStmt();
            return new IfStmt(getLino(), _if, leftParen, cond, rightParen, stmt1, _else, stmt2);
        } else {
            return new IfStmt(getLino(), _if, leftParen, cond, rightParen, stmt1);
        }
    }
    
    private Cond parseCond() {
        return new Cond(getLino(), parseLOrExp());
    }
    
    private LOrExp parseLOrExp() {
        ArrayList<LAndExp> lAndExps = new ArrayList<>();
        ArrayList<Token> orOps = new ArrayList<>();
        lAndExps.add(parseLAndExp());
        while (curToken.getType() == TokenType.OR || curToken.getType() == BADOR) {
            orOps.add(checkAndAssign(TokenType.OR));
            lAndExps.add(parseLAndExp());
        }
        return new LOrExp(getLino(), lAndExps, orOps);
    }
    
    private LAndExp parseLAndExp() {
        ArrayList<EqExp> eqExps = new ArrayList<>();
        ArrayList<Token> andOps = new ArrayList<>();
        eqExps.add(parseEqExp());
        while (curToken.getType() == TokenType.AND || curToken.getType() == TokenType.BADAND) {
            andOps.add(checkAndAssign(TokenType.AND));
            eqExps.add(parseEqExp());
        }
        return new LAndExp(getLino(), eqExps, andOps);
    }
    
    private EqExp parseEqExp() {
        ArrayList<RelExp> relExps = new ArrayList<>();
        ArrayList<Token> eqOps = new ArrayList<>();
        relExps.add(parseRelExp());
        while (curToken.getType() == TokenType.EQL || curToken.getType() == TokenType.NEQ) {
            eqOps.add(checkAndAssign(curToken.getType()));
            relExps.add(parseRelExp());
        }
        return new EqExp(getLino(), relExps, eqOps);
    }
    
    private RelExp parseRelExp() {
        ArrayList<AddExp> addExps = new ArrayList<>();
        ArrayList<Token> relOps = new ArrayList<>();
        addExps.add(parseAddExp());
        while (curToken.getType() == TokenType.LSS || curToken.getType() == TokenType.LEQ
                || curToken.getType() == TokenType.GRE || curToken.getType() == TokenType.GEQ) {
            relOps.add(checkAndAssign(curToken.getType()));
            addExps.add(parseAddExp());
        }
        return new RelExp(getLino(), addExps, relOps);
    }
    
    private Return parseReturnStmt() {
        Token _return = checkAndAssign(TokenType.RETURNTK);
        if (curToken.getType() == TokenType.SEMICN) {
            return new Return(getLino(), _return, checkAndAssign(TokenType.SEMICN));
        } else {
            Exp exp = parseExp();
            return new Return(getLino(), _return, exp, checkAndAssign(TokenType.SEMICN));
        }
    }
    
    private ForStmts parseForStmts() {
        if (getLino() == 11) {
            System.out.println("ForStmts");
        }
        Token _for = checkAndAssign(TokenType.FORTK);
        Token leftParen = checkAndAssign(TokenType.LPARENT);
        ForStmt stmt1 = null;
        if (curToken.getType() != TokenType.SEMICN) {
            stmt1 = parseForStmt();
        }
        Token semicn1 = checkAndAssign(TokenType.SEMICN);
        Cond cond = null;
        if (curToken.getType() != TokenType.SEMICN) {
            cond = parseCond();
        }
        Token semicn2 = checkAndAssign(TokenType.SEMICN);
        ForStmt stmt2 = null;
        if (curToken.getType() != TokenType.RPARENT) {
            stmt2 = parseForStmt();
        }
        Token rightParen = checkAndAssign(TokenType.RPARENT);
        Stmt stmt = parseStmt();
        return new ForStmts(getLino(), _for, leftParen, stmt1, semicn1, cond, semicn2, stmt2, rightParen, stmt);
    }
    
    private ForStmt parseForStmt() {
        LVal lVal = parseLVal();
        Token assign = checkAndAssign(TokenType.ASSIGN);
        Exp exp = parseExp();
        return new ForStmt(getLino(), lVal, assign, exp);
    }
    
    private Break parseBreakStmt() {
        return new Break(getLino(), checkAndAssign(TokenType.BREAKTK), checkAndAssign(TokenType.SEMICN));
    }
    
    private Continue parseContinueStmt() {
        return new Continue(getLino(), checkAndAssign(TokenType.CONTINUETK), checkAndAssign(TokenType.SEMICN));
    }
    
    private Printf parsePrintfStmt() {
        Token _printf = checkAndAssign(TokenType.PRINTFTK);
        Token leftParen = checkAndAssign(TokenType.LPARENT);
        Token strConst = checkAndAssign(STRCON);
        ArrayList<Exp> exps = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        while (curToken.getType() == TokenType.COMMA) {
            commas.add(checkAndAssign(TokenType.COMMA));
            exps.add(parseExp());
        }
        Token rightParen = checkAndAssign(TokenType.RPARENT);
        return new Printf(getLino(), _printf, leftParen, strConst, exps, commas, rightParen, checkAndAssign(TokenType.SEMICN));
    }
    
    private InputInt parseInputInt() {
        return new InputInt(getLino(), parseLVal(), checkAndAssign(TokenType.ASSIGN), checkAndAssign(TokenType.GETINTTK),
                checkAndAssign(TokenType.LPARENT), checkAndAssign(TokenType.RPARENT), checkAndAssign(TokenType.SEMICN));
    }
    
    private InputChar parseInputChar() {
        return new InputChar(getLino(), parseLVal(), checkAndAssign(TokenType.ASSIGN), checkAndAssign(TokenType.GETCHARTK),
                checkAndAssign(TokenType.LPARENT), checkAndAssign(TokenType.RPARENT), checkAndAssign(TokenType.SEMICN));
    }
    
    private AssignStmt parseAssignStmt() {
        LVal lVal = parseLVal();
        Token assign = checkAndAssign(TokenType.ASSIGN);
        Exp exp = parseExp();
        return new AssignStmt(getLino(), lVal, assign, exp, checkAndAssign(TokenType.SEMICN));
    }
    
    private MainFuncDef parseMainFuncDef() {
        Token _int = checkAndAssign(TokenType.INTTK);
        Token main = checkAndAssign(TokenType.MAINTK);
        Token leftParen = checkAndAssign(TokenType.LPARENT);
        Token rightParen = checkAndAssign(TokenType.RPARENT);
        Block block = parseBlock();
        return new MainFuncDef(getLino(), _int, main, leftParen, rightParen, block);
    }
}
