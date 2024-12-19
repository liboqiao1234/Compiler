package IR.Instr.Terminators;

import IR.Instr.Operator;
import IR.Type.IntType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class BrInstr extends Instruction {
    /*
    br i1 <cond>, label <if true>, label <if false>
    br label <dest>   ; Unconditional branch
     */
    private BasicBlock next;
    private Value cond;
    private BasicBlock ifTrue;
    private BasicBlock ifFalse;


    public BrInstr(BasicBlock block, BasicBlock next) {
        super(IntType.VOID, Operator.BR, block);
        addArgument(next);
        this.next = next;
    }

    public BrInstr(BasicBlock block, Value cond, BasicBlock ifTrue, BasicBlock ifFalse) {
        // value should be I1
        super(IntType.VOID, Operator.BR, block);
        this.cond = cond;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
        addArgument(cond);
        addArgument(ifTrue);
        addArgument(ifFalse);
    }

    public Value getCond() {
        return cond;
    }

    public BasicBlock getNext() {
        return next;
    }

    public BasicBlock getIfTrue() {
        return ifTrue;
    }

    public BasicBlock getIfFalse() {
        return ifFalse;
    }

    public String toString() {
        if (cond == null) {
            return "br label %" + next.getName();
        } else {
            return "br i1 " + cond.getName() + ", label %" + ifTrue.getName() + ", label %" + ifFalse.getName();
        }
    }
}
