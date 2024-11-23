package IR.Value;

import IR.Type.IRType;
import IR.Type.IntType;

public class ConstInt extends ConstValue {
    private int value;

    public ConstInt(int value) {
        super(String.valueOf(value), IntType.I32);
        this.value = value;
    }

    public ConstInt(int value, IRType type) {
        super(String.valueOf(value), type);
        this.value = value;
    }

    public ConstInt(char ch) {
        super(String.valueOf(new Integer(ch)), IntType.I8);
        this.value = (int) ch;
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return value + "";
    }
}
