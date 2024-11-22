package IR.Type;

import static IR.Type.IntType.I32;
import static IR.Type.IntType.I8;
import static IR.Type.IntType.VOID;

public class IRType {

    public boolean isVoid() {
        return this == VOID;
    }

    public boolean isI8() {
        return this == I8;
    }

    public boolean isI32() {
        return this == I32;
    }

    public boolean isFunc() {
        return this instanceof FunctionType;
    }

    public boolean isArray() {
        return this instanceof ArrayType;
    }

    public boolean isPointer() {
        return this instanceof PointerType;
    }

    public String toString() {
        return "default IRType";
    }
}
