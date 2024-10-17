package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

import java.util.ArrayList;

public class FuncFParams extends Node {
    private ArrayList<FuncFParam> funcFParams;
    private ArrayList<Token> commas;
    
    public FuncFParams(int lino, ArrayList<FuncFParam> funcFParams, ArrayList<Token> commas) {
        super(lino, NodeType.FuncFParams);
        this.funcFParams = funcFParams;
        this.commas = commas;
    }
    
    public ArrayList<FuncFParam> getFuncFParams() {
        return funcFParams;
    }
    
    public void print() {
        for (int i = 0; i < funcFParams.size(); i++) {
            funcFParams.get(i).print();
            if (i != funcFParams.size() - 1) {
                commas.get(i).print();
            }
        }
        super.print();
    }
}
