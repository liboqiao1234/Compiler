package IR.Instr;

import IR.Type.ArrayType;
import IR.Type.IRType;
import IR.Type.PointerType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

import java.util.Objects;

public class GetelementptrInstr extends Instruction {
    // %6 = getelementptr inbounds [6 x i32], [6 x i32]* @a, i32 0, i32 4
    // %10 = getelementptr inbounds [3 x i32], [3 x i32]* %1, i32 0, i32 0
    public GetelementptrInstr(String name, Value arr, Value offset, BasicBlock bb) {
        super(new PointerType(
                ((ArrayType) (((PointerType) arr.getType()).getPointeeType())   ).getElementType()
        ), Operator.GETELEMENTPTR, bb, name);
        addArgument(arr);
        addArgument(offset);
    }

    public GetelementptrInstr(Value arr, Value offset, BasicBlock bb) {
        super(new PointerType(((PointerType) arr.getType()).getPointeeType()), Operator.GETELEMENTPTR, bb, "");
        addArgument(arr);
        addArgument(offset);
    }

    @Override
    public String toString() {
        Value array = getArgument(0);
        Value offset = getArgument(1);
        ArrayType arrayType = (ArrayType) ((PointerType) array.getType()).getPointeeType();
        IRType elementType = arrayType.getElementType();
        StringBuilder sb = new StringBuilder();
        if (!Objects.equals(getName(), "")) {
            if (getName().charAt(0) != '%') {
                sb.append("%");
            }
            sb.append(getName()).append(" = ");
        }
        // TODO: check the usage of inbound
        sb. append("getelementptr inbounds ").append(arrayType).append(", ");
        sb.append(array.getType()).append(" ").append(array.getName()).append(", ").append(elementType).append(" 0, ");
        sb.append(offset.getType()).append(" ").append(offset.getName());
        return sb.toString();
    }
}
