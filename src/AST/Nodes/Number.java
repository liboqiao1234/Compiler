package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class Number extends Node {
    private Token num;
    
    public Number(int lino, Token num) {
        super(lino, NodeType.Number);
        this.num = num;
    }
    
    public void print() {
        num.print();
        super.print();
    }
}
