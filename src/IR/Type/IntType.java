package IR.Type;

public class IntType extends IRType {

    public static final IntType VOID = new IntType(0);
    public static final IntType I1 = new IntType(1);
    public static final IntType I8 = new IntType(8);
    public static final IntType I32 = new IntType(32);


    private int bits;

    public IntType(int bits) {
        this.bits = bits;
    }

    public int getBits() {
        return bits;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntType) {
            return ((IntType) obj).bits == bits;
        }
        return false;
    }

    @Override
    public String toString() {
        if (bits != 0)
            return "i" + bits;
        return "void";
    }
}
