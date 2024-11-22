package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class Character extends Node {
    private Token charConst;
    
    public Character(int lineno, Token charConst) {
        super(lineno, NodeType.Character);
        this.charConst = charConst;
    }

    public char getCharConst() {
        return charConst.getContent().charAt(0); // TODO: check
    }

    public void print() {
        charConst.print();
        super.print();
    }
}
