package IR.Instr;

import IR.Type.ArrayType;
import IR.Type.IRType;
import IR.Type.IntType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;

import java.util.ArrayList;

public class PhiInstr extends Instruction {
    private ArrayList<Instruction> operands;

    public PhiInstr(String name, IRType type, ArrayList<Instruction> op, BasicBlock bb) {
        super(type, Operator.PHI, bb, name);
        this.operands = op;
    }

    public void setOperands(ArrayList<Instruction> operands) {
        this.operands = operands;
    }

    public void addOperands(Instruction op) {
        this.operands.add(op);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(" = phi ").append(getType()).append(" ");
        for (Instruction op : operands) {
            sb.append("[").append(op.getName()).append(", ").append(op.getParentBB().getName()).append("], ");
        }
        return sb.toString();
    }
    // %indvar = phi i32 [ 0, %LoopHeader ], [ %nextindvar, %Loop ]
}
