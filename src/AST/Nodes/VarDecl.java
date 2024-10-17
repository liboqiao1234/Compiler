package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

import java.util.ArrayList;

public class VarDecl extends Node {
    private BType bType;
    private ArrayList<VarDef> varDefs;
    private ArrayList<Token> commas;
    private Token semicn;
    
    public VarDecl(int lineno, BType bType, ArrayList<VarDef> varDefs, ArrayList<Token> commas, Token semicn) {
        super(lineno, NodeType.VarDecl);
        this.bType = bType;
        this.varDefs = varDefs;
        this.commas = commas;
        this.semicn = semicn;
    }
    
    public BType getBType() {
        return bType;
    }
    
    public ArrayList<VarDef> getVarDefs() {
        return varDefs;
    }
    
    public ArrayList<Token> getCommas() {
        return commas;
    }
    
    public Token getSemicn() {
        return semicn;
    }
    
    public void print() {
        bType.print();
        for (int i = 0; i < varDefs.size(); i++) {
            varDefs.get(i).print();
            if (i != varDefs.size() - 1) {
                commas.get(i).print();
            }
        }
        semicn.print();
        super.print();
    }
}
