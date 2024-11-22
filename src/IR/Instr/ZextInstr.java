package IR.Instr;

import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class ZextInstr extends Instruction {

    public ZextInstr(String name, Value arg1, Value toTypeVal, BasicBlock bb) {
        super(toTypeVal.getType(), Operator.TRUNC, bb, name);
        addArgument(arg1);
        addArgument(toTypeVal);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Value arg1 = getArgument(0);
        sb.append(getName()).append(" = zext ").append(arg1.getType()).append(" ").append(arg1.getName());
        sb.append(" to ").append(getType());
        return sb.toString();
    }
}
