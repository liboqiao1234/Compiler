package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

import java.util.ArrayList;

public class ConstInitVal extends Node {
    private ConstExp constExp;
    
    private Token leftBrace;
    private ArrayList<ConstExp> constExps;
    private ArrayList<Token> commas;
    private Token rightBrace;
    
    private Token stringConst;
    
    
    public ConstInitVal(int lineno, ConstExp constExp) {
        super(lineno, NodeType.ConstInitVal);
        this.constExp = constExp;
    }
    
    public ConstInitVal(int lineno, Token leftBrace, ArrayList<ConstExp> constExps, ArrayList<Token> commas, Token rightBrace) {
        super(lineno, NodeType.ConstInitVal);
        this.leftBrace = leftBrace;
        this.constExps = constExps;
        this.commas = commas;
        this.rightBrace = rightBrace;
    }
    
    public ConstInitVal(int lineno, Token stringConst) {
        super(lineno, NodeType.ConstInitVal);
        this.stringConst = stringConst;
    }
    
    public ConstExp getConstExp() {
        return constExp;
    }
    
    public ArrayList<ConstExp> getConstExps() {
        return constExps;
    }
    
    public void print() {
        if (constExp != null) {
            constExp.print();
        } else if (leftBrace != null) {
            leftBrace.print();
            for (int i = 0; i < constExps.size(); i++) {
                constExps.get(i).print();
                if (i != commas.size()) commas.get(i).print();
            }
            rightBrace.print();
        } else {
            stringConst.print();
        }
        
        super.print();
    }
}
