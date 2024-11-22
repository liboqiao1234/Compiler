package IR.Value;

import IR.Type.ArrayType;
import IR.Type.FunctionType;
import IR.Type.IRType;

import java.util.ArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Function extends Value {
    // 约定包括函数参数在内的第一个基本块（因为不需要名）由函数创建，但是参数的alloca & store不由函数完成（因为要放到符号表）
    private boolean isLibFunc;
    private ArrayList<IRType> paraList;
    private ArrayList<BasicBlock> blocks = new ArrayList<>();

    public Function(String name, IRType retType, boolean isLibFunc) { // no para
        super(name, new FunctionType(retType));
        this.isLibFunc = isLibFunc;
        blocks.add(new BasicBlock(this));
    }

    public Function(String name, IRType retType) { // no para
        super(name, new FunctionType(retType));
        this.isLibFunc = false;
        blocks.add(new BasicBlock(this));
    }

    public Function(String name, IRType retType, ArrayList<IRType> paraList, boolean isLibFunc) {
        super(name, new FunctionType(retType, paraList));
        this.paraList = paraList;
        this.isLibFunc = isLibFunc;
        blocks.add(new BasicBlock(this));
    }

    public Function(String name, IRType retType, ArrayList<IRType> paraList) {
        super(name, new FunctionType(retType, paraList));
        this.paraList = paraList;
        this.isLibFunc = false;
        blocks.add(new BasicBlock(this));
    }

    public ArrayList<IRType> getParaList() {
        return paraList;
    }

    public BasicBlock getFirstBlock() {
        return blocks.get(0);
    }

    public ArrayList<BasicBlock> getBlocks() {
        return blocks;
    }

    public void addBlock(BasicBlock block) {
        blocks.add(block);
    }

    public boolean isLibFunc() {
        return isLibFunc;
    }

    @Override
    public String toString() {
        if (isLibFunc) {
            System.err.println("LibFunc output from toString!");
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\ndefine dso_local ").append(getType().toString()).append(" ");
        sb.append(getName()).append("(");
        if (paraList != null) {
            sb.append(paraList.stream().map(IRType::toString).collect(Collectors.joining(", ")));
        }
        sb.append(") {\n");
        for (BasicBlock block : blocks) {
            sb.append(block.toString()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
