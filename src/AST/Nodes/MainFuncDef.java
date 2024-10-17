package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;
import util.Printer;

public class MainFuncDef extends Node {
    private Token _int;
    private Token _main;
    private Token leftParen;
    private Token rightParen;
    private Block block;
    
    public MainFuncDef(int lino, Token _int, Token _main, Token leftParen, Token rightParen, Block block) {
        super(lino, NodeType.MainFuncDef);
        this._int = _int;
        this._main = _main;
        this.leftParen = leftParen;
        this.rightParen = rightParen;
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
    
    public void print() {
        Printer.print(_int);
        Printer.print(_main);
        Printer.print(leftParen);
        Printer.print(rightParen);
        block.print();
        super.print();
        // System.out.println("<MainFuncDef>");
    }
}
