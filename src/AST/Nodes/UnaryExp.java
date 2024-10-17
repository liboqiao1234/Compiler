package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;
import util.Printer;

import java.util.ArrayList;

public class UnaryExp extends Node {
    // UnaryExp → {UnaryOp} PrimaryExp | Ident '(' [FuncRParams] ')'
    private ArrayList<UnaryOp> unaryOps;
    private PrimaryExp primaryExp;
    
    private Token ident;
    private Token leftParen;
    private FuncRParams funcRParams;
    private Token rightParen;
    
    
    public UnaryExp(int lino, PrimaryExp primaryExp, ArrayList<UnaryOp> unaryOps) {
        super(lino, NodeType.UnaryExp);
        this.primaryExp = primaryExp;
        this.unaryOps = unaryOps;
    }
    
    public UnaryExp(int lino, Token ident, Token leftParen, FuncRParams funcRParams, Token rightParen, ArrayList<UnaryOp> unaryOps) {
        super(lino, NodeType.UnaryExp);
        this.ident = ident;
        this.leftParen = leftParen;
        this.funcRParams = funcRParams; // might be null;
        this.rightParen = rightParen;
        this.unaryOps = unaryOps;
    }
    
    public PrimaryExp getPrimaryExp() {
        return primaryExp;
    }
    
    public Token getIdent() {
        return ident;
    }
    
    public FuncRParams getFuncRParams() {
        return funcRParams;
    }
    
    public void print() {
        for (UnaryOp unaryOp : unaryOps) {
            unaryOp.print();
        }
        if (primaryExp != null)
            primaryExp.print();
        else {
            Printer.print(ident);
            Printer.print(leftParen);
            if (funcRParams != null) {
                funcRParams.print();
            }
            Printer.print(rightParen);
        }
        for (int i = 0; i < unaryOps.size(); i++) super.print();
        super.print();
    }
}
