package IR.Value;

import IR.Type.FunctionType;
import IR.Type.PointerType;

import java.util.ArrayList;

public class LLVMModule {
    private ArrayList<GlobalVariable> globalVariables;
    private ArrayList<StringLiteral> stringLiterals;
    private ArrayList<Function> functions;
    private ArrayList<Function> declareFunctions;

    public LLVMModule() {
        this.globalVariables = new ArrayList<>();
        this.stringLiterals = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.declareFunctions = new ArrayList<>();
    }

    public void addDeclare(Function function) {
        declareFunctions.add(function);
    }

    public void addGlobalVariable(GlobalVariable globalVariable) {
        this.globalVariables.add(globalVariable);
    }

    public void addStringLiteral(StringLiteral stringLiteral) {
        this.stringLiterals.add(stringLiteral);
    }

    public void addFunction(Function function) {
        this.functions.add(function);
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public ArrayList<GlobalVariable> getGlobalVariables() {
        return globalVariables;
    }

    public ArrayList<StringLiteral> getStringLiterals() {
        return stringLiterals;
    }

    public ArrayList<Function> getDeclareFunctions() {
        return declareFunctions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Function function : declareFunctions) {
            sb.append("declare ").append(((FunctionType)function.getType()).getRetType()).append(" ");
            sb.append(function.getName()).append("(");
            if (function.getParaList() != null) {
                for (int i = 0; i < function.getParaList().size(); i++) {
                    sb.append(function.getParaList().get(i));
                    if (i != function.getParaList().size() - 1) {
                        sb.append(", ");
                    }
                }
            }
            sb.append(")\n");
        }
        sb.append("\n");

        for (GlobalVariable globalVariable : globalVariables) {
            sb.append(globalVariable.getName()).append(" = dso_local global ");
            sb.append(((PointerType)globalVariable.getType()).getPointeeType().toString()).append(" ");
            if (globalVariable.getInitValue() != null) {
                sb.append(globalVariable.getInitValue().toString());
            }
            sb.append("\n");
        }

        for (StringLiteral stringLiteral : stringLiterals) {
            sb.append(stringLiteral.toString()).append("\n");
        }

        for (Function function : functions) {// TODO: wait for finish.
            sb.append(function.toString()).append("\n");
        }

        return sb.toString();
    }

}
