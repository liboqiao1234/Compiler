package Optimization;

import IR.Instr.AllocaInstr;
import IR.Instr.LoadInstr;
import IR.Instr.PhiInstr;
import IR.Instr.StoreInstr;
import IR.Type.IRType;
import IR.Type.PointerType;
import IR.Value.BasicBlock;
import IR.Value.Function;
import IR.Value.Instruction;
import IR.Value.LLVMModule;
import IR.Value.Undefined;
import IR.Value.Use;
import IR.Value.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Stack;

public class Mem2Reg {
    private LLVMModule module;
    private static int phi_counter = 0;
    private IRType nowType;
    private AllocaInstr nowAlloca;
    private ArrayList<BasicBlock> def = new ArrayList<>();
    private ArrayList<BasicBlock> waitForInsertBB = new ArrayList<>();
    private ArrayList<PhiInstr> waitForInsertPhi = new ArrayList<>();
    private ArrayList<Instruction> waitForRemove = new ArrayList<>();

    public Mem2Reg(LLVMModule module) {
        this.module = module;
    }

    private void preAdd(BasicBlock bb, PhiInstr phi) {
        waitForInsertBB.add(bb);
        waitForInsertPhi.add(phi);
    }

    private void preRemove(Instruction instr) {
        waitForRemove.add(instr);
    }

    private void doRemove() {
        for (Instruction instr : waitForRemove) {
            for (Value arg: instr.getArguments()) {
                arg.removeUse(instr);
            }
            instr.getParentBB().getInstructions().remove(instr);
        }
        waitForRemove.clear();
    }

    private void doInsert() {
        for (int i = 0; i < waitForInsertBB.size();i++) {
            BasicBlock bb = waitForInsertBB.get(i);

//            System.err.println("Insert phi at " + bb.getName());
            PhiInstr phi = waitForInsertPhi.get(i);
            bb.addFirst(phi);
        }

    }

    private void clearInsert(){
        waitForInsertBB.clear();
        waitForInsertPhi.clear();
    }

    private void insertPhi() {
//        for (BasicBlock b : def) {
//            System.err.println("def: " + b.getName());
//        }
        ArrayList<BasicBlock> F = new ArrayList<>();
        ArrayList<BasicBlock> W = new ArrayList<>(def);
        while (!W.isEmpty()) {
            BasicBlock b = W.remove(0);
            for(BasicBlock dfs : b.getDomFrontier()) {
                if (!F.contains(dfs)) {
                    F.add(dfs);
                    preAdd(dfs, new PhiInstr("%phi_" + (++phi_counter), nowType, new ArrayList<>(), dfs));
                    //b.addFirst(new PhiInstr("%phi_" + (++phi_counter), new ArrayList<>(), b));
                    //System.err.println("Add phi in " + dfs.getName());
                    if (!def.contains(dfs)) {
                        W.add(dfs);
                    }
                }
            }

        }
    }

    public void run() {
        for (Function function : module.getFunctions()) {
            if (function.isLibFunc()) continue;
            ArrayList<BasicBlock> blocks = function.getBlocks();
            for (BasicBlock block : blocks) {
                //把下面的for改成迭代器遍历

                for (Instruction instr : block.getInstructions()) {
                    if (instr instanceof AllocaInstr && !(((PointerType)instr.getType()).getPointeeType().isArray()
                    || ((PointerType)instr.getType()).getPointeeType().isPointer())) {
                        def = new ArrayList<>();

                        if (instr.getUseList().isEmpty()) {
                            preRemove(instr);
                            continue;
                        }
                        for (Use use: instr.getUseList()) {
                            if (use.getUser() instanceof StoreInstr) {
                                if (!def.contains(((StoreInstr)use.getUser()).getParentBB())) {
                                    def.add(((StoreInstr) use.getUser()).getParentBB());// TODO:check
                                }
                            }
                        }
                        nowType = ((PointerType)(instr.getType())).getPointeeType();
                        nowAlloca = (AllocaInstr) instr;
                        insertPhi();
                        stack.clear();
                        doInsert();// 写错逻辑的历史遗留，不会Concurrent
                        rename(function.getFirstBlock());
                        clearInsert();
                    }
                }
                doRemove();
            }
        }

    }

    private Stack<Value> stack = new Stack<>(); // 存储rename变量最近定义值


    private void rename(BasicBlock bb) {
//        System.err.println("Rename " + bb.getName());
//        if (bb.getName().equals("18") && bb.getFunction().getName().equals("@main")) {
//            System.err.println("8");
//        }
        // 用的话，原来全部是经由load指令，只需要换成当前最近的指令（要么是刚刚store的，要么是刚刚phi的，分别对应支配和未支配要合并）
        // 这里能用dfs，如果是store的话肯定没事，如果是phi的话，每个基本块都已经在头上插入了phi
        int stack_push = 0;
        ListIterator<Instruction> iter = bb.getInstructions().listIterator();
        while(iter.hasNext()) {
            Instruction instr = iter.next();
            if (instr instanceof LoadInstr && instr.getArgument(0) == nowAlloca ) { // && instr.getArgument(0) == nowAlloca
                // 现在不要load，把用load的那些指令的usedV 改成栈顶；
                instr.replaceLoadValue(instr, stack.empty() ? new Undefined() : stack.peek());
                preRemove(instr);
            } else if (instr instanceof StoreInstr  && instr.getArgument(1) == nowAlloca) { //  && instr.getArgument(1) == nowAlloca
                stack.push(instr.getArgument(0));
                stack_push++;
                preRemove(instr);
            } else if (instr instanceof PhiInstr && waitForInsertPhi.contains(instr)) {//TODO:check this
                stack.push(instr);
                stack_push++;
            } else if (instr instanceof AllocaInstr && instr == nowAlloca ) {
                preRemove(instr);
            }
        }

        for (BasicBlock dfs : bb.getSuccessors()) {
            Instruction instr = dfs.getInstructions().get(0);
            if (instr instanceof PhiInstr && waitForInsertPhi.contains(instr) ) {// TODO:fix
                // 确定是这一轮插入的phi，不是之前插入的
                if (stack.empty()){
                    ((PhiInstr) instr).addOperands(new Undefined(), bb);
                } else {
                    ((PhiInstr) instr).addOperands( stack.peek(), bb);
                }
            }
        }
//
        for (BasicBlock subs: bb.getiDomChildren()) {
            rename(subs);
        }

        for (int i = 0; i < stack_push; i++) {
            stack.pop();
        }
    }
}
