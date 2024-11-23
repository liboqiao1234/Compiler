package IR.Value;

import IR.Type.BlockType;

import java.util.ArrayList;

public class BasicBlock extends Value{
    private ArrayList<Instruction> instructions = new ArrayList<>();
    private Function function;
    private Instruction terminator;

    public BasicBlock(String name, Function function) {
        super(name, new BlockType()); // name:  "reg_num:"
        this.function = function;
    }

    public BasicBlock(Function function) {
        super("firstBlock", new BlockType()); // name:  "reg_num:"
        this.function = function;
    }

    public void addInstr(Instruction instruction) {
        instructions.add(instruction);
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public Instruction getTerminator() {
        return terminator;
    }

    public void setTerminator(Instruction terminator) {
        this.terminator = terminator;
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!getName().equals("firstBlock")) {
            sb.append(getName() + ":\n");
        }
        for (Instruction instruction : instructions) {
            sb.append("    ").append(instruction.toString()).append("\n");
        }
        return sb.toString();
    }

}
