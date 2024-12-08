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
            // 如果基本块的第一条指令不是 Phi 指令，则跳过该基本块
            if (!(block.getInstructions().get(0) instanceof PhiInstr)) continue;
            // 获取该基本块的所有前驱基本块.进行复制，防止遍历时修改
            ArrayList<BasicBlock> preList = new ArrayList<>(block.getPredecessors());
            // 用于存储该基本块对应前驱要添加的 Pcopy 指令，按照前驱的顺序
            ArrayList<PcopyInstr> pcopyList = new ArrayList<>();
            // 为每个前驱基本块添加一个 Pcopy 指令
            for (BasicBlock preBlock : preList) {
                PcopyInstr instr = new PcopyInstr(preBlock);
                pcopyList.add(instr);
                // 如果前驱基本块只有一个后继，则将 Pcopy 指令插入到前驱基本块的跳转之前
                if (preBlock.getSuccessors().size() == 1) {
                    preBlock.insertBeforeLast(instr);
                } else {
                    // 如果前驱基本块有多个后继，则将 Pcopy 指令与后继基本块绑定
                    preBlock.addBlockAndInsertPcopyInstr(block, instr);
                }
            }

            Iterator<Instruction> iterator = block.getInstructions().iterator();
            while (iterator.hasNext()) {
                Instruction instr = iterator.next();
                if (instr instanceof PhiInstr phiInstr) {
                    ArrayList<IrValue> operands = phiInstr.getOperands();
                    // 将每个操作数与 Pcopy 指令关联
                    for (IrValue operand : operands) {
                        // TODO 未定义的操作数不添加到 Pcopy 指令中
                        pcopyList.get(operands.indexOf(operand)).addCopy(phiInstr, operand);
                    }
                    // 删除该 Phi 指令
                    iterator.remove();
                }
            }
        }
    }

    public void Pcopy2Move(Function func) {
        for (BasicBlock block : func.getBasicBlocks()) {
            ArrayList<Instruction> instructions = block.getInstructions();
            int len = instructions.size();
            if (len < 2) continue;
            if (instructions.get(len - 2) instanceof PcopyInstr pcopyInstr) {
                // 删除原来的 Pcopy 指令
                instructions.remove(len - 2);
                // 获取 Pcopy 指令对应的所有 Move 指令
                ArrayList<MoveInstr> moveInstrs = pcopyInstr.getMoveInstrs();
                for (MoveInstr moveInstr : moveInstrs) {
                    block.insertBeforeLast(moveInstr);
                    moveInstr.setParentBasicBlock(block);
                }
            }
        }
    }
}
