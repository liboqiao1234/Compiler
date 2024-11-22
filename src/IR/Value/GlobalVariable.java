package IR.Value;

import IR.Type.IRType;
import IR.Type.PointerType;

import java.awt.*;

public class GlobalVariable extends Value {
    private boolean isConst;
    private ConstValue initValue;

    public GlobalVariable(String name, ConstValue initValue, boolean isConst) {
        // const global
        super("@" + name, new PointerType(initValue.getType()));
        this.isConst = isConst;
        this.initValue = initValue;
    }

    public GlobalVariable(String name, IRType type, ConstValue initValue) {
        // var global
        super("@" + name, new PointerType(type));
        this.isConst = false;
        this.initValue = initValue;
    }

    public GlobalVariable(String name, IRType type) {
        // var global
        super("@" + name, new PointerType(type));
        this.isConst = false;
        this.initValue = null;
    }

    public boolean isConst() {
        return isConst;
    }

    public ConstValue getInitValue() {
        return initValue;
    }

    public String toString() {
        return getName();
    }
}
