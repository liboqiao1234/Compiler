package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class PrimaryExp extends Node {
    private Token leftParen;
    private Exp exp;
    private Token rightParen;
    
    private LVal lVal;
    private Number number;
    private Character character;
    
    public PrimaryExp(int lino, Token leftParen, Exp exp, Token rightParen) {
        super(lino, NodeType.PrimaryExp);
        this.leftParen = leftParen;
        this.exp = exp;
        this.rightParen = rightParen;
    }
    
    public PrimaryExp(int lino, LVal lVal) {
        super(lino, NodeType.PrimaryExp);
        this.lVal = lVal;
    }
    
    public PrimaryExp(int lino, Number number) {
        super(lino, NodeType.PrimaryExp);
        this.number = number;
    }
    
    public PrimaryExp(int lino, Character character) {
        super(lino, NodeType.PrimaryExp);
        this.character = character;
    }
    
    public Exp getExp() {
        return exp;
    }
    
    public LVal getLVal() {
        return lVal;
    }
    
    public Number getNumber() {
        return number;
    }
    
    public Character getCharacter() {
        return character;
    }
    
    public void print() {
        if (leftParen != null) {
            leftParen.print();
            exp.print();
            rightParen.print();
        } else if (lVal != null) {
            lVal.print();
        } else if (number != null) {
            number.print();
        } else if (character != null) {
            character.print();
        }
        
        super.print();
    }
}
