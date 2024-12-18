package IR.Value;

import IR.Type.ArrayType;
import IR.Type.IRType;

import java.util.ArrayList;

public class User extends Value {

    private ArrayList<Value> useList = new ArrayList<>();

    public User(String name, IRType type) {
        super(name, type);
    }



}
