package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class TruncInstr extends Instruction {
    private IrValue irValue;
    private LLVMType destType;

    public TruncInstr(IrValue irValue, LLVMType destType, BasicBlock parentBasicBlock) {
        super(destType, new ArrayList<>(), parentBasicBlock);
        this.destType = destType;
        super.addOperand(irValue);
    }

    public void dump(PrintWriter writer) {
        getIrValue();
        writer.printf("  %s = trunc %s %s to %s\n",
                this.getName(),
                irValue.getTypeOfValue().toString(),
                irValue.getName(),
                destType.toString());
    }

    public String dumpToString() {
        getIrValue();
        return String.format("%s = trunc %s %s to %s",
                this.getName(),
                irValue.getTypeOfValue().toString(),
                irValue.getName(),
                destType.toString());
    }

    public IrValue getIrValue() {
        this.irValue = super.getOperands().get(0);
        return irValue;
    }

    public LLVMType getDestType() {
        return destType;
    }
}
