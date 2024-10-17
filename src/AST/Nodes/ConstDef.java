package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;
import util.Printer;

public class ConstDef extends Node {
    private Token ident;
    private Token leftBraket;
    private ConstExp constExp = null;
    private Token rightBraket;
    private Token assign;
    private ConstInitVal constInitVal;
    
    public ConstDef(int lineno, Token ident, Token leftBraket, ConstExp constExp, Token rightBraket, Token assign, ConstInitVal constInitVal) {
        super(lineno, NodeType.ConstDef);
        this.ident = ident;
        this.leftBraket = leftBraket;
        this.constExp = constExp;
        this.rightBraket = rightBraket;
        this.assign = assign;
        this.constInitVal = constInitVal;
    }
    
    public ConstDef(int lineno, Token ident, Token assign, ConstInitVal constInitVal) {
        super(lineno, NodeType.ConstDef);
        this.ident = ident;
        this.assign = assign;
        this.constInitVal = constInitVal;
    }
    
    public Token getIdent() {
        return ident;
    }
    
    public ConstExp getConstExp() {
        return constExp;
    }
    
    public ConstInitVal getConstInitVal() {
        return constInitVal;
    }
    
    public void print() {
        Printer.print(ident);
        if (leftBraket != null) {
            Printer.print(leftBraket);
            constExp.print();
            Printer.print(rightBraket);
        }
        Printer.print(assign);
        constInitVal.print();
        super.print();
        
    }
}
