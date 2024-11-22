package IR.Value;

import IR.Type.IRType;

public class Argument extends Value {
    // 尴尬，正常的Var都是直接声明的时候把分配语句放进符号表，但是参数不需要分配语句，于是有了Argument类
    // 呃呃好像其实用Value就可以，但是那也太烂了，写一个一样的增加可读性吧。
    public Argument(String name, IRType type) {
        super(name, type);
    }

    @Override
    public String toString() {
        return getType().toString() + " " + getName();
    }
}
