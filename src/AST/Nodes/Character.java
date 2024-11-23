package AST.Nodes;

import AST.Node;
import AST.NodeType;
import MyToken.Token;

public class Character extends Node {
    private Token charConst;
    
    public Character(int lineno, Token charConst) {
        super(lineno, NodeType.Character);
        this.charConst = charConst;
    }

    public char getCharConst() {
        if (charConst.getContent().length() >3) {
            String str = charConst.getContent().substring(1, 3);
            assert str.charAt(0) == '\\';
            switch (str.charAt(1)){
                case 'n':
                    return '\n';
                case 't':
                    return '\t';
                case 'a':
                    return (char)(7);
                case 'b':
                    return (char)(8);
                case 'f':
                    return (char)(12);
                case 'v':
                    return (char)(11);
                case '"':
                    return (char)(34);
                case '\'':
                    return (char)(39);
                case '\\':
                    return (char)(92);
                case '0':
                    return (char)(0);
                default:
                    System.err.println("error in getCharConst!");
                    return (char)-1;
            }
        } else {
            return charConst.getContent().charAt(1); // TODO: check
        }
    }

    public void print() {
        charConst.print();
        super.print();
    }
}
