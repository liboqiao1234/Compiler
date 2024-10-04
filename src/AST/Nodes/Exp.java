package AST.Nodes;

import AST.Node;
import AST.NodeType;

public class Exp extends Node {
    private AddExp addExp;
    
    public Exp(int lineno, AddExp addExp) {
        super(lineno, NodeType.Exp);
        this.addExp = addExp;
    }
    
    public void print() {
        addExp.print();
        super.print();
    }
}
