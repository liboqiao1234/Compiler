package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;
import util.Printer;

import java.util.ArrayList;

public class MulExp extends Node {
    private ArrayList<UnaryExp> unaryExps;
    private ArrayList<Token> operators;
    
    public MulExp(int lineno, ArrayList<UnaryExp> unaryExps, ArrayList<Token> operators) {
        super(lineno, NodeType.MulExp);
        this.unaryExps = unaryExps;
        this.operators = operators;
    }
    
    public ArrayList<UnaryExp> getUnaryExps() {
        return unaryExps;
    }
    
    public void print() {
        for (int i = 0; i < unaryExps.size(); i++) {
            unaryExps.get(i).print();
            super.print();
            if (i < operators.size()) {
                Printer.print(operators.get(i));
            }
        }
    }
}
