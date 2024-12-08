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

    /**
     * 将目标值和源值列表转换为一系列的移动指令（MoveInstr）。
     * 通过插入临时变量来处理循环赋值和寄存器冲突问题。
     *
     * @return 返回优化后的移动指令列表。
     */
    public ArrayList<MoveInstr> getMoveInstrs() {
        // 获取当前基本块所在函数的寄存器映射（Value -> Register）
        Function func = getParentBasicBlock().getParentFunction();
        HashMap<IrValue, MIPSRegister> value2Reg = func.getValue2Reg();
        ArrayList<MoveInstr> moveInstrs = new ArrayList<>();
        // 根据目标值和源值生成移动指令
        for (int i = 0; i < dstList.size(); i++) {
            IrValue dst = dstList.get(i);
            IrValue src = srcList.get(i);
            MoveInstr instr = new MoveInstr(dst, src, getParentBasicBlock());
            moveInstrs.add(instr);
        }
        // 存储临时生成的指令，用于解决循环赋值和寄存器冲突问题
        ArrayList<MoveInstr> tmpList = new ArrayList<>();
        // 存储已经处理过的值，避免重复处理
        HashSet<IrValue> rec = new HashSet<>();
        // 处理循环赋值的问题
        for (int i = 0; i < moveInstrs.size(); i++) {
            IrValue value = moveInstrs.get(i).getDst();
            // 排除常量值（Immediate 值）和已处理过的值
            if (!(value instanceof ImmIrValueI32) && !(value instanceof ImmIrValueI8) &&
                    !(value instanceof ImmIrValueBool) && !rec.contains(value)) {
                // 查找后续指令，检测是否存在循环赋值的问题
                for (int j = i + 1; j < moveInstrs.size(); j++) {
                    if (moveInstrs.get(j).getSrc().equals(value)) {
                        // 如果发现值作为源操作数出现在后续指令中，表示存在循环赋值
                        // 通过克隆当前值来解决循环赋值问题
                        IrValue cloneValue = new CloneValue(value);
                        // 修改后续所有使用当前值作为源操作数的移动指令
                        for (MoveInstr instr : moveInstrs) {
                            if (instr.getSrc().equals(value)) {
                                instr.setSrc(cloneValue);
                            }
                        }
                        // 在 tmpList 中插入新的移动指令，将当前值赋给克隆值（作为中间变量）
                        tmpList.add(new MoveInstr(cloneValue, value, getParentBasicBlock()));
                        break;
                    }
                }
            }
            rec.add(value);
        }
        rec.clear();
        // 处理寄存器冲突的问题（反向遍历移动指令列表）
        for (int i = moveInstrs.size() - 1; i >= 0; i--) {
            IrValue value = moveInstrs.get(i).getSrc();
            if (!(value instanceof ImmIrValueI32) && !(value instanceof ImmIrValueI8) &&
                    !(value instanceof ImmIrValueBool) && !rec.contains(value)) {
                // 查找之前的指令，检测是否存在寄存器冲突
                for (int j = i - 1; j >= 0; j--) {
                    // 如果当前值和某个指令的目标寄存器相同，表示有寄存器冲突
                    if (value2Reg.get(value) != null && value2Reg.get(value).equals(value2Reg.get(moveInstrs.get(j).getDst()))) {
                        // 通过克隆当前值来解决寄存器冲突
                        IrValue cloneValue = new CloneValue(value);
                        // 修改之前所有使用当前值作为源操作数的移动指令
                        for (MoveInstr instr : moveInstrs) {
                            if (instr.getSrc().equals(value)) {
                                instr.setSrc(cloneValue);
                            }
                        }
                        // 在 tmpList 中插入新的移动指令，将当前值赋给克隆值（作为中间变量）
                        tmpList.add(new MoveInstr(cloneValue, value, getParentBasicBlock()));
                        break;
                    }
                }
            }
            rec.add(value);
        }

        tmpList.addAll(moveInstrs);
        return tmpList;
    }
}
