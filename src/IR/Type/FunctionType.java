package IR.Type;

import java.util.ArrayList;

public class FunctionType extends IRType {
    private ArrayList<IRType> paramTypes = null;
    private IRType retType = null;

    public FunctionType(IRType retType, ArrayList<IRType> params) {
        this.paramTypes = params;
        this.retType = retType;
    }

    public FunctionType(IRType retType) {
        this.retType = retType;
    }

    public FunctionType() {

    }

    public ArrayList<IRType> getParams() {
        return paramTypes;
    }

    public IRType getRetType() {
        return retType;
    }

    @Override
    public String toString() {
        return retType.toString();
    }
}
