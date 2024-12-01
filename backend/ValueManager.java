package backend;

import backend.Value.MIPSRegister;
import backend.Value.MIPSValue;
import middleend.LLVM_components.IrValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class ValueManager {
    private final HashMap<IrValue, MIPSValue> globalMap = new HashMap<>();
    private final HashMap<IrValue, MIPSValue> occupation = new HashMap<>();
    private final HashMap<IrValue, Integer> OffsetValueMap = new HashMap<>();
    private final HashMap<MIPSValue, IrValue> MIPSValueMap = new HashMap<>();
    private final LinkedList<MIPSRegister> timeQueue = new LinkedList<>();
    private final MIPSmodule mipsmodule;
    private LinkedList<MIPSRegister> tempRegPool;
    private int currentOffset = 0;

    public ValueManager(MIPSmodule mipsmodule) {
        this.mipsmodule = mipsmodule;
        this.tempRegPool = MIPSRegister.getTregAndSreg();
    }

    public void subOffset(int size) {
        this.currentOffset -= size;
    }

    public int getOffset() {
        return this.currentOffset;
    }

    public void setOffset(int offset) {
        this.currentOffset = offset;
    }

    public void addOffSetValueMap(IrValue irValue, int offset) {
        OffsetValueMap.put(irValue, offset);
    }

    public void addRegValueMap(IrValue irValue, MIPSValue mipsValue) {
        occupation.put(irValue, mipsValue);
        MIPSValueMap.put(mipsValue, irValue);
    }

    public void removeOffSetValueMap(IrValue irValue) {
        OffsetValueMap.remove(irValue);
    }

    public int getOffSetOfValue(IrValue irValue) {
        return OffsetValueMap.getOrDefault(irValue, null);
    }

    public void allocRegForPara(IrValue irValue, int index) {
        MIPSRegister reg = MIPSRegister.getReg("a" + index);
        occupation.put(irValue, reg);
        MIPSValueMap.put(reg, irValue);
    }

    public ArrayList<MIPSRegister> getAllocatedRegs() {
        ArrayList<MIPSRegister> regs = new ArrayList<>();
        for (MIPSValue mipsValue : occupation.values()) {
            if (mipsValue instanceof MIPSRegister reg && !regs.contains(reg)) {
                regs.add(reg);
            }
        }
        return regs;
    }

    public void reset() {
        occupation.clear();
        OffsetValueMap.clear();
        MIPSValueMap.clear();
        timeQueue.clear();
        tempRegPool = MIPSRegister.getTregAndSreg();
        currentOffset = 0;
    }

    public void addGlobal(IrValue irValue, MIPSValue mipsValue) {
        globalMap.put(irValue, mipsValue);
    }

    public MIPSRegister getRegOfValue(IrValue value) {
        if (occupation.containsKey(value)) {
            MIPSValue mipsValue = occupation.get(value);
            if (mipsValue instanceof MIPSRegister reg) {
                return reg;
            }
        }
        return null;
    }

    public MIPSValue getLabelOfGlobal(IrValue value) {
        return globalMap.getOrDefault(value, null);
    }

//    public void push(IrValue value) {
//        MIPSRegister reg = getReg(value);
//        MIPSOffset offset = new MIPSOffset(currentOffset);
//        mipsmodule.addText(new InstrText("sw", Generator.buildArray(reg, offset)));
//        release(value);
//        occupation.put(value, offset);
//        MIPSValueMap.put(offset, value);
//        currentOffset -= 4; // Adjust stack pointer offset
//    }
//
//    public void load(IrValue value) {
//        MIPSRegister reg = getFreeTmp();
//        MIPSOffset offset = (MIPSOffset) occupation.get(value);
//        mipsmodule.addText(new InstrText("lw", Generator.buildArray(reg, offset)));
//        release(value);
//        occupy(reg, value);
//    }
//
//    public MIPSRegister allocReg(IrValue value) {
//        MIPSRegister reg = getFreeTmp();
//        occupy(reg, value);
//        return reg;
//    }
//
//    /*
//    doc：getReg()方法用于获取一个寄存器，如果该值已经在寄存器中，则直接返回，否则调用allocReg()方法分配一个寄存器
//     */
//    public MIPSRegister getReg(IrValue value) {
//        if (globalMap.containsKey(value)) {
//            MIPSValue mipsValue = globalMap.get(value);
//            MIPSLabel label = (MIPSLabel) mipsValue;
//            MIPSRegister reg = allocReg(value);
//            mipsmodule.addText(new InstrText("la", Generator.buildArray(reg, label)));
//            return reg;
//        }
//        if (occupation.containsKey(value)) {
//            if (occupation.get(value) instanceof MIPSRegister reg) {
//                timeQueue.remove(reg);
//                timeQueue.add(reg);
//                return reg;
//            } else { //TODO 对于指针要如何处理？
//                load(value);
//                return (MIPSRegister) occupation.get(value);
//            }
//        }
//        return allocReg(value);
//    }
//
//    public void release(IrValue value) {
//        if (occupation.containsKey(value)) {
//            MIPSValue mipsvalue = occupation.get(value);
//            if (mipsvalue instanceof MIPSRegister reg) {
//                tempRegPool.add(reg);
//                timeQueue.remove(reg);
//                occupation.remove(value);
//                MIPSValueMap.remove(reg);
//            }
//        }
//    }
//
//    public void occupy(MIPSValue mipsvalue, IrValue irvalue) {
//        if (mipsvalue instanceof MIPSRegister reg) {
//            tempRegPool.remove(reg);
//            timeQueue.add(reg);
//        }
//        occupation.put(irvalue, mipsvalue);
//        MIPSValueMap.put(mipsvalue, irvalue);
//    }
//
//    private MIPSRegister getFreeTmp() {
//        if (tempRegPool.isEmpty()) {
//            MIPSRegister reg = timeQueue.removeFirst();
//            IrValue irValue = MIPSValueMap.get(reg);
//            push(irValue);
//            return tempRegPool.removeFirst();
//        } else {
//            return tempRegPool.removeFirst();
//        }
//    }
//
//    public MIPSValue getMipsValue(IrValue irValue) {
//        if (irValue instanceof ImmIrValueI32 immValueI32) {
//            return new MIPSImmediate(immValueI32.getValue());
//        } else if (irValue instanceof ImmIrValueBool immValueBool) {
//            return new MIPSImmediate(immValueBool.getValue());
//        }
//        if (localMap.containsKey(irValue)) {
//            return localMap.get(irValue);
//        } else if (globalMap.containsKey(irValue)) {
//            return globalMap.get(irValue);
//        }
//        return null;
//    }
//
//    public void addGlobalValue(IrValue irValue, MIPSLabel label) {
//        globalMap.put(irValue, label);
//    }
//
//    public int addLocalValue(Function func) {
//        int paranum = func.getParams().size();
//        ArrayList<AllocaInstr> allocaInstrs = func.getAllocaInstrs();
//        ArrayList<AllocaInstr> paraAlloca = (ArrayList<AllocaInstr>) allocaInstrs.subList(0, paranum);
//        ArrayList<AllocaInstr> varAlloca = (ArrayList<AllocaInstr>) allocaInstrs.subList(paranum, allocaInstrs.size());
//
//        for (int i = 0; i < paranum && i < 4; i++) {
//            localMap.put(paraAlloca.get(i), MIPSRegister.getReg("a" + i));
//        }
//        basicAllocGlobalReg(varAlloca); // 待优化
//        int paranum_REG = Math.min(paranum, 4);
//        int memory_require = 4 * paranum_REG;
//
//        for (BasicBlock block : func.getBasicBlocks()) {
//            for (Instruction instruction : block.getInstructions()) {
//                if (instruction instanceof StoreInstr ||
//                        instruction instanceof BrInstr ||
//                        instruction instanceof ReturnInstr ||
//                        (instruction instanceof CallInstr callInstr && callInstr.getTypeOfValue().getBaseType() == BaseTypeEnum.VOID)) {
//                    continue;
//                }
//                if (localMap.containsKey(instruction)) {
//                    continue;
//                }
//                if (instruction instanceof AllocaInstr allocaInstr &&
//                        allocaInstr.getTypeOfValue() instanceof ArrayType arrayType &&
//                        arrayType.getPtrNum() == 0) {
//                    memory_require += arrayType.getArraysize() * (arrayType.getBaseType() == BaseTypeEnum.INT ? 4 : 1);
//                } else {
//                    memory_require += instruction.getTypeOfValue().getBaseType() == BaseTypeEnum.INT ? 4 : 1;  //指针的大小？ TODO
//                }
//            }
//        }
//
//        int baseOffset = memory_require;
//        MIPSRegister reg = MIPSRegister.SP;
//
//        for (int i = paranum - 1; i >= 4; i--) {
//            baseOffset -= 4;
//            localMap.put(paraAlloca.get(i), new MIPSOffset(reg, baseOffset));
//        }
//        baseOffset -= paranum_REG * 4;
//
//        for (BasicBlock block : func.getBasicBlocks()) {
//            for (Instruction instruction : block.getInstructions()) {
//                if (instruction instanceof StoreInstr ||
//                        instruction instanceof BrInstr ||
//                        instruction instanceof ReturnInstr ||
//                        (instruction instanceof CallInstr callInstr && callInstr.getTypeOfValue().getBaseType() != BaseTypeEnum.VOID)) {
//                    continue;
//                }
//                if (localMap.containsKey(instruction)) {
//                    continue;
//                }
//                if (instruction instanceof AllocaInstr allocaInstr &&
//                        allocaInstr.getTypeOfValue() instanceof ArrayType arrayType &&
//                        arrayType.getPtrNum() == 0) {
//                    baseOffset -= arrayType.getArraysize() * (arrayType.getBaseType() == BaseTypeEnum.INT ? 4 : 1);
//                } else {
//                    baseOffset -= instruction.getTypeOfValue().getBaseType() == BaseTypeEnum.INT ? 4 : 1;
//                }
//                localMap.put(instruction, new MIPSOffset(reg, baseOffset));
//            }
//        }
//        if (baseOffset != 0) {
//            System.out.println("baseOffset not zero");
//        }
//        return memory_require;
//    }
//
//    public void basicAllocGlobalReg(ArrayList<AllocaInstr> allocaInstrs) {
//        LinkedList<MIPSRegister> registers = MIPSRegister.getSRegs();
//        for (AllocaInstr allocaInstr : allocaInstrs) {
//            if (registers.isEmpty()) {
//                break;
//            }
//            if (allocaInstr.getAllocatedType() instanceof ArrayType) {
//                continue;
//            }
//            MIPSRegister reg = registers.removeLast();
//            this.localMap.put(allocaInstr, reg);
//        }
//    }
//
//    public LinkedList<MIPSRegister> getRegsInUse() {
//        LinkedList<MIPSRegister> regs = new LinkedList<>();
//        for (MIPSValue value : localMap.values()) {
//            if (value instanceof MIPSRegister reg && !regs.contains(reg)) {
//                regs.add(reg);
//            }
//        }
//        return regs;
//    }
//
//    public void resetLocalMap() {
//        localMap.clear();
//    }
}
