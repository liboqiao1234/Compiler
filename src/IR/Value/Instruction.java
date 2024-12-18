package IR.Value;

import IR.Instr.Operator;
import IR.Type.IRType;

import java.util.ArrayList;

public class Instruction extends User {
    private BasicBlock parentBlock;
    private Operator op;
    private ArrayList<Value> arguments = new ArrayList<>();

    public Instruction(IRType type, Operator op, BasicBlock parentBlock,String name) {
        super(name, type);
        this.parentBlock = parentBlock;
        this.op = op;
    }

    public Instruction(IRType type, Operator op, BasicBlock parentBlock) {
        super("", type);
        this.parentBlock = parentBlock;
        this.op = op;
    }

    public void addArgument(Value arg) {
        arg.addUse(this);
        arguments.add(arg);
    }

    public Operator getOp() {
        return op;
    }

     public BasicBlock getParentBB() {
        return parentBlock;
    }

    public ArrayList<Value> getArguments() {
        return arguments;
    }

    public Value getArgument(int index) {
        if (index >= arguments.size()) {
            return null;
        }
        return arguments.get(index);
    }

}
