package IR.Instr;

import IR.Type.IntType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class CalcInstr extends Instruction {

    public CalcInstr(Operator op, String name, Value a, Value b, BasicBlock bb) {
        super(IntType.I32, op, bb, name);
        addArgument(a);
        addArgument(b);
    }

    @Override
    public String toString() {
        //%4 = sub nsw i32 0, %3
        return getName() + " = " + getOp().toString() + " i32 "
                + getArgument(0).getName() + ", " + getArgument(1).getName();
    }
}
