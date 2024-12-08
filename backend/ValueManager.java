package backend;

import backend.Value.MIPSRegister;
import backend.Value.MIPSValue;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.IrValue;

import java.util.ArrayList;
import java.util.HashMap;

public class ValueManager {
    private final HashMap<IrValue, MIPSValue> globalMap = new HashMap<>();
    private HashMap<IrValue, MIPSRegister> valueRegMap = new HashMap<>();
    private final HashMap<IrValue, Integer> OffsetValueMap = new HashMap<>();
    private final MIPSmodule mipsmodule;
    private int currentOffset = 0;

    public ValueManager(MIPSmodule mipsmodule) {
        this.mipsmodule = mipsmodule;
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

    public void removeOffSetValueMap(IrValue irValue) {
        OffsetValueMap.remove(irValue);
    }

    public int getOffSetOfValue(IrValue irValue) {
        return OffsetValueMap.getOrDefault(irValue, null);
    }

    public boolean containsValueOffset(IrValue irValue) {
        return OffsetValueMap.containsKey(irValue);
    }

    public void allocRegForPara(IrValue irValue, int index) {
        MIPSRegister reg = MIPSRegister.getReg("a" + index);
        valueRegMap.put(irValue, reg);
    }

    public ArrayList<MIPSRegister> getAllocatedRegs() {
        ArrayList<MIPSRegister> regs = new ArrayList<>();
        for (MIPSRegister reg : valueRegMap.values()) {
            if (!regs.contains(reg)) {
                regs.add(reg);
            }
        }
        return regs;
    }

    public void addGlobal(IrValue irValue, MIPSValue mipsValue) {
        globalMap.put(irValue, mipsValue);
    }

    public MIPSValue getLabelOfGlobal(IrValue value) {
        return globalMap.getOrDefault(value, null);
    }

    public MIPSRegister getRegOfValue(IrValue value) {
        return valueRegMap.getOrDefault(value, null);
    }

    public void replaceReg(IrValue value, MIPSRegister reg) {
        valueRegMap.put(value, reg);
    }

    public void reset(Function function) {
        OffsetValueMap.clear();
        currentOffset = 0;
        valueRegMap = function.getValue2Reg();
    }
}
