package Optimization;

import IR.Value.Function;
import IR.Value.LLVMModule;

import java.util.ArrayList;

public class Optimizer {
    private LLVMModule module;
    public Optimizer(LLVMModule module) {
        this.module = module;
    }


    private void buildDom() {
        ArrayList<Function> functions = module.getFunctions();
        for (Function function : functions) {
            function.buildDom();
        }
    }

    public void optimize() {
        buildDom();
    }
}
