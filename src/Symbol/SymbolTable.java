package Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class SymbolTable<T> {
    private boolean hasReturn; // if it is a func: check error type 'g';
    private int id; // symbol table ID, not level!
    private LinkedHashMap<String, T> table;
    private SymbolTable<T> father;
    private ArrayList<SymbolTable<T>> sons;
    
    public SymbolTable(SymbolTable<T> fa, int id) {
        father = fa;
        this.id = id;
        table = new LinkedHashMap<String, T>();
        sons = new ArrayList<SymbolTable<T>>();
        hasReturn = false;
    }
    
    public SymbolTable(SymbolTable<T> fa, boolean hasReturn, int id) {
        father = fa;
        this.id = id;
        table = new LinkedHashMap<String, T>();
        sons = new ArrayList<SymbolTable<T>>();
        this.hasReturn = hasReturn;
    }
    
    public void insert(String name, T symbol) {
        /*if (contains(name)) {
            System.err.println("Error: Duplicate declaration of " + name + " at line " + symbol.getLineno());
            return;
        }*/
        // System.out.println("[DEBUG] SymTable ID " + id + ", Insert " + name + " as " + symbol.getDataType().toString());
        table.put(name, symbol);
    }
    
    public T find(String name) {
        if (contains(name)) {
            return table.get(name);
        }
        if (father != null) {
            return father.find(name);
        }
        return null;
    }
    
    public void remove(String name) {
        table.remove(name);
    }
    
    public boolean contains(String name) {
        return table.containsKey(name);
    }
    
    public SymbolTable<T> getFather() {
        return father;
    }
    
    public SymbolTable<T> createSon(boolean hasReturn, int id) {
        SymbolTable<T> son = new SymbolTable<T>(this, hasReturn, id);
        sons.add(son);
        return son;
    }

    public SymbolTable<T> createSon() {
        SymbolTable<T> son = new SymbolTable<T>(this, false, 0);
        sons.add(son);
        return son;
    }
    
    public void print() {
        for (String name : table.keySet()) {
            System.out.println(id + " " + name + " " + ((Symbol)table.get(name)).getDataType().toString());
            /*if (table.get(name) instanceof FuncSymbol) {
                ArrayList<VarSymbol> params = ((FuncSymbol) table.get(name)).getParams();
                for (VarSymbol param : params) {
                    System.out.println(id + " " + param.getName() + " " + param.getDataType().toString());
                }
            }*/
        }
        for (SymbolTable son : sons) {
            son.print();
        }
    }
}
