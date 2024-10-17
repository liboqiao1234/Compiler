package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;
import util.Printer;

import java.util.Objects;

public class BType extends Node {
    private Token type;
    
    public BType(int lineno, Token type) {
        super(lineno, NodeType.BType);
        this.type = type;
    }
    
    public void print() {
        Printer.print(type);
        // no super
    }
    
    public boolean equals(String comparedType) {
        return Objects.equals(type.getContent(), comparedType);
    }
    
}
