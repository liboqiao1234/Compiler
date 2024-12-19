package Optimization;

import IR.Instr.PhiInstr;
import IR.Value.BasicBlock;
import IR.Value.Function;
import IR.Value.Instruction;
import IR.Value.LLVMModule;
import IR.Value.Undefined;

import java.util.Objects;

public class IrNumClear {
    private int count;
    private LLVMModule module;

    public IrNumClear(LLVMModule module) {
        this.module = module;
    }

    public void clear() {
        for (Function func : module.getFunctions()) {
            count = 0;
            if (func.getParaList() != null)
                count = func.getParaList().size(); // 1: start from 1
            for (BasicBlock block : func.getBlocks()) {
                clearBlock(block);
            }
        }
    }

    private void clearBlock(BasicBlock bb) {
        if (bb.getName().charAt(0) != 'f') {
            bb.setName((count++) + "");
        }
        for (Instruction instr : bb.getInstructions()) {
            if (!Objects.equals(instr.getName(), "") || instr instanceof PhiInstr) {
                instr.setName("%" + count++);
            }
        }
    }
}
