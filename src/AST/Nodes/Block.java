package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;
import util.Printer;

import java.util.ArrayList;

public class Block extends Node {
    private Token leftBracket;
    private ArrayList<BlockItem> blockItems;
    private Token rightBracket;
    
    public Block(int lino, Token leftBracket, ArrayList<BlockItem> blockItems, Token rightBracket) {
        super(lino, NodeType.Block);
        this.leftBracket = leftBracket;
        this.blockItems = blockItems;
        this.rightBracket = rightBracket;
    }
    
    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }
    
    public Token getRightBracket() {
        return rightBracket;
    }
    
    public void print() {
        Printer.print(leftBracket);
        for (BlockItem blockItem : blockItems) {
            blockItem.print();
        }
        Printer.print(rightBracket);
        super.print();
    }
}
