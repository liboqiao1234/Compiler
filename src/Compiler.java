import AST.Nodes.CompUnit;
import AST.Nodes.ConstExp;
import MyError.MyError;
import MyFrontEnd.Lexer;
import MyFrontEnd.Parser;
import MyToken.Token;
import MyToken.TokenType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

public class Compiler {
    public static void main(String[] args) {
        StringBuilder source = new StringBuilder();
        try (FileInputStream fis = new FileInputStream("testfile.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = br.readLine()) != null) {
                source.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Lexer lexer = Lexer.getInstance();
        lexer.init(source.toString());
        boolean success = lexer.wordAnalyze();
        ArrayList<MyError> errors = new ArrayList<>();
        if (success) {
            ArrayList<Token> tokens = lexer.getTokens();
            try (FileOutputStream fos = new FileOutputStream("lexer.txt");
                 PrintWriter writer = new PrintWriter(fos)) {
                for (Token token : tokens)
                    writer.println(token.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errors = lexer.getErrors();
            try (FileOutputStream fos = new FileOutputStream("error.txt");
                 PrintWriter writer = new PrintWriter(fos)) {
                for (MyError error : errors)
                    writer.println(error.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        Parser parser = new Parser(lexer.getTokens());
        CompUnit root = parser.parseCompUnit();
        errors.addAll(parser.getErrors());
        Collections.sort(errors);
        // if (root != null) root.print();
        
        if (errors.isEmpty()) {
            try {
                PrintStream out = new PrintStream(new FileOutputStream("parser.txt"));
                
                System.setOut(out);
                if (root != null) {
                    root.print();
                }
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                PrintStream out = new PrintStream(new FileOutputStream("error.txt"));
                
                System.setOut(out);
                for (MyError error : errors)
                    System.out.println(error.toString());
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        
        
    }
}
