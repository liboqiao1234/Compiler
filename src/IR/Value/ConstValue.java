package IR.Value;

import IR.Type.IRType;

public class ConstValue extends Value{
    public ConstValue(String name, IRType type) {
        super(name, type);
    }

//    @Override
//    public String toString() {
//        if (this instanceof ConstInt){
//            return ((ConstInt) this).getValue() + "";
//        } else if (this instanceof StringLiteral){
//            System.err.println("String Literal should not be here!");
//            return ((StringLiteral) this).getValue() + "";
//        } else if (this instanceof ConstArray) {
//            return ((ConstArray) this).toString();
//        }
//        return "Error in constValue";
//    }
}
