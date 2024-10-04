package AST.Nodes;

import AST.Node;
import AST.NodeType;

public class Cond extends Node {
    private LOrExp lorExp;
    
    public Cond(int lineno, LOrExp lorExp) {
        super(lineno, NodeType.Cond);
        this.lorExp = lorExp;
    }
    
    public void print() {
        lorExp.print();
        super.print();
    }
}
