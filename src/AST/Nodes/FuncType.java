package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class FuncType extends Node {
    private Token type;
    
    public FuncType(int lino, Token type) {
        super(lino, NodeType.FuncType);
        this.type = type;
    }
    
    public boolean equals(String comparedType) {
        return type.getContent().equals(comparedType);
    }
    
    public void print() {
        type.print();
        super.print();
    }
}
