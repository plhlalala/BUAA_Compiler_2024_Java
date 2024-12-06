package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.util.ArrayList;

public class MoveInstr extends Instruction {

    public MoveInstr(IrValue dst, IrValue src, BasicBlock parentBasicBlock) {
        super(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList<>(), parentBasicBlock);
        addOperand(dst);
        addOperand(src);
    }

    public IrValue getDst() {
        return getOperands().get(0);
    }

    public IrValue getSrc() {
        return getOperands().get(1);
    }

    public void setDst(IrValue dst) {
        replaceOperand(0, dst);
    }

    public void setSrc(IrValue src) {
        replaceOperand(1, src);
    }

//    @Override
//    public String toString() {
//        ArrayList<IrValue> operands = getOperands();
//        return "move " + operands.get(0).getTypeOfValue() + " " + operands.get(0).getName() + ", " + operands.get(1).getName();
//    }
}
