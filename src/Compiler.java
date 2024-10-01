import MyError.MyError;
import MyFrontEnd.Lexer;
import MyToken.Token;
import MyToken.TokenType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        String source = "";
        try (FileInputStream fis = new FileInputStream("testfile.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = br.readLine()) != null) {
                source += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Lexer lexer = Lexer.getInstance();
        lexer.init(source);
        boolean success = lexer.wordAnalyze();
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
            ArrayList<MyError> errors = lexer.getErrors();
            try (FileOutputStream fos = new FileOutputStream("error.txt");
                 PrintWriter writer = new PrintWriter(fos)) {
                for (MyError error : errors)
                    writer.println(error.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
