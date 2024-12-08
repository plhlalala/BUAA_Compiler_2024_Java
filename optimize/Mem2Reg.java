package optimize;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.ImmIrValueI32;
import middleend.LLVM_components.ImmIrValueI8;
import middleend.LLVM_components.IrModule;
import middleend.LLVM_components.IrValue;
import middleend.LLVM_components.Use;
import middleend.instruction.AllocaInstr;
import middleend.instruction.Instruction;
import middleend.instruction.LoadInstr;
import middleend.instruction.PhiInstr;
import middleend.instruction.StoreInstr;
import middleend.type.ArrayType;
import middleend.type.BaseTypeEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class Mem2Reg {
    private IrModule module;
    private Instruction curInst;
    private ArrayList<Instruction> useInstructions;
    private ArrayList<Instruction> defInstructions;
    private ArrayList<BasicBlock> defBlocks;
    private ArrayList<BasicBlock> useBlocks;
    private LinkedList<IrValue> valueList;

    public void optimize(IrModule module) {
        this.module = module;
        for (Function func : module.getFunctionListWithMain()) {
            BasicBlock allocBlock = func.getBasicBlocks().get(0);
            for (Instruction instr : new ArrayList<>(allocBlock.getInstructions())) {
                if (instr instanceof AllocaInstr allocaInstr && ! (allocaInstr.getTypeOfValue() instanceof ArrayType)) {
                    curInst = instr;
                    useInstructions = new ArrayList<>();
                    defInstructions = new ArrayList<>();
                    defBlocks = new ArrayList<>();
                    useBlocks = new ArrayList<>();
                    valueList = new LinkedList<>();
                    buildUseDefChain(allocaInstr);
                    insertPhi();
                    rename(allocBlock);
                }
            }
        }
    }

    /**
     * 构建 AllocaInstr 指令的使用-定义链。
     * 遍历 AllocaInstr 指令的所有使用者（LoadInstr）和定义者（StoreInstr），
     * 将它们分别添加到 useInstructions 和 defInstructions 中，并记录它们所在的基本块。
     *
     * @param instr 当前的 AllocaInstr 指令
     */
    private void buildUseDefChain(AllocaInstr instr) {
        ArrayList<Use> uses = instr.getUseList();
        for (Use use : uses) {
            Instruction user = (Instruction) use.getUser();
            if (user instanceof LoadInstr loadInstr) {
                useInstructions.add(user);
                if (!useBlocks.contains(user.getParentBasicBlock())) {
                    useBlocks.add(user.getParentBasicBlock());
                }
            } else if (user instanceof StoreInstr storeInstr) {
                defInstructions.add(user);
                if (!defBlocks.contains(user.getParentBasicBlock())) {
                    defBlocks.add(user.getParentBasicBlock());
                }
            }
        }
    }

    /**
     * 在需要的位置插入 Phi 指令。
     * 根据变量定义的基本块和其支配前沿（domFrontier），在合适的地方插入 Phi 指令，
     * 以便在变量的多个定义路径合流时正确地选择对应的值。
     */
    private void insertPhi() {
        HashSet<BasicBlock> phiBlock = new HashSet<>(); // 添加 phi 指令的基本块
        LinkedList<BasicBlock> workList = new LinkedList<>(defBlocks); // 变量定义的基本块
        while (!workList.isEmpty()) {
            BasicBlock block = workList.removeLast();
            for (BasicBlock dfblock : block.getDomFrontierList()) {
                if (!phiBlock.contains(dfblock)) {
                    // 在 df 前部创建 phi 指令
                    IrValue phi = dfblock.createPhiInstrInFront(curInst.getTypeOfValue().getTypeClone().subPtr(), dfblock.getPredecessors());
                    useInstructions.add((Instruction) phi);
                    defInstructions.add((Instruction) phi);
                    phiBlock.add(dfblock);
                    if (!defBlocks.contains(dfblock)) {
                        // 如果 df 不在 defBlocks 中，将 df 加入 workList
                        workList.add(dfblock);
                    }
                }
            }
        }
    }

    /**
     * 进行变量重命名。
     * 遍历基本块中的指令，将所有的 StoreInstr 和 LoadInstr 重命名为使用最新值的版本，
     * 并为需要的基本块添加 Phi 指令操作数。
     *
     * @param block 当前基本块
     */
    private void rename(BasicBlock block) {
        int len = valueList.size();
        Iterator<Instruction> iterator = block.getInstructions().iterator();
        while (iterator.hasNext()) {
            Instruction instr = iterator.next();
            if (instr instanceof StoreInstr storeInstr && defInstructions.contains(storeInstr)) {
                valueList.add(storeInstr.getOperands().get(0));
                iterator.remove();
            } else if (instr instanceof LoadInstr loadInstr && useInstructions.contains(loadInstr)) {
                if (valueList.isEmpty()) {
                    if (curInst.getTypeOfValue().getBaseType() == BaseTypeEnum.INT) {
                        valueList.add(new ImmIrValueI32(114));
                    } else {
                        valueList.add(new ImmIrValueI8(114));
                    }
                }
                loadInstr.replaceAllUse(valueList.getLast());
                iterator.remove();
            } else if (instr instanceof PhiInstr phiInstr && defInstructions.contains(phiInstr)) {
                valueList.add(phiInstr);
            } else if (instr instanceof AllocaInstr && instr == curInst) {
                iterator.remove();
            }
        }

        // 为后继基本块的 PhiInstr 添加最新值
        for (BasicBlock succ : block.getSuccessors()) {
            Instruction firstInstr = succ.getInstructions().get(0);
            if (firstInstr instanceof PhiInstr phiInstr && useInstructions.contains(phiInstr)) {
                if (valueList.isEmpty()) {
                    if (curInst.getTypeOfValue().getBaseType() == BaseTypeEnum.INT) {
                        valueList.add(new ImmIrValueI32(114));
                    } else {
                        valueList.add(new ImmIrValueI8(114));
                    }
                }
                phiInstr.replaceOperand(valueList.getLast(), block);
            }
        }

        // 对所有支配的子树递归调用 rename 方法
        for (BasicBlock child : block.getChildrenDom()) {
            rename(child);
        }
        valueList = new LinkedList<>(valueList.subList(0, len));
    }
}
