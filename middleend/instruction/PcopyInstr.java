package middleend.instruction;

import backend.Value.MIPSRegister;
import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.CloneValue;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.ImmIrValueBool;
import middleend.LLVM_components.ImmIrValueI32;
import middleend.LLVM_components.ImmIrValueI8;
import middleend.LLVM_components.IrValue;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PcopyInstr extends Instruction {
    private ArrayList<IrValue> dstList;
    private ArrayList<IrValue> srcList;

    public PcopyInstr(BasicBlock block) {
        super(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList<>(), block);
        this.dstList = new ArrayList<>();
        this.srcList = new ArrayList<>();
    }

    public void addCopy(IrValue dst, IrValue src) {
        dstList.add(dst);
        srcList.add(src);
    }

    public ArrayList<IrValue> getSrcList() {
        return srcList;
    }

    public ArrayList<IrValue> getDstList() {
        return dstList;
    }

    public ArrayList<MoveInstr> getMoveInstrs() {
        Function func = getParentBasicBlock().getParentFunction();
        HashMap<IrValue, MIPSRegister> value2Reg = func.getValue2Reg();
        ArrayList<MoveInstr> moveInstrs = new ArrayList<>();
        for (int i = 0; i < dstList.size(); i++) {
            IrValue dst = dstList.get(i);
            IrValue src = srcList.get(i);
            MoveInstr instr = new MoveInstr(dst, src, getParentBasicBlock());
            moveInstrs.add(instr);
        }

        ArrayList<MoveInstr> tmpList = new ArrayList<>();
        HashSet<IrValue> rec = new HashSet<>();

        for (int i = 0; i < moveInstrs.size(); i++) {
            IrValue value = moveInstrs.get(i).getDst();
            if (!(value instanceof ImmIrValueI32) && !(value instanceof ImmIrValueI8) &&
                    !(value instanceof ImmIrValueBool) && !rec.contains(value)) {
                for (int j = i + 1; j < moveInstrs.size(); j++) {
                    if (moveInstrs.get(j).getSrc().equals(value)) {
                        IrValue cloneValue = new CloneValue(value);
                        for (MoveInstr instr : moveInstrs) {
                            if (instr.getSrc().equals(value)) {
                                instr.setSrc(cloneValue);
                            }
                        }
                        break;
                    }
                }
                tmpList.add(new MoveInstr(new CloneValue(value), value, getParentBasicBlock()));
            }
            rec.add(value);
        }
        rec.clear();

        for (int i = moveInstrs.size() - 1; i >= 0; i--) {
            IrValue value = moveInstrs.get(i).getSrc();
            if (!(value instanceof ImmIrValueI32) && !(value instanceof ImmIrValueI8) &&
                    !(value instanceof ImmIrValueBool) && !rec.contains(value)) {
                for (int j = i - 1; j >= 0; j--) {
                    if (value2Reg.get(value) != null && value2Reg.get(value).equals(value2Reg.get(moveInstrs.get(j).getDst()))) {
                        IrValue cloneValue = new CloneValue(value);
                        for (MoveInstr instr : moveInstrs) {
                            if (instr.getSrc().equals(value)) {
                                instr.setSrc(cloneValue);
                            }
                        }
                        break;
                    }
                }
                tmpList.add(new MoveInstr(value, new CloneValue(value), getParentBasicBlock()));
            }
            rec.add(value);
        }
        tmpList.addAll(moveInstrs);
        return tmpList;
    }
}
