package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class BlockItem extends Node {
    private Decl decl;
    private Stmt stmt;
    
    public BlockItem(int lino, Decl decl) {
        super(lino, NodeType.BlockItem);
        this.decl = decl;
    }
    
    public BlockItem(int lino, Stmt stmt) {
        super(lino, NodeType.BlockItem);
        this.stmt = stmt;
    }
    
    public void print() {
        if (decl != null) {
            decl.print();
        } else {
            stmt.print();
        }
    }
}
