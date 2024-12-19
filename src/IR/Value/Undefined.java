package IR.Value;

import IR.Type.IntType;

public class Undefined extends Value{
    public Undefined() {
        super("0", IntType.I32);
    }

    @Override
    public String toString() {
        return "0";
    }
}
