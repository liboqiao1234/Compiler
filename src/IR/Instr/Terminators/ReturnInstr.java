package IR.Instr.Terminators;

import IR.Instr.Operator;
import IR.Type.IntType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class ReturnInstr extends Instruction {
    public ReturnInstr(Value value, BasicBlock bb) {
        super(IntType.VOID, Operator.RET, bb);
        addArgument(value);
    }

    public ReturnInstr(BasicBlock bb) {
        super(IntType.VOID, Operator.RET, bb);
    }

    @Override
    public String toString() {
        if (getArguments().size() == 0) {
            return "ret void";
        } else {
            Value res = getArgument(0);
            return "ret " + res.getType() + " " + res.getName();
        }
    }
}
