package Symbol;

public class VarSymbol extends Symbol {
    private boolean isConst;
    private boolean isArray;
    private int length; // if Array, length. -1 when array in para;
    
    
    public VarSymbol(String name, int lineno, int scope, SymbolType type) {
        super(name, lineno, scope, type);
        this.isConst = (type == SymbolType.ConstInt || type == SymbolType.ConstChar
                || type == SymbolType.ConstIntArray || type == SymbolType.ConstCharArray);
        this.isArray = (type == SymbolType.CharArray || type == SymbolType.IntArray
                || type == SymbolType.ConstCharArray || type == SymbolType.ConstIntArray);
        this.length = 0;
    }
    
    public VarSymbol(String name, int lineno, int scope, SymbolType type, int length) { // Array var
        super(name, lineno, scope, type);
        this.isConst = (type == SymbolType.ConstIntArray || type == SymbolType.ConstCharArray);
        this.isArray = true;
        this.length = length;
    }
    
    public boolean isConst() {
        return isConst;
    }
    
    public boolean isArray() {
        return isArray;
    }
}
