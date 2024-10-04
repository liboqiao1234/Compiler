package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;
import util.Printer;

import java.util.ArrayList;

public class ConstDecl extends Node {
    private Token _const;
    private BType bType;
    private ArrayList<ConstDef> constDefs;
    private ArrayList<Token> commas;
    private Token semicn;
    
    public ConstDecl(int lineno, Token _const, BType bType, ArrayList<ConstDef> constDefs, ArrayList<Token> commas, Token semicn) {
        super(lineno, NodeType.ConstDecl);
        this._const = _const;
        this.bType = bType;
        this.constDefs = constDefs;
        this.commas = commas;
        this.semicn = semicn;
    }
    
    public void print() {
        Printer.print(_const);
        bType.print();
        for (int i = 0 ;i < constDefs.size(); i++) {
            constDefs.get(i).print();
            if (i != constDefs.size() - 1) {
                Printer.print(commas.get(i));
            }
        }
        Printer.print(semicn);
        super.print();
    }
}
