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
        this.left = left;
        this.right = right;
        ArrayList<IrValue> operands = new ArrayList<>();
        operands.add(left);
        operands.add(right);
        super.addOperands(operands);
    }

    // %result = icmp sgt i32 %a, %b
    public void dump(PrintWriter writer) {
        writer.printf("  %s = icmp %s %s %s, %s\n",
                this.getName(),
                cond.toString().toLowerCase(),
                left.getTypeOfValue().toString(),
                left.getName(),
                right.getName());
    }

    public String dumpToString() {
        return String.format("%s = icmp %s %s %s, %s",
                this.getName(),
                cond.toString().toLowerCase(),
                left.getTypeOfValue().toString(),
                left.getName(),
                right.getName());
    }

    public IrValue getLeft() {
        return left;
    }

    public IrValue getRight() {
        return right;
    }

    public IcmpOpEnum getCond() {
        return cond;
    }
}
