package optimize;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.IrModule;
import middleend.LLVM_components.IrValue;
import middleend.instruction.Instruction;
import middleend.instruction.MoveInstr;
import middleend.instruction.PcopyInstr;
import middleend.instruction.PhiInstr;

import java.util.ArrayList;
import java.util.Iterator;

public class PhiRemove {
    public void optimize(IrModule irModule) {
        for (var func : irModule.getFunctionListWithMain()) {
            Phi2Pcopy(func);
            Pcopy2Move(func);
        }
    }

    private void Phi2Pcopy(Function func) {
        ArrayList<BasicBlock> blockList = new ArrayList<>(func.getBasicBlocks());
        for (BasicBlock block : blockList) {
            if (!(block.getInstructions().get(0) instanceof PhiInstr)) continue;
            ArrayList<BasicBlock> preList = block.getPredecessors();
            ArrayList<PcopyInstr> pcopyList = new ArrayList<>(); // 该块对应前驱要添加的pcopy指令，按照前驱的顺序
            for (BasicBlock preBlock : preList) {
                PcopyInstr instr = new PcopyInstr(preBlock);
                pcopyList.add(instr);
                if (preBlock.getSuccessors().size() == 1) {
                    preBlock.insertInstrToLast(instr);
                } else {
                    preBlock.addBlockAndInsertPcopyInstrToLast(block, instr);
                }
            }
            Iterator<Instruction> iterator = block.getInstructions().iterator();
            while (iterator.hasNext()) {
                Instruction instr = iterator.next();
                if (instr instanceof PhiInstr phiInstr) {
                    ArrayList<IrValue> operands = phiInstr.getOperands();
                    for (IrValue oprand : operands) {
                        pcopyList.get(operands.indexOf(oprand)).addCopy(phiInstr, oprand);
                    }
                    iterator.remove();
                }
            }
        }
    }

    public void Pcopy2Move(Function func) {
        for (BasicBlock block : func.getBasicBlocks()) {
            ArrayList<Instruction> instructions = block.getInstructions();
            int len = instructions.size();
            if (instructions.get(len - 2) instanceof PcopyInstr pcopyInstr) {
                instructions.remove(len - 2);
                ArrayList<MoveInstr> moveInstrs = pcopyInstr.getMoveInstrs();
                for (MoveInstr moveInstr : moveInstrs) {
                    block.getInstructions().add(instructions.size() - 1, moveInstr);
                }
            }
        }
    }
}
