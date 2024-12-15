package optimize;

import middleend.LLVM_components.ImmIrValueBool;
import middleend.LLVM_components.IrModule;
import middleend.instruction.BrInstr;
import middleend.instruction.Instruction;

public class BrOptimize {
    public void optimize(IrModule module) {
        for (var func : module.getFunctionListWithMain()) {
            for (var block : func.getBasicBlocks()) {
                for (int i = 0; i < block.getInstructions().size(); i++) {
                    Instruction instr = block.getInstructions().get(i);
                    if (instr instanceof BrInstr brInstr && brInstr.getCond() instanceof ImmIrValueBool bool) {
                        if (bool.getValue() == 0) {
                            BrInstr newBrInstr = new BrInstr(brInstr.getFalseBranch(), block);
                            block.getInstructions().add(i, newBrInstr);
                            block.getInstructions().remove(i + 1);
                        } else {
                            BrInstr newBrInstr = new BrInstr(brInstr.getTrueBranch(), block);
                            block.getInstructions().add(i, newBrInstr);
                            block.getInstructions().remove(i + 1);
                        }
                    }
                }
            }
        }
    }
}
