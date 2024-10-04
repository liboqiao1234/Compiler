package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class Return extends Node {
    private Token _return;
    private Exp exp = null;
    private Token semicn;
    
    public Return(int lineno, Token _return, Token semicn) {
        super(lineno, NodeType.Return);
        this._return = _return;
        this.semicn = semicn;
    }
    
    public Return(int lineno, Token _return, Exp exp, Token semicn) {
        super(lineno, NodeType.Return);
        this._return = _return;
        this.exp = exp;
        this.semicn = semicn;
    }
    
    public void print() {
        _return.print();
        if (exp != null) {
            exp.print();
        }
        semicn.print();
    }
}
