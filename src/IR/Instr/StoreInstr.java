package IR.Instr;

import IR.Type.IntType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class StoreInstr extends Instruction {
    // value, pointer;

    public StoreInstr(Value value, Value pointer, BasicBlock bb) {
        super(IntType.VOID, Operator.STORE, bb);
        addArgument(value);
        addArgument(pointer);
        // 存name
    }

    @Override
    public String toString() {
        // store i32 %3, i32* %1
        Value arg1 = getArgument(0);
        Value arg2 = getArgument(1);
        StringBuilder sb = new StringBuilder();
        sb.append("store ").append(arg1.getType()).append(" ").append(arg1.getName()).append(", ");
        sb.append(arg2.getType()).append(" ").append(arg2.getName());
        return sb.toString();
//        return "store " + getArgument(0).getType() + " " + ", " + getArgument(1).toString() ;
    }

}
