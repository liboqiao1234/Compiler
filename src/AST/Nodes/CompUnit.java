package AST.Nodes;

import AST.Node;
import AST.NodeType;

import java.util.ArrayList;

public class CompUnit extends Node {
    private ArrayList<Decl> decls;
    private ArrayList<FuncDef> funcDefs;
    private MainFuncDef mainFuncDef;
    
    public CompUnit(int lineno, ArrayList<Decl> decls, ArrayList<FuncDef> funcDefs, MainFuncDef mainFuncDef) {
        super(lineno, NodeType.CompUnit);
        this.decls = decls;
        this.funcDefs = funcDefs;
        this.mainFuncDef = mainFuncDef;
    }
    
    public void print() {
        for (Decl decl : decls) {
            decl.print();
        }
        for (FuncDef funcDef : funcDefs) {
            funcDef.print();
        }
        mainFuncDef.print();
        super.print();
//        System.out.println("<CompUnit>");
    }
}
