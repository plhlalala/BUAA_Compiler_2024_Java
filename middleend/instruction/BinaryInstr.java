package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;

import java.io.PrintWriter;
import java.util.ArrayList;

public class BinaryInstr extends Instruction {
    private BinaryOp op;
    private IrValue left;
    private IrValue right;

    public BinaryInstr(BinaryOp op, IrValue left, IrValue right, BasicBlock parentBasicBlock) {
        super(left.getTypeOfValue(), new ArrayList<>(), parentBasicBlock);
        this.op = op;
        this.left = left;
        this.right = right;
        super.addOperand(left);
        super.addOperand(right);
    }

    public BinaryOp getOp() {
        return op;
    }

    public IrValue getLeft() {
        return left;
    }

    public IrValue getRight() {
        return right;
    }

    public void dump(PrintWriter writer) {
        writer.printf("  %s = %s %s %s, %s\n", this.getName(),
                op.toString().toLowerCase(), getTypeOfValue().toString(), left.getName(), right.getName());
    }

    public String dumpToString() {
        return String.format("%s = %s %s %s, %s", this.getName(),
                op.toString().toLowerCase(), getTypeOfValue().toString(), left.getName(), right.getName());
    }
}
