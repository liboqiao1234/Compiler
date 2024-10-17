package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;
import util.Printer;

import java.util.ArrayList;

public class AddExp extends Node {
    private ArrayList<MulExp> mulExps;
    private ArrayList<Token> addOps;
    
    public AddExp(int lineno, ArrayList<MulExp> mulExps, ArrayList<Token> addOps) {
        super(lineno, NodeType.AddExp);
        this.mulExps = mulExps;
        this.addOps = addOps;
    }
    
    public ArrayList<MulExp> getMulExps() {
        return mulExps;
    }
    
    public ArrayList<Token> getAddOps() {
        return addOps;
    }
    
    public void print() {
        for (int i = 0; i < mulExps.size(); i++) {
            mulExps.get(i).print();
            super.print();
            if (i != addOps.size()) {
                Printer.print(addOps.get(i));
            }
            
        }
        
        // System.out.println("<AddExp>");
    }
}
