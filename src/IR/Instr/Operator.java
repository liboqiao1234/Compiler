package IR.Instr;

public enum Operator {
    add, sub, mul, sdiv, srem,
    ICMP, AND, OR , eq, ne, sgt, sge, slt, sle,
    CALL,
    ALLOCA, LOAD, STORE, GETELEMENTPTR,
    PHI, RET, BR,
    ZEXT, TRUNC,

    ERROR,
}
