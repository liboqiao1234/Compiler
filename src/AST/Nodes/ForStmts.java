package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class ForStmts extends Node {
    /*
    
    
    'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
     */
    
    private Token _for;
    private Token leftParen;
    private ForStmt forStmt1;
    private Token semicn1;
    private Cond cond;
    private Token semicn2;
    private ForStmt forStmt2;
    private Token rightParen;
    private Stmt stmt;
    
    public ForStmts(int lineno, Token _for, Token leftParen, ForStmt forStmt1, Token semicn1, Cond cond, Token semicn2, ForStmt forStmt2, Token rightParen, Stmt stmt) {
        super(lineno, NodeType.ForStmts);
        this._for = _for;
        this.leftParen = leftParen;
        this.forStmt1 = forStmt1;
        this.semicn1 = semicn1;
        this.cond = cond;
        this.semicn2 = semicn2;
        this.forStmt2 = forStmt2;
        this.rightParen = rightParen;
        this.stmt = stmt;
    }
    
    public ForStmt getForStmt1() {
        return forStmt1;
    }
    
    public ForStmt getForStmt2() {
        return forStmt2;
    }
    
    public Cond getCond() {
        return cond;
    }
    
    public Stmt getStmt() {
        return stmt;
    }
    
    public void print() {
        _for.print();
        leftParen.print();
        if (forStmt1 != null) {
            forStmt1.print();
        }
        semicn1.print();
        if (cond != null) {
            cond.print();
        }
        semicn2.print();
        if (forStmt2 != null) {
            forStmt2.print();
        }
        rightParen.print();
        stmt.print();
    }
}
