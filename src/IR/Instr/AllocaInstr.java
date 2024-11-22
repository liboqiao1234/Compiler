package IR.Instr;

import IR.Type.IRType;
import IR.Type.PointerType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;

public class AllocaInstr extends Instruction {
    // %3 = alloca i32
    // 把alloca存在符号表里，用来记录。

    /*

    * @param name
    * @param retType
    * @param bb
     */
    public AllocaInstr(String name, IRType retType, BasicBlock bb) {
        super(new PointerType(retType), Operator.ALLOCA, bb, name);// IRtype, Op, bb
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getName().charAt(0) != '%') {
            sb.append("%");
        }
        sb.append(getName()).append(" = alloca ");
        sb.append(((PointerType) getType()).getPointeeType().toString());
        return sb.toString();
    }
}
