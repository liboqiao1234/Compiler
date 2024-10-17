package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class InputInt extends Node {
    private LVal lVal;
    private Token assign;
    private Token getint;
    private Token leftParen;
    private Token rightParen;
    private Token semicn;
    
    public InputInt(int lineno, LVal lVal, Token assign, Token getint, Token leftParen, Token rightParen, Token semicn) {
        super(lineno, NodeType.InputInt);
        this.lVal = lVal;
        this.assign = assign;
        this.getint = getint;
        this.leftParen = leftParen;
        this.rightParen = rightParen;
        this.semicn = semicn;
    }
    
    public LVal getLVal() {
        return lVal;
    }
    
    public void print() {
        lVal.print();
        assign.print();
        getint.print();
        leftParen.print();
        rightParen.print();
        semicn.print();
    }
}
