package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Value;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class IcmpInstr extends Instruction {
    private IcmpCondEnum cond;
    private Value left;
    private Value right;

    public IcmpInstr(IcmpCondEnum cond, Value left, Value right, BasicBlock parentBasicBlock) {
        super(new BasicType(BaseTypeEnum.BOOL, 0), new ArrayList<>(), parentBasicBlock);
        this.cond = cond;
        this.left = left;
        this.right = right;
        ArrayList<Value> operands = new ArrayList<>();
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


}
