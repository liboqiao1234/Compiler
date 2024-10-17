package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;
import Symbol.SymbolType;

public class LVal extends Node {
    private Token ident;
    
    private Token leftBracket = null;
    private Exp exp = null;
    private Token rightBracket = null;
    
    public Token getIdent() {
        return ident;
    }
    
    public Exp getExp() {
        return exp;
    }
    
    
    public LVal(int lino, Token ident) {
        super(lino, NodeType.LVal);
        this.ident = ident;
    }
    
    public LVal(int lino, Token ident, Token leftBracket, Exp exp, Token rightBracket) {
        super(lino, NodeType.LVal);
        this.ident = ident;
        this.leftBracket = leftBracket;
        this.exp = exp;
        this.rightBracket = rightBracket;
    }

    public void print() {
        ident.print();
        if (leftBracket != null) {
            leftBracket.print();
            exp.print();
            rightBracket.print();
        }
        super.print();
    }
}
