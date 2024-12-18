package IR.Value;

import AST.Nodes.Block;
import IR.Type.ArrayType;
import IR.Type.FunctionType;
import IR.Type.IRType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Function extends Value {
    // 约定包括函数参数在内的第一个基本块（因为不需要名）由函数创建，但是参数的alloca & store不由函数完成（因为要放到符号表）
    private boolean isLibFunc;
    private ArrayList<IRType> paraList;
    private ArrayList<BasicBlock> blocks = new ArrayList<>();


    private int dfsCounter;
    private HashMap<BasicBlock, Integer> dfsno = new HashMap<>();
    private HashMap<Integer, BasicBlock> blockMap = new HashMap<>();
    private HashMap<BasicBlock, Boolean> visited = new HashMap<>();
    private HashMap<BasicBlock, HashSet<BasicBlock>> dom = new HashMap<>();

    private void visitBlocks(BasicBlock now) {
        visited.put(now, true);
        for (BasicBlock block : now.getSuccessors()) {
            if (!visited.get(block)) {
                visitBlocks(block);
            }
        }
        blockMap.put(dfsCounter, now);
        dfsno.put(now, dfsCounter--);
    }

    private void visitBlockForReachable(BasicBlock now) {
        visited.put(now, true);
        for (BasicBlock block : now.getSuccessors()) {
            if (!visited.get(block)) {
                visitBlockForReachable(block);
            }
        }
    }

    private void deleteUnreachableAndEmpty(){
        for (BasicBlock block : blocks) {
            visited.put(block, false);
        }
        visited.put(blocks.get(0), true);
        visitBlockForReachable(blocks.get(0));


        blocks.removeIf(block -> {
            boolean isUnreachable = !visited.get(block);
            if (isUnreachable) {
                //System.err.println("remove unreachable " + block.getName() + " in function:" + getName());
                for (BasicBlock bb:blocks) {
                    bb.removeIfExist(block);
                }
            }
            return isUnreachable;
        });
    }

    public void buildDom() {
        // 删除不可达和空
        //blocks = blocks.stream().filter(block -> !block.getInstructions().isEmpty()).collect(Collectors.toCollection(ArrayList::new));
        deleteUnreachableAndEmpty();
        // 需要重新构造一次前缀后缀
//        for (BasicBlock block : blocks) {
//            if (!visited.get(block)) {
//                //blocks.remove(block);
//                System.err.println("remove unreachable " + block.getName());
//            }
//        }



        //TODO: check

        // 构造逆后续
        dfsCounter = blocks.size();
        for (BasicBlock block : blocks) {
            visited.put(block, false);
        }
        visitBlocks(blocks.get(0));

        // 按照逆后续计算dom
        dom.put(blocks.get(0), new HashSet<>());
        dom.get(blocks.get(0)).add(blocks.get(0));
        for (BasicBlock block : blocks) {
            // 对不是firstblock的所有block，把dom设置为所有block
            if (block != blocks.get(0)) {
                dom.put(block, new HashSet<>(blocks));
            }
        }
        //初始化完成，开始计算

        boolean changed = true;
        while (changed) {
            changed = false;
            // 按照逆后续更新
            for (int i = 1; i <= blocks.size(); i++) {
                BasicBlock now = blockMap.get(i);
                if (now == blocks.get(0)) {
                    continue;
                }
                HashSet<BasicBlock> newDom = new HashSet<>(blocks);
                for (BasicBlock block : now.getPredecessors()) {
//                    System.err.println(block.getName()+":");
//                    System.err.println(dom.get(block));
                    newDom.retainAll(dom.get(block));
                }
                newDom.add(now);
                if (!newDom.equals(dom.get(now))) {
                    dom.put(now, newDom);
                    changed = true;
//                    System.err.println(now.getName() + " :");
//                    for (BasicBlock j :dom.get(now)) {
//                        System.err.println( " " + j.getName());
//                    }
//                    System.err.println("\n");
                }
            }
        }

        for (int i = 2; i < blocks.size(); i++) {
            BasicBlock now = blockMap.get(i);
            for (BasicBlock block : dom.get(now)) {
                HashSet<BasicBlock> tmp = new HashSet<>(dom.get(now));
                tmp.removeAll(dom.get(block));// TODO:逻辑不对
                if (tmp.size() == 1 && tmp.contains(now)) {
                    now.setiDomParent(block);
                    break;
                }
            }
        }

//        for (int i = 2;i<blocks.size();i++) {
//            System.err.println(blockMap.get(i).getName() + " "
//                    + blockMap.get(i).getiDomParent().getName());
//        }
//
//        System.err.println("Dom tree:");
//        for (BasicBlock block : blocks) {
//            System.err.println(block.getName() + " dom: " + dom.get(block).stream().map(BasicBlock::getName).collect(Collectors.joining(", ")));
//        }
    }

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
        if (block != getFirstBlock()) {
            blocks.add(block);
        }
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
            for (int i = 0; i < paraList.size(); i++) {
                sb.append(paraList.get(i).toString()).append(" %").append(i);
                if (i != paraList.size() - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append(") {\n");
        for (BasicBlock block : blocks) {
            sb.append(block.toString());
        }

        sb.append("}");
        return sb.toString();
    }
}
