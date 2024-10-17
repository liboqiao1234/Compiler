package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class IfStmt extends Node {
    private Token _if;
    private Token leftParen;
    private Cond cond;
    private Token rightParen;
    private Stmt stmt;
    private Token _else;
    private Stmt _elseStmt;
    
    public IfStmt(int lineno, Token _if, Token leftParen, Cond cond, Token rightParen, Stmt stmt) {
        super(lineno, NodeType.IfStmt);
        this._if = _if;
        this.leftParen = leftParen;
        this.cond = cond;
        this.rightParen = rightParen;
        this.stmt = stmt;
    }
    
    public IfStmt(int lineno, Token _if, Token leftParen, Cond cond, Token rightParen, Stmt stmt, Token _else, Stmt _elseStmt) {
        super(lineno, NodeType.IfStmt);
        this._if = _if;
        this.leftParen = leftParen;
        this.cond = cond;
        this.rightParen = rightParen;
        this.stmt = stmt;
        this._else = _else;
        this._elseStmt = _elseStmt;
    }
    
    public Cond getCond() {
        return cond;
    }
    
    public Stmt getStmt() {
        return stmt;
    }
    
    public Stmt getElseStmt() {
        return _elseStmt;
    }
    
    
    
    public void print() {
        _if.print();
        leftParen.print();
        cond.print();
        rightParen.print();
        stmt.print();
        if (_else != null) {
            _else.print();
            _elseStmt.print();
        }
    }
}
