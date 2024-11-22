package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;
import util.Printer;

public class UnaryOp extends Node {
    private Token op;
    
    public UnaryOp(int lino, Token op) {
        super(lino, NodeType.UnaryOp);
        this.op = op;
    }

    public Token getOp() {
        return op;
    }
    
    public void print() {
        Printer.print(op);
        super.print();
    }
}
