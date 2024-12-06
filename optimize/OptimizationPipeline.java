package optimize;

import middleend.LLVM_components.IrModule;

public class OptimizationPipeline {
    private IrModule module;

    public void optimize(IrModule irModule) {
        this.module = irModule;
        new BlockSimplify().simplify(module);
        new CFG().optimize(module);
        new Mem2Reg().optimize(module);
//      new LiveVariableAnalysis().optimize(module);
//      new Allocator().optimize(module);
//      new PhiRemove().optimize(module);
    }
}
