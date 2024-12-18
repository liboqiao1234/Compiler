package Optimization;

import IR.Instr.AllocaInstr;
import IR.Instr.PhiInstr;
import IR.Instr.StoreInstr;
import IR.Type.IRType;
import IR.Type.PointerType;
import IR.Value.BasicBlock;
import IR.Value.Function;
import IR.Value.Instruction;
import IR.Value.LLVMModule;
import IR.Value.Use;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;

public class Mem2Reg {
    private LLVMModule module;
    private static int phi_counter = 0;
    private IRType nowType;
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
            instr.getParentBB().getInstructions().remove(instr);
        }
        waitForRemove.clear();
    }

    private void doInsert() {
        for (int i = 0; i < waitForInsertBB.size();i++) {
            BasicBlock bb = waitForInsertBB.get(i);
            PhiInstr phi = waitForInsertPhi.get(i);
            bb.addFirst(phi);
        }
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
                    if (instr instanceof AllocaInstr) {
                        def = new ArrayList<>();

                        if (instr.getUseList().isEmpty()) {
                            preRemove(instr);
                        }
                        for (Use use: instr.getUseList()) {
                            if (use.getUser() instanceof StoreInstr) {
                                def.add(((StoreInstr)use.getUser()).getParentBB());// TODO:check
                            }
                        }
                        nowType = ((PointerType)(instr.getType())).getPointeeType();
                        insertPhi();
                    }
                }
                doInsert();
                doRemove();
            }
        }

    }
}
