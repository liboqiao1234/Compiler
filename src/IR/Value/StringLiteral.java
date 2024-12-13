package IR.Value;

import IR.Type.ArrayType;
import IR.Type.IntType;
import IR.Type.PointerType;

public class StringLiteral extends ConstValue {
    private final String value;
    private final String noConvert;
    private static int count = 0;

    public StringLiteral(String value) {
        super("@str." + (++count), new PointerType(new ArrayType(IntType.I8, value.length() + 1)));
        noConvert = value;
        // TODO: check: value转换为IR stringLit格式;
        // \n 从两个长度变成了 \0A 三个长度，另外增加了3长度的\00，因此\n的数量： t = (afterlen - 3 - tmplen)，所以真正长度是afterlen - 2 - 2*t

        this.value = value.replace("\\n", "\\0A") + "\\00";
        int realLen = this.value.length() - 2 - 2 * (this.value.length() - 3 - value.length());
        updateType(new PointerType(new ArrayType(IntType.I8, realLen)));
    }

    public String getValue() {
        return value;
    }

    public String getNoConvert() {
        return noConvert;
    }

    @Override
    public String toString() {
        // @.str = private unnamed_addr constant [13 x i8] c"hello world\0A\00"
        int length = ((ArrayType)((PointerType)getType()).getPointeeType()).getLength();
        return getName() + " = unnamed_addr constant [" + length + " x i8] c\"" + value + "\"";
    }
}
