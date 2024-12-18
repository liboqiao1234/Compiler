package IR.Value;

import IR.Instr.Terminators.BrInstr;
import IR.Instr.Terminators.ReturnInstr;
import IR.Type.BlockType;
import MIPS.Instr.J;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private ArrayList<Instruction> instructions = new ArrayList<>();
    private Function function;
    private Instruction terminator;
    private ArrayList<BasicBlock> predecessors = new ArrayList<>();
    private ArrayList<BasicBlock> successors = new ArrayList<>();
    private BasicBlock iDomParent;

    public void setiDomParent (BasicBlock iDomParent) {
        this.iDomParent = iDomParent;
    }

    public BasicBlock getiDomParent() {
        return iDomParent;
    }


    public BasicBlock(String name, Function function) {
        super(name, new BlockType()); // name:  "reg_num:"
        this.function = function;
    }

    public BasicBlock(Function function) {
        super("firstBlock", new BlockType()); // name:  "reg_num:"
        this.function = function;
    }

    public boolean addInstr(Instruction instruction) {
        if (terminator == null) {
            instructions.add(instruction);
            return true;
        }
        return false;
    }

    public ArrayList<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public ArrayList<BasicBlock> getSuccessors() {
        return successors;
    }

    public void addPre(BasicBlock pre) {
        predecessors.add(pre);
    }

    public void addSucc(BasicBlock succ) {
        successors.add(succ);
    }

    public void removeIfExist(BasicBlock block) {
        if (predecessors.contains(block)) {
            predecessors.remove(block);
        }
        if (successors.contains(block)) {
            successors.remove(block);
        }
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public Instruction getTerminator() {
        return terminator;
    }

    public void setTerminator(Instruction terminator) {
        this.terminator = terminator;
        if (terminator instanceof BrInstr) {
            BrInstr br = (BrInstr) terminator;
            if (br.getIfTrue() != null) {
                // conditional
                successors.add(br.getIfTrue());
                successors.add(br.getIfFalse());
                br.getIfTrue().addPre(this);
                br.getIfFalse().addPre(this);
            } else {
                // unconditional
                successors.add(br.getNext());
                br.getNext().addPre(this);
            }
        }
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
