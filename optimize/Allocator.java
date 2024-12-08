package optimize;

import backend.Value.MIPSRegister;
import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.IrModule;
import middleend.LLVM_components.IrValue;
import middleend.instruction.AllocaInstr;
import middleend.instruction.BinaryInstr;
import middleend.instruction.CallInstr;
import middleend.instruction.GetelementptrInstr;
import middleend.instruction.IcmpInstr;
import middleend.instruction.Instruction;
import middleend.instruction.LoadInstr;
import middleend.instruction.PhiInstr;
import middleend.instruction.TruncInstr;
import middleend.instruction.ZextInstr;
import middleend.type.BaseTypeEnum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * 使用 LRU 策略优化寄存器的使用。
 * 1. 初始化：重置寄存器状态，准备空闲寄存器列表和使用队列。
 * 2. 分配寄存器：
 * - 为指令操作数和结果分配寄存器，优先使用空闲寄存器。
 * - 如果没有空闲寄存器，则回收最久未使用的寄存器。
 * 3. 释放寄存器：
 * - 对于不再使用的变量或指令，及时释放对应的寄存器。
 * 4. 递归处理：递归地为函数的基本块及其子块分配寄存器，并在控制流图中处理寄存器的活跃性和冲突问题。
 */

public class Allocator {
    private HashMap<MIPSRegister, IrValue> reg2Value;
    private HashMap<IrValue, MIPSRegister> value2Reg;
    private TreeSet<MIPSRegister> freeRegs;
    private LinkedList<MIPSRegister> timeQueue;

    public void optimize(IrModule module) {
        this.freeRegs = new TreeSet<>(Comparator.comparing(MIPSRegister::toString));
        for (Function func : module.getFunctionListWithMain()) {
            reset();  // 重置分配状态
            BasicBlock block = func.getBasicBlocks().get(0);
            allocRegForBlock(block);
            func.setValue2Reg(value2Reg);
        }
    }

    public void allocRegForBlock(BasicBlock block) {
        ArrayList<Instruction> instructions = block.getInstructions();
        HashSet<IrValue> defSet = new HashSet<>();  // 存储定义变量
        HashSet<IrValue> noUseSet = new HashSet<>();  // 存储不再使用变量
        HashMap<IrValue, IrValue> lastUse = new HashMap<>();  // 存储每个变量最后使用的指令

        for (Instruction instruction : instructions) {
            for (IrValue operand : instruction.getOperands()) {
                lastUse.put(operand, instruction);  // 记录操作数在当前基本块最后一次出现的位置
            }
        }

        for (Instruction instr : instructions) {
            if (!(instr instanceof PhiInstr)) {  // 跳过 Phi 指令
                for (IrValue operand : instr.getOperands()) {
                    // 如果该操作数的最后使用指令是当前指令并且它已经分配了寄存器，且该操作数不再被块外的指令使用
                    if (lastUse.get(operand).equals(instr) && value2Reg.containsKey(operand) && !block.getOut().contains(operand)) {
                        freeReg(operand);  // 释放该操作数的寄存器
                        noUseSet.add(operand);  // 记录该操作数不再使用
                    } else {
                        updatetTimeQueue(operand);  // 更新该操作数的寄存器使用时间
                    }
                }
            }
            assert freeRegs.size() + reg2Value.size() == 9;  // 检查寄存器数量是否正确
            assert timeQueue.size() == reg2Value.size();  // 检查寄存器队列是否正确
            // 如果该指令是一个值类型的指令（例如算术运算、加载指令等），并且不是 Zext 或 Trunc 指令
            if (judgeIsValue(instr) && !(instr instanceof ZextInstr) && !(instr instanceof TruncInstr)) {
                defSet.add(instr);  // 记录该指令为定义指令
                allocReg(instr);  // 分配寄存器
            }
        }

        // 处理当前基本块的子块，递归地为它们分配寄存器
        for (BasicBlock child : block.getChildrenDom()) {
            assert freeRegs.size() + reg2Value.size() == 9;  // 检查寄存器数量是否正确
            assert timeQueue.size() == reg2Value.size();  // 检查寄存器队列是否正确
            HashMap<MIPSRegister, IrValue> tmpNoUse = new HashMap<>();
            // 遍历当前基本块中的寄存器，检查是否被子块使用
            for (MIPSRegister reg : reg2Value.keySet()) {
                if (!child.getIn().contains(reg2Value.get(reg))) {
                    tmpNoUse.put(reg, reg2Value.get(reg));  // 如果该寄存器不在子块的输入集合中，记录该寄存器
                }
            }
            for (MIPSRegister reg : tmpNoUse.keySet()) {
                freeReg(tmpNoUse.get(reg));  // 释放不在子块使用的寄存器
            }
            allocRegForBlock(child);  // 为子块分配寄存器
            // 恢复在子块处理前的寄存器分配状态
            for (MIPSRegister reg : tmpNoUse.keySet()) {
                occupyReg(tmpNoUse.get(reg));  // 恢复寄存器分配状态
            }
        }

        for (IrValue value : defSet) {
            freeReg(value);  // 释放寄存器
        }

        for (IrValue value : noUseSet) {
            if (!defSet.contains(value)) {
                if (value2Reg.containsKey(value)) {
                    occupyReg(value);  // 占用原始寄存器，避免错误的寄存器释放
                }
            }
        }
    }

