package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class Stmt extends Node {
    /*
    语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    | [Exp] ';' //有无Exp两种情况
    | Block
     | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
     | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省，1种情况 2.
    ForStmt与Cond中缺省一个，3种情况 3. ForStmt与Cond中缺省两个，3种情况 4. ForStmt与Cond全部
    缺省，1种情况
    | 'break' ';' | 'continue' ';'
     | 'return' [Exp] ';' // 1.有Exp 2.无Exp
     | LVal '=' 'getint''('')'';'
     | LVal '=' 'getchar''('')'';'
     | 'printf''('StringConst {','Exp}')'';' // 1.有Exp 2.无Exp
    
    */
    private AssignStmt assignStmt;
    
    private Exp exp;
    private Token semicn; // for [exp];
    
    private Block block;
    
    private IfStmt ifStmt;
    private ForStmts forStmts;
    
    private Break _break;
    private Continue _continue;
    private Return _return;
    private InputInt inputInt;
    private InputChar inputChar;
    private Printf printf;
    
    public Stmt(int lineno, AssignStmt assignStmt) {
        super(lineno, NodeType.Stmt);
        this.assignStmt = assignStmt;
    }
    
    public Stmt(int lineno, Exp exp, Token semicn) {
        super(lineno, NodeType.Stmt);
        this.exp = exp;
        this.semicn = semicn;
    }
    
    public Stmt(int lino, Token semicn) {
        super(lino, NodeType.Stmt);
        this.semicn = semicn;
    }
    
    public Stmt(int lineno, Block block) {
        super(lineno, NodeType.Stmt);
        this.block = block;
    }
    
    public Stmt(int lineno, IfStmt ifStmt) {
        super(lineno, NodeType.Stmt);
        this.ifStmt = ifStmt;
    }
    
    public Stmt(int lineno, ForStmts forStmts) {
        super(lineno, NodeType.Stmt);
        this.forStmts = forStmts;
    }
    
    public Stmt(int lineno, Break _break) {
        super(lineno, NodeType.Stmt);
        this._break = _break;
    }
    
    public Stmt(int lineno, Continue _continue) {
        super(lineno, NodeType.Stmt);
        this._continue = _continue;
    }
    
    public Stmt(int lineno, Return _return) {
        super(lineno, NodeType.Stmt);
        this._return = _return;
    }
    
    public Stmt(int lineno, InputInt inputInt) {
        super(lineno, NodeType.Stmt);
        this.inputInt = inputInt;
    }
    
    public Stmt(int lineno, InputChar inputChar) {
        super(lineno, NodeType.Stmt);
        this.inputChar = inputChar;
    }
    
    public Stmt(int lineno, Printf printf) {
        super(lineno, NodeType.Stmt);
        this.printf = printf;
    }
    
    public void print() {
        if (assignStmt != null) {
            assignStmt.print();
        } else if (block != null) {
            block.print();
        } else if (ifStmt != null) {
            ifStmt.print();
        } else if (forStmts != null) {
            forStmts.print();
        } else if (_break != null) {
            _break.print();
        } else if (_continue != null) {
            _continue.print();
        } else if (_return != null) {
            _return.print();
        } else if (inputInt != null) {
            inputInt.print();
        } else if (inputChar != null) {
            inputChar.print();
        } else if (printf != null) {
            printf.print();
        } else if (exp != null) {
            exp.print();
            semicn.print();
        } else {
            semicn.print();
        }
        super.print();
    }
}

