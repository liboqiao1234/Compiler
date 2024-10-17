package Symbol;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private ArrayList<VarSymbol> params; // only memorize the name, can find in Symbol table.
    
    public FuncSymbol(String name, int lineno, int scope, SymbolType type) {
        super(name, lineno, scope, type);
        params = new ArrayList<>();
    }
    
    public void addParam(VarSymbol param) {
        params.add(param);
    }
    
    public ArrayList<VarSymbol> getParams() {
        return params;
    }
    
    public boolean hasReturn() {
        return getDataType() != SymbolType.VoidFunc;
    }
}
