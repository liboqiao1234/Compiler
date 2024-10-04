package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class Break extends Node {
    private Token _break;
    private Token semicn;
    
    public Break(int lineno, Token _break, Token semicn) {
        super(lineno, NodeType.Break);
        this._break = _break;
        this.semicn = semicn;
    }
    
    public void print() {
        _break.print();
        semicn.print();
    }
}
