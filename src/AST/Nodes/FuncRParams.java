package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

import java.util.ArrayList;

public class FuncRParams extends Node {
    private ArrayList<Exp> exps;
    private ArrayList<Token> commas;
    
    public FuncRParams(int lino, ArrayList<Exp> exps, ArrayList<Token> commas) {
        super(lino, NodeType.FuncRParams);
        this.exps = exps;
        this.commas = commas;
    }
    
    public void print() {
        for(int i = 0; i < exps.size(); i++) {
            exps.get(i).print();
            if(i != exps.size() - 1)
                commas.get(i).print();
        }
        super.print();
    }
}