    public void reset() {
        this.reg2Value = new HashMap<>();  // 寄存器到值的映射
        this.value2Reg = new HashMap<>();  // 值到寄存器的映射
        this.timeQueue = new LinkedList<>();  // 用于 LRU (最久未使用) 策略的队列
        this.freeRegs.clear();
        this.freeRegs.addAll(MIPSRegister.getTRegs());  // 初始化空闲寄存器队列
    }

    /**
     * 为指定的值分配一个寄存器。如果有空闲寄存器，直接分配；否则使用 LRU 策略。
     *
     * @param value 需要分配寄存器的值
     */
    public void allocReg(IrValue value) {
        MIPSRegister reg;
        if (!freeRegs.isEmpty()) {  // 如果有空闲寄存器
            reg = freeRegs.pollFirst();
        } else {  // 如果没有空闲寄存器，使用 LRU 策略
            reg = timeQueue.getFirst();
            IrValue removed = reg2Value.get(reg);  // 获取该寄存器对应的值
            freeReg(removed);  // 释放该寄存器
            value2Reg.remove(removed);  // 移除值与寄存器的映射
            freeRegs.remove(reg);  // 从空闲寄存器队列中移除该寄存器
        }
        reg2Value.put(reg, value);  // 将寄存器与值关联
        value2Reg.put(value, reg);  // 将值与寄存器映射
        timeQueue.addLast(reg);  // 将寄存器加入使用队列
    }

    public void freeReg(IrValue value) {
        MIPSRegister reg = value2Reg.get(value);  // 获取该值对应的寄存器
        if (reg2Value.get(reg) != value) {
            return;
        }
        freeRegs.add(reg);  // 将该寄存器放入空闲寄存器队列
        reg2Value.remove(reg);  // 移除 reg2Value 映射
        timeQueue.remove(reg);  // 从 LRU 使用队列中移除该寄存器
    }

    public void updatetTimeQueue(IrValue value) {
        if (value2Reg.containsKey(value) && reg2Value.containsKey(value2Reg.get(value))) {
            MIPSRegister reg = value2Reg.get(value);
            timeQueue.remove(reg);
            timeQueue.addLast(reg);
        }
    }

    public void occupyReg(IrValue value) {
        MIPSRegister reg = value2Reg.get(value);  // 获取该值对应的寄存器
        reg2Value.put(reg, value);  // 将寄存器与值重新关联
        freeRegs.remove(reg);  // 从空闲寄存器队列中移除该寄存器
        timeQueue.remove(reg);  // 从使用队列中移除该寄存器
        timeQueue.addLast(reg);  // 将该寄存器重新加入队列
    }

    /**
     * 判断指令是否定义了一个值。
     *
     * @param instr 需要判断的指令
     * @return 如果指令定义了一个值，则返回 true；否则返回 false
     */
    @SuppressWarnings("DuplicatedCode")
    public boolean judgeIsValue(Instruction instr) {
        // 判断各种指令是否定义了一个值（例如算术、加载、比较等指令）
        if (instr instanceof AllocaInstr || instr instanceof BinaryInstr || instr instanceof GetelementptrInstr
                || instr instanceof LoadInstr || instr instanceof TruncInstr || instr instanceof ZextInstr
                || instr instanceof PhiInstr || instr instanceof IcmpInstr) {
            return true;
        }
        // 如果是调用指令，并且该函数有返回值
        if (instr instanceof CallInstr callInstr) {
            return callInstr.getFunc().getReturnBaseType() != BaseTypeEnum.VOID;
        }
        return false;
    }
}
