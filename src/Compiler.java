import AST.Nodes.CompUnit;
import AST.Nodes.ConstExp;
import IR.IRgenerator;
import IR.Value.LLVMModule;
import MIPS.MipsGenerator;
import MIPS.MipsModule;
import MyError.MyError;
import MyFrontEnd.Lexer;
import MyFrontEnd.Parser;
import MyFrontEnd.Semanticer;
import MyToken.Token;
import MyToken.TokenType;
import Optimization.Optimizer;
import Symbol.Symbol;
import Symbol.SymbolTable;

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
import java.util.HashMap;

public class Compiler {
    private static int Optimize = 1;
    private static boolean mipsOnReg = true;

    private static void debugOutput(String name) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(name));
            System.setOut(out);
            System.out.println("test");
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

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
        }

        Parser parser = new Parser(lexer.getTokens());
        CompUnit root = parser.parseCompUnit();
        errors.addAll(parser.getErrors());
        Collections.sort(errors);


        Semanticer sm = new Semanticer(root);
        debugOutput("symbol.txt");
        debugOutput("error.txt");
        //if (true) return ;
        sm.check();
        ArrayList<MyError> semanticErrors = sm.getErrors();
        errors.addAll(semanticErrors);
        Collections.sort(errors);
        if (errors.isEmpty()) {
            SymbolTable<Symbol> table = sm.getSymbolTable();
            try {
                PrintStream out = new PrintStream(new FileOutputStream("symbol.txt"));
                System.setOut(out);
                table.print();
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
            return;
        }

        IRgenerator iRgenerator = IRgenerator.getInstance();
        iRgenerator.generateIR(root);

        LLVMModule module = iRgenerator.getModule();
        if (Optimize == 1) {
            Optimizer optimizer = new Optimizer(module);
            optimizer.optimize();
            // ↑ 原版
            // ↓ 新版
//            optimizer.optimizeNew1();
        } else if (Optimize == 2) {
            Optimizer optimizer = new Optimizer(module);
            optimizer.optimizeNew1();
        }
        String LLVM_IR = iRgenerator.output();
        try {
            PrintStream out = new PrintStream(new FileOutputStream("llvm_ir.txt"));
            System.setOut(out);
            System.out.println(LLVM_IR);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        if (true) {
//            return ;
//        }
        MipsGenerator mipsGenerator = new MipsGenerator(module);
        if (Optimize <= 1 && !mipsOnReg) {
            mipsGenerator.generate();
        } else if (Optimize == 2 || mipsOnReg) {
            mipsGenerator.generateNew1();
        }
        //mipsGenerator.generate();
        try {
            PrintStream out = new PrintStream(new FileOutputStream("mips.txt"));
            System.setOut(out);
            System.out.println(MipsModule.getInstance().toString());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        /*
        
        1. TokenType增加，Lexer中保留字字典增加，
        2. NodeType增加对应**语法成分**
        3. Node增加相关类：属性设计、print重写（如果是自定义不输出，就不在最后加super.print())、Token包含Ident和关键字
        4. 加常量的话记得在isExp()里面修改first对应的内容！
        
        
         */
    }
}
