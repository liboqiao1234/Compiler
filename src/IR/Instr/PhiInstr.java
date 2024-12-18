package IR.Instr;

import IR.Type.ArrayType;
import IR.Type.IRType;
import IR.Type.IntType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;

import java.util.ArrayList;

public class PhiInstr extends Instruction {
    private ArrayList<Instruction> operands;

    public PhiInstr(String name, ArrayList<Instruction> op, BasicBlock bb) {
        super(IntType.I32,Operator.PHI, bb ,name) ;
        this.operands = op;
    }

}
