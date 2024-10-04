package MyFrontEnd;

import MyToken.Token;
import MyToken.TokenType;
import MyError.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private static final Lexer instance = new Lexer();
    private int curPos;
    private String source;
    private int sourceLen;
    private String curContent;
    private int curLineno;
    private HashMap<String, TokenType> reserved = new HashMap<String, TokenType>();
    private HashMap<Character, TokenType> singleChar = new HashMap<Character, TokenType>();
    private ArrayList<Token> tokens = new ArrayList<Token>();
    private ArrayList<MyError> errors = new ArrayList<MyError>();
    
    private Lexer() {
    
    }
    
    public void init(String source) {
        this.source = source;
        this.curPos = 0;
        this.curLineno = 1;
        this.sourceLen = source.length();
        reserved.put("main", TokenType.MAINTK);
        reserved.put("const", TokenType.CONSTTK);
        reserved.put("int", TokenType.INTTK);
        reserved.put("char", TokenType.CHARTK);
        reserved.put("break", TokenType.BREAKTK);
        reserved.put("continue", TokenType.CONTINUETK);
        reserved.put("if", TokenType.IFTK);
        reserved.put("else", TokenType.ELSETK);
        reserved.put("for", TokenType.FORTK);
        reserved.put("getint", TokenType.GETINTTK);
        reserved.put("getchar", TokenType.GETCHARTK);
        reserved.put("printf", TokenType.PRINTFTK);
        reserved.put("return", TokenType.RETURNTK);
        reserved.put("void", TokenType.VOIDTK);
        singleChar.put('+', TokenType.PLUS);
        singleChar.put('-', TokenType.MINU);
        singleChar.put('*', TokenType.MULT);
        singleChar.put('/', TokenType.DIV);
        singleChar.put('%', TokenType.MOD);
        singleChar.put(';', TokenType.SEMICN);
        singleChar.put(',', TokenType.COMMA);
        singleChar.put('(', TokenType.LPARENT);
        singleChar.put(')', TokenType.RPARENT);
        singleChar.put('[', TokenType.LBRACK);
        singleChar.put(']', TokenType.RBRACK);
        singleChar.put('{', TokenType.LBRACE);
        singleChar.put('}', TokenType.RBRACE);
    }
    
    private boolean charCheck(char c) {
        return (c >= 32 && c <= 126 || c >= 7 && c <= 12 || c == 34 || c == 39 || c == 0) && c != 92;
    }
    
    public static Lexer getInstance() {
        return instance;
    }
    
    public Token next() {
        if (source == null) {
            return new Token(TokenType.ERROR, "Source is null", 0);
        }
        
        while (curPos < sourceLen && Character.isWhitespace(source.charAt(curPos))) {
            if (source.charAt(curPos) == '\n') {
                curLineno++;
            }
            curPos++;
        }
        
        if (curPos >= sourceLen) {
            return new Token(TokenType.EOF, "", 0);
        }
        
        curContent = "";
        char c = source.charAt(curPos);
        if (Character.isLetter(c) || c == '_') {
            curContent += c;
            curPos++;
            c = source.charAt(curPos);
            while (curPos < sourceLen && (Character.isLetterOrDigit(c) || c == '_')) {
                curContent += c;
                curPos++;
                c = source.charAt(curPos);
            }
            Token res = new Token(reserved.getOrDefault(curContent, TokenType.IDENFR), curContent, curLineno);
            tokens.add(res);
            return res;
        } else if (Character.isDigit(c)) {
            curContent += c;
            curPos++;
            c = source.charAt(curPos);
            while (curPos < sourceLen && Character.isDigit(c)) {
                curContent += c;
                curPos++;
                c = source.charAt(curPos);
            }
            Token res = new Token(TokenType.INTCON, curContent, curLineno);
            tokens.add(res);
            return res;
        } else if (c == '/') {
            curContent += c;
            curPos++;
            if (curPos < sourceLen && source.charAt(curPos) == '/') {
                c = source.charAt(curPos);
                while (curPos < sourceLen && c != '\n') {
                    curPos++;
                    c = source.charAt(curPos);
                }
                return next();
            } else if (curPos < sourceLen && source.charAt(curPos) == '*') {
                c = source.charAt(curPos);
                while (curPos < sourceLen && !(c == '*' && source.charAt(curPos + 1) == '/')) {
                    curPos++;
                    c = source.charAt(curPos);
                }
                if ((c == '*' && source.charAt(curPos + 1) == '/')) {
                    curPos += 2;
                }
                return next();
            } else {
                Token res = new Token(TokenType.DIV, curContent, curLineno);
                tokens.add(res);
                return res;
            }
        } else if (singleChar.containsKey(c)) {
            curContent += c;
            curPos++;
            Token res = new Token(singleChar.get(c), curContent, curLineno);
            tokens.add(res);
            return res;
        } else if (c == '<') {
            curContent += c;
            curPos++;
            if (curPos < sourceLen && source.charAt(curPos) == '=') {
                curContent += '=';
                curPos++;
                Token res = new Token(TokenType.LEQ, curContent, curLineno);
                tokens.add(res);
                return res;
            } else {
                Token res = new Token(TokenType.LSS, curContent, curLineno);
                tokens.add(res);
                return res;
            }
        } else if (c == '>') {
            curContent += c;
            curPos++;
            if (curPos < sourceLen && source.charAt(curPos) == '=') {
                curContent += '=';
                curPos++;
                Token res = new Token(TokenType.GEQ, curContent, curLineno);
                tokens.add(res);
                return res;
            } else {
                Token res = new Token(TokenType.GRE, curContent, curLineno);
                tokens.add(res);
                return res;
            }
        } else if (c == '=') {
            curContent += c;
            curPos++;
            if (curPos < sourceLen && source.charAt(curPos) == '=') {
                curContent += '=';
                curPos++;
                Token res = new Token(TokenType.EQL, curContent, curLineno);
                tokens.add(res);
                return res;
            } else {
                Token res = new Token(TokenType.ASSIGN, curContent, curLineno);
                tokens.add(res);
                return res;
            }
        } else if (c == '!') {
            curContent += c;
            curPos++;
            if (curPos < sourceLen && source.charAt(curPos) == '=') {
                curContent += '=';
                curPos++;
                Token res = new Token(TokenType.NEQ, curContent, curLineno);
                tokens.add(res);
                return res;
            } else {
                Token res = new Token(TokenType.NOT, curContent, curLineno);
                tokens.add(res);
                return res;
            }
        } else if (c == '&') {
            curContent += c;
            curPos++;
            if (curPos < sourceLen && source.charAt(curPos) == '&') {
                curContent += '&';
                curPos++;
                Token res = new Token(TokenType.AND, curContent, curLineno);
                tokens.add(res);
                return res;
            } else {
                MyError error = new MyError(curLineno, MyErrorType.a);
                errors.add(error);
                Token res = new Token(TokenType.BADAND, curContent, curLineno);
                tokens.add(res);
                return res;
            }
        } else if (c == '|') {
            curContent += c;
            curPos++;
            if (curPos < sourceLen && source.charAt(curPos) == '|') {
                curContent += '|';
                curPos++;
                Token res = new Token(TokenType.OR, curContent, curLineno);
                tokens.add(res);
                return res;
            } else {
                MyError error = new MyError(curLineno, MyErrorType.a);
                errors.add(error);
                Token res = new Token(TokenType.BADOR, curContent, curLineno);
                tokens.add(res);
                return res;
            }
        } else if (c == '\'') {
            curContent += c;
            curPos++;
            if (curPos + 1 < sourceLen && (charCheck(source.charAt(curPos)))
                    && source.charAt(curPos + 1) == '\'') {
                curContent += source.charAt(curPos);
                curContent += '\'';
                curPos += 2;
                Token res = new Token(TokenType.CHRCON, curContent, curLineno);
                tokens.add(res);
                return res;
            } else if (curPos + 2 < sourceLen && (source.charAt(curPos) == '\\')) {
                curContent += source.charAt(curPos);
                curContent += source.charAt(curPos + 1);
                curContent += source.charAt(curPos + 2);
                curPos += 3;
                Token res = new Token(TokenType.CHRCON, curContent, curLineno);
                tokens.add(res);
                return res;
            }
        } else if (c == '"') {
            curContent += c;
            curPos++;
            while (curPos < sourceLen && source.charAt(curPos) != '"') {
                curContent += source.charAt(curPos);
                curPos++;
            }
            if (curPos < sourceLen && source.charAt(curPos) == '"') {
                curContent += '"';
                curPos++;
                Token res = new Token(TokenType.STRCON, curContent, curLineno);
                tokens.add(res);
                return res;
            }
        }
        return new Token(TokenType.ERROR, curContent, curLineno);
    }
    
    public boolean hasNext() {
        return curPos < sourceLen;
    }
    
    public ArrayList<Token> getTokens() {
        return tokens;
    }
    
    public ArrayList<MyError> getErrors() {
        return errors;
    }
    
    public boolean wordAnalyze() {
        while (hasNext()) {
            next();
        }
        return errors.isEmpty();
    }
}
