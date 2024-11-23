package IR.Value;

import IR.Type.ArrayType;
import IR.Type.IRType;

public class Zeroinitializer extends ConstValue{
    public Zeroinitializer(ArrayType type) {
        super("", type);
    }

    @Override
    public String toString() {
        return "zeroinitializer";
    }
}
