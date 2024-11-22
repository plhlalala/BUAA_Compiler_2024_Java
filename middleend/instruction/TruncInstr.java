package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Value;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class TruncInstr extends Instruction {
    private Value value;
    private LLVMType destType;

    public TruncInstr(Value value, LLVMType destType, BasicBlock parentBasicBlock) {
        super(destType, new ArrayList<>(), parentBasicBlock);
        this.value = value;
        this.destType = destType;
        super.addOperand(value);
    }

    public void dump(PrintWriter writer) {
        writer.printf("  %s = trunc %s %s to %s\n",
                this.getName(),
                value.getTypeOfValue().toString(),
                value.getName(),
                destType.toString());
    }
}
