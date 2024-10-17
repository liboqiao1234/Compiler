package Symbol;

public class Symbol {
    private String name;
    private int lineno;
    private int scope; // id or level?  temporary use id.
    private SymbolType dataType; // void, int, char;
    
    public Symbol(String name, int lineno, int scope, SymbolType dataType) {
        this.name = name;
        this.lineno = lineno;
        this.scope = scope;
        this.dataType = dataType;
    }
    
    public String getName() {
        return name;
    }
    
    public int getLineno() {
        return lineno;
    }
    
    public int getScope() {
        return scope;
    }
    
    public SymbolType getDataType() {
        return dataType;
    }
}
