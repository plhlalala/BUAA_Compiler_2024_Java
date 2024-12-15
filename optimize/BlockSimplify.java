package optimize;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.IrModule;
import middleend.instruction.BrInstr;
import middleend.instruction.ReturnInstr;

import java.util.HashSet;

public class BlockSimplify {
    private IrModule module;

    public void optimize(IrModule irModule) {
        this.module = irModule;
        for (Function func : module.getFunctionListWithMain()) {
            for (BasicBlock block : func.getBasicBlocks()) {
                removeUnreachableInstr(block);
            }
        }

        for (Function func : module.getFunctionListWithMain()) {
            removeUnreachableBlock(func);
        }
    }

    private void removeUnreachableInstr(BasicBlock block) {
        boolean canRemove = false;
        for (int i = 0; i < block.getInstructions().size(); i++) {
            if (canRemove) {
                block.getInstructions().remove(i);
                i--;
            } else if (block.getInstructions().get(i) instanceof BrInstr || block.getInstructions().get(i) instanceof ReturnInstr) {
                canRemove = true;
            }
        }
    }

    private void removeUnreachableBlock(Function func) {
        BasicBlock entry = func.getBasicBlocks().get(0);
        HashSet<BasicBlock> set = new HashSet<>();
        dfs(entry, set);
        for (int i = 0; i < func.getBasicBlocks().size(); i++) {
            BasicBlock block = func.getBasicBlocks().get(i);
            if (!set.contains(block)) {
                func.getBasicBlocks().remove(i);
                i--;
            }
        }
    }

    private void dfs(BasicBlock block, HashSet<BasicBlock> set) {
        if (set.contains(block)) {
            return;
        }
        set.add(block);
        for (BasicBlock next : block.getSuccessors()) {
            dfs(next, set);
        }
    }
}
