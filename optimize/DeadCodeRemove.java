package optimize;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrModule;
import middleend.instruction.CallInstr;
import middleend.instruction.Instruction;

public class DeadCodeRemove {
    public void optimize(IrModule module) {
        for (var func : module.getFunctionListWithMain()) {
            for (var block : func.getBasicBlocks()) {
                removeDeadCode(block);
            }
        }
    }

    public void removeDeadCode(BasicBlock block) {
        for (int i = 0; i < block.getInstructions().size(); i++) {
            var instr = block.getInstructions().get(i);
            if (Instruction.judgeIsValue(instr) && instr.getUseSize() == 0 && !(instr instanceof CallInstr)) {
                block.getInstructions().remove(i);
                i--;
            }
        }
    }
}
