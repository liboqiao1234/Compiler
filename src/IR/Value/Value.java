package IR.Value;

import IR.Type.ArrayType;
import IR.Type.IRType;

import java.util.ArrayList;

public class Value {
    private IRType type;
    private String name;
    private int id;
    private ArrayList<User> userList;
    private static int idCounter = 0;

    public Value (String nameIR, IRType type) {
        this.type = type;
        this.name = nameIR;
        this.id = idCounter++;
        this.userList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public IRType getType() {
        return type;
    }

    public ArrayList<User> getUserList() {
        return userList;
    }

    @Override
    public String toString() {
        return type.toString() + " " + name;
    }
}
