package IR.Instr;

import IR.Type.IRType;
import IR.Type.PointerType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class LoadInstr extends Instruction {
    //  %2 = load i32, i32* @b
    public LoadInstr(String name, Value src1, BasicBlock bb) {
        super(((PointerType)src1.getType()).getPointeeType(), Operator.LOAD, bb, name);
        addArgument(src1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Value pointer = getArgument(0);// TODO: instruction?
        IRType type = pointer.getType();
        sb.append(getName()).append(" = load ").append(getType()).append(", ").append(type);
        sb.append(" ").append(pointer.getName());
        return sb.toString();
    }
}
