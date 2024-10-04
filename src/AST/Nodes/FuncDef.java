package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class FuncDef extends Node {
    private FuncType funcType;
    private Token ident;
    private Token leftParen;
    
    private FuncFParams funcFParams = null;
    
    private Token rightParen;
    private Block block;
    
    public FuncDef(int lino, FuncType funcType, Token ident, Token leftParen, FuncFParams funcFParams, Token rightParen, Block block) {
        super(lino, NodeType.FuncDef);
        this.funcType = funcType;
        this.ident = ident;
        this.leftParen = leftParen;
        this.funcFParams = funcFParams;
        this.rightParen = rightParen;
        this.block = block;
    }
    
    public FuncDef(int lino, FuncType funcType, Token ident, Token leftParen, Token rightParen, Block block) {
        super(lino, NodeType.FuncDef);
        this.funcType = funcType;
        this.ident = ident;
        this.leftParen = leftParen;
        this.rightParen = rightParen;
        this.block = block;
    }
    
    public void print() {
        funcType.print();
        ident.print();
        leftParen.print();
        if (funcFParams != null) {
            funcFParams.print();
        }
        rightParen.print();
        block.print();
        super.print();
    }
}
