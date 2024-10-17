package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class AssignStmt extends Node {
    private LVal lVal;
    private Token assign;
    private Exp exp;
    private Token semicn;
    
    public AssignStmt(int lino, LVal lVal, Token assign, Exp exp, Token semicn) {
        super(lino, NodeType.AssignStmt);
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
        this.semicn = semicn;
    }
    
    public LVal getlVal() {
        return lVal;
    }
    
    public Exp getExp() {
        return exp;
    }
    
    public void print() {
        lVal.print();
        System.out.println(assign.getType() + " " + assign.getContent());
        exp.print();
        System.out.println(semicn.getType() + " " + semicn.getContent());
    }
}
