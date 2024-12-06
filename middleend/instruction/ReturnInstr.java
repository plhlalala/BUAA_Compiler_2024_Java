package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class ReturnInstr extends Instruction {
    private IrValue irValue;

    public ReturnInstr(IrValue irValue, BasicBlock parentBasicBlock) {
        super(irValue.getTypeOfValue().getTypeClone(), new ArrayList<>(), parentBasicBlock);
        super.addOperand(irValue);
    }

    public ReturnInstr(BasicBlock parentBasicBlock) {
        super(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList<>(), parentBasicBlock);
        this.irValue = null;
    }

    public void dump(PrintWriter writer) {
        getReturn();
        if (this.getTypeOfValue().getBaseType() != BaseTypeEnum.VOID) {
            writer.printf("  ret %s %s\n", getTypeOfValue().toString(), irValue.getName());
        } else {
            writer.printf("  ret void\n");
        }
    }

    public String dumpToString() {
        getReturn();
        if (this.getTypeOfValue().getBaseType() != BaseTypeEnum.VOID) {
            return String.format("ret %s %s", getTypeOfValue().toString(), irValue.getName());
        } else {
            return "ret void";
        }
    }

    public IrValue getReturn() {
        if (super.getOperands().isEmpty()) {
            return null;
        }
        this.irValue = super.getOperands().get(0);
        return irValue;
    }
}