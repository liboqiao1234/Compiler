package AST.Nodes;

import AST.Node;
import AST.NodeType;

public class ConstExp extends Node {
    private AddExp addExp;
    
    public ConstExp(int lineno, AddExp node) {
        super(lineno, NodeType.ConstExp);
        this.addExp = node;
    }
    
    public AddExp getAddExp() {
        return addExp;
    }
    
    public void print() {
        addExp.print();
        super.print();
    }
}
