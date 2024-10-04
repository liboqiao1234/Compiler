package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class VarDef extends Node {
    private Token Iden;
    private Token LeftBracket;
    private ConstExp constExp;
    private Token RightBracket;
    
    private Token assign = null;
    private InitVal initVal = null;
    
    public VarDef(int lino, Token iden, Token leftBracket, ConstExp constExp,Token rightBracket) {
        super(lino, NodeType.VarDef);
        this.Iden = iden;
        this.LeftBracket = leftBracket;
        this.constExp = constExp;
        this.RightBracket = rightBracket;
    }
    
    public VarDef(int lino, Token iden, Token leftBracket, ConstExp constExp, Token rightBracket, Token assign, InitVal initVal) {
        super(lino, NodeType.VarDef);
        this.Iden = iden;
        this.LeftBracket = leftBracket;
        this.constExp = constExp;
        this.RightBracket = rightBracket;
        this.assign = assign;
        this.initVal = initVal;
    }
    
    public void print() {
        Iden.print();
        if (LeftBracket != null) {
            LeftBracket.print();
            constExp.print();
            RightBracket.print();
        }
        if (assign != null) {
            assign.print();
            initVal.print();
        }
        super.print();
    }
}
