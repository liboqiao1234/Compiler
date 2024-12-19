package IR.Instr;

import IR.Type.ArrayType;
import IR.Type.IRType;
import IR.Type.IntType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Undefined;
import IR.Value.Value;

import java.util.ArrayList;

public class PhiInstr extends Instruction {
    private ArrayList<Value> operands;
    private ArrayList<BasicBlock> opBBs = new ArrayList<>();

    public PhiInstr(String name, IRType type, ArrayList<Value> op, BasicBlock bb) {
        super(type, Operator.PHI, bb, name);
        this.operands = op;
    }

    public void setOperands(ArrayList<Value> operands) {
        this.operands = operands;
    }

    public void addOperands(Value op, BasicBlock bb) {
        this.operands.add(op);
//        op.addUse(this);
//        bb.addUse(this);
        addArgument(op);
        addArgument(bb);
        if (!(op instanceof Instruction)) {
            opBBs.add(bb);
        } else {
            assert bb == ((Instruction) op).getParentBB();
            opBBs.add(bb);
        }
    }

//    public void addOperands(Value op) {
//        this.operands.add(op);
//        assert (op instanceof Instruction);
//        opBBs.add(((Instruction) op).getParentBB());
//    }

    @Override
    public String toString() {
//        for(Value op: operands){
//            if (op instanceof Undefined) {
//                return "";
//            }
//        }
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(" = phi ").append(getType()).append(" ");
        for (int i = 0; i < operands.size(); i++) {
            Value op = operands.get(i);
            if (op instanceof Undefined) {
                sb.append("[").append(0).append(", ");
            } else {
                sb.append("[").append(op.getName()).append(", ");
            }
            sb.append("%" + opBBs.get(i).getName()).append("]");
//            if (!(op instanceof Instruction)) {
//                sb.append("null").append("]");
//            } else {
//                sb.append("%").append(((Instruction) op).getParentBB().getName()).append("]");
//            }
            if (i != operands.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
    // %indvar = phi i32 [ 0, %LoopHeader ], [ %nextindvar, %Loop ]
}
