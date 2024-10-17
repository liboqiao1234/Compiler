package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

import java.util.ArrayList;

public class LOrExp extends Node {
    private ArrayList<LAndExp> lAndExps;
    private ArrayList<Token> lOps;
    
    public LOrExp(int lino, ArrayList<LAndExp> lAndExps, ArrayList<Token> lOps) {
        super(lino, NodeType.LOrExp);
        this.lAndExps = lAndExps;
        this.lOps = lOps;
    }
    
    public ArrayList<LAndExp> getLAndExps() {
        return lAndExps;
    }
    
    public void print() {
        for (int i = 0;i<lAndExps.size();i++) {
            lAndExps.get(i).print();
            super.print();
            if (i != lAndExps.size() - 1)
                lOps.get(i).print();
        }
        
    }
}
