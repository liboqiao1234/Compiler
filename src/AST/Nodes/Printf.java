package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

import java.util.ArrayList;

public class Printf extends Node {
    private Token printf;
    private Token leftParen;
    private Token stringConst;
    private ArrayList<Exp> exps = null;
    private ArrayList<Token> commas;
    private Token rightParen;
    private Token semicn;
    
    public Printf(int lineno, Token printf, Token leftParen, Token stringConst, Token rightParen, Token semicn) {
        super(lineno, NodeType.Printf);
        this.printf = printf;
        this.leftParen = leftParen;
        this.stringConst = stringConst;
        this.rightParen = rightParen;
        this.semicn = semicn;
    }
    
    public Printf(int lineno, Token printf, Token leftParen, Token stringConst, ArrayList<Exp> exps, ArrayList<Token>commas, Token rightParen, Token semicn) {
        super(lineno, NodeType.Printf);
        this.printf = printf;
        this.leftParen = leftParen;
        this.stringConst = stringConst;
        this.exps = exps;
        this.commas = commas;
        this.rightParen = rightParen;
        this.semicn = semicn;
    }
    
    public String getStringConst() {
        return stringConst.getContent();
    }
    
    public ArrayList<Exp> getExps() {
        return exps;
    }
    
    public void print() {
        printf.print();
        leftParen.print();
        stringConst.print();
        if (exps != null) {
            for (int i = 0;i<exps.size();i++) {
                commas.get(i).print();
                exps.get(i).print();
            }
        }
        rightParen.print();
        semicn.print();
    }
}
