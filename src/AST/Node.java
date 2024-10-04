package AST;

public class Node {
    private int lineno;
    private NodeType type;
    
    public Node(int lineno, NodeType type) {
        this.lineno = lineno;
        this.type = type;
    }
    
    public int getLineno() {
        return lineno;
    }
    
    public NodeType getType() {
        return type;
    }
    
    public void print() {
        System.out.println("<" + type.toString() + ">");
    }
}
