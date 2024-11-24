package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

import java.util.ArrayList;

public class EqExp extends Node {
    private ArrayList<RelExp> relExps;
    private ArrayList<Token> eqOps;
    
    public EqExp(int lino, ArrayList<RelExp> relExps, ArrayList<Token> eqs) {
        super(lino, NodeType.EqExp);
        this.relExps = relExps;
        this.eqOps = eqs;
    }

    public ArrayList<Token> getEqOps() {
        return eqOps;
    }
    
    public ArrayList<RelExp> getRelExps() {
        return relExps;
    }
    
    public void print() {
        for (int i = 0; i < relExps.size(); i++) {
            relExps.get(i).print();
            super.print();
            if (i < eqOps.size()) {
                eqOps.get(i).print();
            }
        }
        
    }
}
