package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

import java.util.ArrayList;

public class LAndExp extends Node {
    private ArrayList<EqExp> eqExps;
    private ArrayList<Token> ands;
    
    public LAndExp(int lino, ArrayList<EqExp> eqExps, ArrayList<Token> ands) {
        super(lino, NodeType.LAndExp);
        this.eqExps = eqExps;
        this.ands = ands;
    }
    
    public ArrayList<EqExp> getEqExps() {
        return eqExps;
    }
    
    public void print() {
        for (int i = 0; i < eqExps.size(); i++) {
            eqExps.get(i).print();
            super.print();
            if (i < eqExps.size() - 1)
                ands.get(i).print();
        }
        
    }
}
