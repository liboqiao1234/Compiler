package MyToken;

import MyToken.TokenType;

public class Token {
    TokenType type;
    String content;
    int lineno;
    
    public Token(TokenType type, String content, int lineno) {
        this.type = type;
        this.content = content;
        this.lineno = lineno;
    }
    
    public int getLineno() {
        return lineno;
    }
    
    public TokenType getType() {
        return type;
    }
    
    public String getContent() {
        return content;
    }
    
    public String toString() {
        return type.toString() + " " + content;
    }
}
