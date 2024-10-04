package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class Continue extends Node {
    private Token _continue;
    private Token semicn;
    
    public Continue(int lineno, Token _continue, Token semicn) {
        super(lineno, NodeType.Continue);
        this._continue = _continue;
        this.semicn = semicn;
    }
    
    @Override
    public void print() {
        _continue.print();
        semicn.print();
    }
}
