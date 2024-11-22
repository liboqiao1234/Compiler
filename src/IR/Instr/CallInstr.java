package IR.Instr;

import IR.Type.FunctionType;
import IR.Type.IRType;
import IR.Value.BasicBlock;
import IR.Value.Function;
import IR.Value.Instruction;
import IR.Value.Value;

import java.util.ArrayList;

public class CallInstr extends Instruction {

    public CallInstr(String name, Function function, BasicBlock bb, ArrayList<Value> args) {
        // name是返回值名字，也是这个的名字；
        super(((FunctionType)function.getType()).getRetType(), Operator.CALL, bb, name);
        addArgument(function);
        if (args != null) {
            for (Value arg : args) {
                addArgument(arg);
            }
        }
    }

    public CallInstr(Function function, BasicBlock bb, ArrayList<Value> args) {
        // name是返回值名字，也是这个的名字；
        super(((FunctionType)function.getType()).getRetType(), Operator.CALL, bb, "");
        addArgument(function);
        if (args != null && !args.isEmpty()) {
            for (Value arg : args) {
                addArgument(arg);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!getName().isEmpty()) {
            sb.append(getName()).append(" = ");
        }
        FunctionType type = (FunctionType) getArgument(0).getType();
        IRType retType = type.getRetType();
        sb.append("call ").append(retType).append(" ").append(getArgument(0).getName()).append("(");
        ArrayList<Value> args = getArguments();
        if (args.size() > 1) {
            for (int i = 1; i < args.size() - 1; i++) {
                sb.append(args.get(i).getType()).append(" ").append(args.get(i).getName()).append(", ");
            }
            sb.append(args.get(args.size() - 1).getType()).append(" ").append(args.get(args.size() - 1).getName());
        }
        sb.append(")");
        return sb.toString();
    }
}
