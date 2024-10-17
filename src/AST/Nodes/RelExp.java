package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

import java.util.ArrayList;

public class RelExp extends Node {
    private ArrayList<AddExp> addExps;
    private ArrayList<Token> relOps;
    
    public RelExp(int lineno, ArrayList<AddExp> addExps, ArrayList<Token> relOps) {
        super(lineno, NodeType.RelExp);
        this.addExps = addExps;
        this.relOps = relOps;
    }
    
    public ArrayList<AddExp> getAddExps() {
        return addExps;
    }
    
    public void print() {
        for (int i = 0; i < addExps.size(); i++) {
            addExps.get(i).print();
            super.print();
            if (i != addExps.size() - 1)
                relOps.get(i).print();
        }
        
    }
}
