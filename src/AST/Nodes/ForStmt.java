package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class ForStmt extends Node {
    private LVal lVal;
    private Token assign;
    private Exp exp;
    
    public ForStmt(int lineno, LVal lVal, Token assign, Exp exp) {
        super(lineno, NodeType.ForStmt);
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
        
    }
    
    public LVal getlVal() {
        return lVal;
    }
    
    public Exp getExp() {
        return exp;
    }
    
    public void print() {
        lVal.print();
        assign.print();
        exp.print();
        super.print();
    }
}
