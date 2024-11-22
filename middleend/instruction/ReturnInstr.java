package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Value;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class ReturnInstr extends Instruction {
    private Value value;

    public ReturnInstr(Value value, BasicBlock parentBasicBlock) {
        super(value.getTypeOfValue().getTypeClone(), new ArrayList<>(), parentBasicBlock);
        this.value = value;
        super.addOperand(value);
    }

    public ReturnInstr(BasicBlock parentBasicBlock) {
        super(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList<>(), parentBasicBlock);
        this.value = null;
    }

    public void dump(PrintWriter writer) {
        if (this.getTypeOfValue().getBaseType() != BaseTypeEnum.VOID) {
            writer.printf("  ret %s %s\n", getTypeOfValue().toString(), value.getName());
        } else {
            writer.printf("  ret void\n");
        }
    }
}