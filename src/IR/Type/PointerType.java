package IR.Type;

public class PointerType extends IRType {
    // for global variable
    private IRType pointeeType;

    public PointerType(IRType pointeeType) {
        this.pointeeType = pointeeType;
    }

    public IRType getPointeeType() {
        return pointeeType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PointerType)
            return pointeeType.equals(((PointerType) obj).getPointeeType());
        return false;
    }

    @Override
    public String toString() {
        return pointeeType.toString() + "*";
    }
}
