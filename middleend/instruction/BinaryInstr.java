package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Value;

import java.io.PrintWriter;
import java.util.ArrayList;

public class BinaryInstr extends Instruction {
    private BinaryOp op;
    private Value left;
    private Value right;

    public BinaryInstr(BinaryOp op, Value left, Value right, BasicBlock parentBasicBlock) {
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

    public Value getLeft() {
        return left;
    }

    public Value getRight() {
        return right;
    }

    public void dump(PrintWriter writer) {
        writer.printf("  %s = %s %s %s, %s\n", this.getName(),
                op.toString().toLowerCase(), getTypeOfValue().toString(), left.getName(), right.getName());
    }
}
