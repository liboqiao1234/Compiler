package IR.Instr;

import IR.Type.IntType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class IcmpInstr extends Instruction {
    // %4 = icmp eq i32 %3, 0 返回值是I1
    Operator cmp;

    public IcmpInstr(Operator cmp, String name, Value left, Value right, BasicBlock bb) {
        super(IntType.I1, Operator.ICMP, bb);
        this.cmp = cmp;
        addArgument(left);
        addArgument(right);
    }

    public Operator getCmp() {
        return cmp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Value arg1 = getArgument(0);
        Value arg2 = getArgument(1);
        sb.append(getName()).append(" = icmp ").append(cmp.toString()).append(" ");
        sb.append(arg1.getType()).append(" ").append(arg1.getName()).append(", ");
        sb.append(arg2.getName());

        return sb.toString();
    }

}
