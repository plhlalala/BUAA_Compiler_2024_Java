package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class IcmpInstr extends Instruction {
    private IcmpOpEnum cond;
    private IrValue left;
    private IrValue right;

    public IcmpInstr(IcmpOpEnum cond, IrValue left, IrValue right, BasicBlock parentBasicBlock) {
        super(new BasicType(BaseTypeEnum.BOOL, 0), new ArrayList<>(), parentBasicBlock);
        this.cond = cond;
        super.addOperand(left);
        super.addOperand(right);
    }

    // %result = icmp sgt i32 %a, %b
    public void dump(PrintWriter writer) {
        getLeft();
        getRight();
        writer.printf("  %s = icmp %s %s %s, %s\n",
                this.getName(),
                cond.toString().toLowerCase(),
                left.getTypeOfValue().toString(),
                left.getName(),
                right.getName());
    }

    public String dumpToString() {
        getLeft();
        getRight();
        return String.format("%s = icmp %s %s %s, %s",
                this.getName(),
                cond.toString().toLowerCase(),
                left.getTypeOfValue().toString(),
                left.getName(),
                right.getName());
    }

    public IrValue getLeft() {
        this.left = super.getOperands().get(0);
        return left;
    }

    public IrValue getRight() {
        this.right = super.getOperands().get(1);
        return right;
    }

    public IcmpOpEnum getCond() {
        return cond;
    }
}
