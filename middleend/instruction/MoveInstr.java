package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.io.PrintWriter;
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
        replaceOneOperand(0, dst);
    }

    public void setSrc(IrValue src) {
        replaceOneOperand(1, src);
    }
    
    @Override
    public void dump(PrintWriter writer) {
        writer.printf("  move %s %s\n", getDst().getName(), getSrc().getName());
    }

    @Override
    public String dumpToString() {
        return String.format("  move %s %s", getDst().getName(), getSrc().getName());
    }
}
