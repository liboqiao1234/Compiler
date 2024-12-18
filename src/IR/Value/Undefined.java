package IR.Value;

import IR.Type.IntType;

public class Undefined extends Value{
    public Undefined() {
        super("undef", IntType.VOID);
    }

    @Override
    public String toString() {
        return "undef";
    }
}
