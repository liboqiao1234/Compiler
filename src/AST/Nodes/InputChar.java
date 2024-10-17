package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class InputChar extends Node {
    private LVal lVal;
    private Token equal;
    private Token getchar;
    private Token leftParen;
    private Token rightParen;
    private Token semicn;
    
    public InputChar(int lineno, LVal lVal, Token equal, Token getchar, Token leftParen, Token rightParen, Token semicn) {
        super(lineno, NodeType.InputChar);
        this.lVal = lVal;
        this.equal = equal;
        this.getchar = getchar;
        this.leftParen = leftParen;
        this.rightParen = rightParen;
        this.semicn = semicn;
    }
    
    public LVal getLVal() {
        return lVal;
    }
    
    public void print() {
        lVal.print();
        equal.print();
        getchar.print();
        leftParen.print();
        rightParen.print();
        semicn.print();
    }
}
