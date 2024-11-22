package IR.Value;

import IR.Type.ArrayType;
import IR.Type.IntType;
import IR.Type.PointerType;

public class StringLiteral extends ConstValue {
    private final String value;
    private static int count = 0;

    public StringLiteral(String value) {
        super("@str." + (++count), new PointerType(new ArrayType(IntType.I8, value.length() + 1)));
        // TODO: check: value转换为IR stringLit格式;
        this.value = value.replace("\n","\\0a") + "\\00";
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        // @.str = private unnamed_addr constant [13 x i8] c"hello world\0A\00"
        int length = ((ArrayType)((PointerType)getType()).getPointeeType()).getLength();
        return getName() + " = unnamed_addr constant [" + length + " x i8] c\"" + value + "\"";
    }
}
