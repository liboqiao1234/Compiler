package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class FuncFParam extends Node {
    private BType bType;
    private Token ident;
    
    private Token leftBracket;
    private Token rightBracket;
    
    public FuncFParam(int lino, BType bType, Token ident, Token leftBracket, Token rightBracket) {
        super(lino, NodeType.FuncFParam);
        this.bType = bType;
        this.ident = ident;
        this.leftBracket = leftBracket;
        this.rightBracket = rightBracket;
    }
    
    public FuncFParam(int lino, BType bType, Token ident) {
        super(lino, NodeType.FuncFParam);
        this.bType = bType;
        this.ident = ident;
    }
    
    public BType getBType() {
        return bType;
    }
    
    public Token getIdent() {
        return ident;
    }
    
    public Token getLeftBracket() {
        return leftBracket;
    }
    
    public Token getRightBracket() {
        return rightBracket;
    }
    
    public void print() {
        bType.print();
        ident.print();
        if (leftBracket != null) {
            leftBracket.print();
            rightBracket.print();
        }
        super.print();
    }
}
