package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

import java.util.ArrayList;

public class InitVal extends Node {
    
    private Exp exp = null;
    
    private Token leftBracket = null;
    private ArrayList<Exp> expList = null;
    private ArrayList<Token> commas = null;
    private Token rightBracket = null;
    private Token stringConst = null;
    
    
    public InitVal(int lineno, Exp exp) {
        super(lineno, NodeType.InitVal);
        this.exp = exp;
    }
    
    public InitVal(int lineno, Token leftBracket, ArrayList<Exp> expList, ArrayList<Token> commas, Token rightBracket) {
        super(lineno, NodeType.InitVal);
        this.leftBracket = leftBracket;
        this.expList = expList;
        this.commas = commas;
        this.rightBracket = rightBracket;
    }
    
    public InitVal(int lineno, Token stringConst) {
        super(lineno, NodeType.InitVal);
        this.stringConst = stringConst;
    }
    
    public Exp getExp() {
        return exp;
    }
    
    public ArrayList<Exp> getExpList() {
        return expList;
    }
    
    public Token getStringConst() {
        return stringConst;
    }
    
    public void print() {
        if (exp != null) {
            exp.print();
        } else if (leftBracket != null) {
            leftBracket.print();
            for (int i = 0; i < expList.size(); i++) {
                expList.get(i).print();
                if (i < commas.size()) {
                    commas.get(i).print();
                }
            }
            rightBracket.print();
        } else {
            stringConst.print();
        }
        super.print();
    }
}
