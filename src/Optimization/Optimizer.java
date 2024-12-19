package Optimization;

import IR.Instr.AllocaInstr;
import IR.Instr.CalcInstr;
import IR.Instr.IcmpInstr;
import IR.Instr.LoadInstr;
import IR.Instr.PhiInstr;
import IR.Instr.StoreInstr;
import IR.Instr.Terminators.BrInstr;
import IR.Instr.Terminators.ReturnInstr;
import IR.Instr.TruncInstr;
import IR.Instr.ZextInstr;
import IR.Value.BasicBlock;
import IR.Value.Function;
import IR.Value.Instruction;
import IR.Value.LLVMModule;
import IR.Value.Value;

import java.util.ArrayList;
import java.util.ListIterator;

public class Optimizer {
    private LLVMModule module;

    public Optimizer(LLVMModule module) {
        this.module = module;
    }


    private void buildDom() {
        ArrayList<Function> functions = module.getFunctions();
        for (Function function : functions) {
            if (function.isLibFunc()) continue;
            function.buildDom();
        }
    }

    private boolean change;

    private boolean safeDelete(Instruction instr) {
        if (instr instanceof ReturnInstr || instr instanceof BrInstr) return false;
        return instr instanceof AllocaInstr||
                instr instanceof LoadInstr
                || instr instanceof PhiInstr || instr instanceof CalcInstr ||
                instr instanceof ZextInstr || instr instanceof IcmpInstr ||
                instr instanceof TruncInstr;
    }

    private void clearFunc(Function func) {
        ListIterator<BasicBlock> it = func.getBlocks().listIterator();
        while (it.hasNext()) {
            BasicBlock bb = it.next();
            if (bb.getUseList().isEmpty() && bb != func.getFirstBlock()) {
                for (Instruction instr : bb.getInstructions()) {
                    for (Value arg : instr.getArguments()) {
                        arg.removeUse(instr);
                    }
                }
                bb.removeSelf();
                it.remove();
                change = true;
            }
        }

        for (BasicBlock bb : func.getBlocks()) {
            ListIterator<Instruction> iter = bb.getInstructions().listIterator();
            while (iter.hasNext()) {
                Instruction instr = iter.next();
                if (instr.getUseList().isEmpty() && safeDelete(instr)) {
                    for (Value arg : instr.getArguments()) {
                        arg.removeUse(instr);
                    }
                    iter.remove();
                    change = true;
                }
            }
        }
    }

    private void DeadClear() {
        change = true;
        while (change) {
            change = false;
            module.getGlobalVariables().removeIf(variable -> variable.getUseList().isEmpty());
            module.getFunctions().removeIf(func -> func.getUseList().isEmpty() && !func.getName().equals("@main"));
            for (Function function : module.getFunctions()) {
                if (function.isLibFunc()) continue;
                clearFunc(function);
            }
        }
    }

    private void removePhiFunc(Function func){
        for(BasicBlock bb : func.getBlocks()){
            if (!(bb.getInstructions().get(0) instanceof PhiInstr))continue;

        }
    }

    private void removePhi() {
        for (Function func: module.getFunctions()) {
            if (func.isLibFunc()) continue;
            removePhiFunc(func);
        }
    }

    public void optimize() {
        DeadClear();
        IrNumClear irNumClear = new IrNumClear(module);
        irNumClear.clear();
        buildDom();
        Mem2Reg mem2Reg = new Mem2Reg(module);
        mem2Reg.run();
        DeadClear();
        irNumClear.clear();
        buildDom();
        removePhi();
        irNumClear.clear();
    }
}
