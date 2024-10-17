package AST.Nodes;

import AST.Node;
import AST.NodeType;

public class Decl extends Node {
    private ConstDecl constDecl;
    private VarDecl varDecl;
    
    public Decl(int lineno, ConstDecl constDecl) {
        super(lineno, NodeType.Decl);
        this.constDecl = constDecl;
    }
    
    public Decl(int lineno, VarDecl varDecl) {
        super(lineno, NodeType.Decl);
        this.varDecl = varDecl;
    }
    
    public ConstDecl getConstDecl() {
        return constDecl;
    }
    
    public VarDecl getVarDecl() {
        return varDecl;
    }
    
    public void print() {
        
        if (constDecl != null) {
            constDecl.print();
        } else {
            varDecl.print();
        }
        // super.print();  none
    }
}
