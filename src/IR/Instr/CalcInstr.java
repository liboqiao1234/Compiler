package IR.Instr;

import IR.Type.IntType;
import IR.Value.BasicBlock;
import IR.Value.ConstInt;
import IR.Value.Instruction;
import IR.Value.Use;
import IR.Value.Value;

public class CalcInstr extends Instruction {

    public CalcInstr(Operator op, String name, Value a, Value b, BasicBlock bb) {
        super(IntType.I32, op, bb, name);
        addArgument(a);
        addArgument(b);
    }

    public void calcOptimize() {
        if (getArguments().size() == 2) {
            if (!(getArgument(0) instanceof ConstInt || getArgument(1) instanceof ConstInt)) {
                return;
            }
            if (getArgument(0) instanceof ConstInt && getArgument(1) instanceof ConstInt) {
                ConstInt constInt1 = (ConstInt) getArgument(0);
                ConstInt constInt2 = (ConstInt) getArgument(1);
                int result = 0;
                switch (getOp().toString()) {
                    case "add":
                        result = constInt1.getValue() + constInt2.getValue();
                        break;
                    case "sub":
                        result = constInt1.getValue() - constInt2.getValue();
                        break;
                    case "mul":
                        result = constInt1.getValue() * constInt2.getValue();
                        break;
                    case "sdiv":
                        result = constInt1.getValue() / constInt2.getValue();
                        break;
                    case "srem":
                        result = constInt1.getValue() % constInt2.getValue();
                        break;
                    default:
                        break;
                }

                replaceLoadValue(this, new ConstInt(result));
            } else if (getArgument(0) instanceof ConstInt) {
                ConstInt constInt = (ConstInt) getArgument(0);
                if (constInt.getValue() == 0) {
                    if (getOp().toString().equals("add")) {
                        replaceLoadValue(this, getArgument(1));
                    } else if (getOp().toString().equals("mul") ||
                            getOp().toString().equals("sdiv") ||
                            getOp().toString().equals("srem")) {
                        replaceLoadValue(this, new ConstInt(0));
                    }
                } else if (constInt.getValue() == 1) {
                    if (getOp().toString().equals("mul")) {
                        replaceLoadValue(this, getArgument(1));
                    }
                }
            } else if (getArgument(1) instanceof ConstInt) {
                ConstInt constInt = (ConstInt) getArgument(1);
                if (constInt.getValue() == 0) {
                    if (getOp().toString().equals("add") || getOp().toString().equals("sub")) {
                        replaceLoadValue(this, getArgument(0));
                    } else if (getOp().toString().equals("mul")) {
                        replaceLoadValue(this, new ConstInt(0));
                    }
                } else if (constInt.getValue() == 1) {
                    if (getOp().toString().equals("mul") || getOp().toString().equals("sdiv") ) {
                        replaceLoadValue(this, getArgument(0));
                    } else if (getOp().toString().equals("srem")) {
                        replaceLoadValue(this, new ConstInt(0));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        //%4 = sub nsw i32 0, %3
        return getName() + " = " + getOp().toString() + " i32 "
                + getArgument(0).getName() + ", " + getArgument(1).getName();
    }
}
