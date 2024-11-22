package IR.Type;

public class ArrayType extends IRType {
    private IRType elementType;
    private int length;

    public ArrayType(IRType elementType, int length) {
        this.elementType = elementType;
        this.length = length;
    }

    public ArrayType(IRType elementType) {
        this.elementType = elementType;
        this.length = 0; // int a[]
    }

    public int getLength() {
        return length;
    }

    public IRType getElementType() {
        return elementType;
    }

    @Override
    public String toString() {
        return "[" + length + " x " + elementType.toString() + "]";
    }
}
