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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Allocator {
    private HashMap<MIPSRegister, IrValue> reg2Value;
    private HashMap<IrValue, MIPSRegister> value2Reg;
    private LinkedList<MIPSRegister> freeRegs;
    private LinkedList<MIPSRegister> timeQueue;

    public void optimize(IrModule module) {
        this.freeRegs = new LinkedList<>(MIPSRegister.getTRegs());
        for (Function func : module.getFunctionListWithMain()) {
            reset();
            BasicBlock block = func.getBasicBlocks().get(0);
            allocRegForBlock(block);
            func.setValue2Reg(value2Reg);
        }
    }

    public void allocRegForBlock(BasicBlock block) {
        ArrayList<Instruction> instructions = block.getInstructions();
        HashSet<IrValue> defSet = new HashSet<>();
        HashSet<IrValue> noUseSet = new HashSet<>();
        HashMap<IrValue, IrValue> lastUse = new HashMap<>();

        for (Instruction instruction : instructions) {
            for (IrValue operand : instruction.getOperands()) {
                lastUse.put(operand, instruction);
            }
        }

        for (Instruction instr : instructions) {
            if (!(instr instanceof PhiInstr)) {
                for (IrValue operand : instr.getOperands()) {
                    if (lastUse.get(operand).equals(instr) && value2Reg.containsKey(operand) && !block.getOut().contains(operand)) {
                        freeReg(operand);
                        noUseSet.add(operand);
                    }
                }
            }

            if (judgeIsValue(instr) && !(instr instanceof ZextInstr) && !(instr instanceof TruncInstr)) {
                defSet.add(instr);
                MIPSRegister reg = allocReg(instr);
                if (reg != null) {
                    if (reg2Value.containsKey(reg)) {
                        freeReg(reg2Value.get(reg));
                    }
                    reg2Value.put(reg, instr);
                    value2Reg.put(instr, reg);
                }
            }
        }

        for (BasicBlock child : block.getChildrenDom()) {
            HashMap<MIPSRegister, IrValue> tmpNoUse = new HashMap<>();
            for (MIPSRegister reg : reg2Value.keySet()) {
                if (!child.getIn().contains(reg2Value.get(reg))) {
                    tmpNoUse.put(reg, reg2Value.get(reg));
                    freeReg(reg2Value.get(reg));
                }
            }

            allocRegForBlock(child);

            for (MIPSRegister reg : tmpNoUse.keySet()) {
                reg2Value.put(reg, tmpNoUse.get(reg));
            }
        }

        for (IrValue value : defSet) {
            freeReg(value);
        }

        for (IrValue value : noUseSet) {
            if (!defSet.contains(value)) {
                if (value2Reg.containsKey(value)) {
                    occupyOriginalReg(value);
                }
            }
        }
    }

    public void reset() {
        this.reg2Value = new HashMap<>();
        this.value2Reg = new HashMap<>();
        this.timeQueue = new LinkedList<>();
    }

    public MIPSRegister allocReg(IrValue value) {
        if (!freeRegs.isEmpty()) {
            MIPSRegister reg = freeRegs.poll();// 从空闲寄存器中取出第一个
            reg2Value.put(reg, value);
            value2Reg.put(value, reg);
            timeQueue.addLast(reg);
            return reg;
        } else {
            MIPSRegister reg = timeQueue.poll();
            reg2Value.remove(reg);
            reg2Value.put(reg, value);
            value2Reg.put(value, reg);
            timeQueue.addLast(reg);
            return reg;
        }
    }

    public void freeReg(IrValue value) {
        MIPSRegister reg = value2Reg.get(value);
        freeRegs.add(reg);
        reg2Value.remove(reg);
        timeQueue.remove(reg);
    }

    public void occupyOriginalReg(IrValue value) {
        MIPSRegister reg = value2Reg.get(value);
        freeRegs.remove(reg);
        timeQueue.remove(reg);
        timeQueue.addLast(reg);
    }

    public boolean judgeIsValue(Instruction instr) {
        if (instr instanceof AllocaInstr || instr instanceof BinaryInstr || instr instanceof GetelementptrInstr
                || instr instanceof LoadInstr || instr instanceof TruncInstr || instr instanceof ZextInstr
                || instr instanceof PhiInstr || instr instanceof IcmpInstr) {
            return true;
        }
        if (instr instanceof CallInstr callInstr) {
            return callInstr.getFunc().getReturnBaseType() != BaseTypeEnum.VOID;
        }
        return false;
    }
}
