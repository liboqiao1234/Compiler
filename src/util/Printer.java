package util;


import MyToken.Token;

public class Printer {
    
    private Printer() {
    
    }
    
    public static void print(Token token) {
        System.out.println(token.getType() + " " + token.getContent());
    }
}
