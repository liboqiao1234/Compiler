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
        this.next = next;
    }

    public BrInstr(BasicBlock block, Value cond, BasicBlock ifTrue, BasicBlock ifFalse) {
        super(IntType.VOID, Operator.BR, block);
        this.cond = cond;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    public String toString() {
        if (cond == null) {
            return "br label " + next.getName();
        } else {
            return "br i1 " + cond + ", label " + ifTrue + ", label " + ifFalse;
        }
    }
}
