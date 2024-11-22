package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Value;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class StoreInstr extends Instruction {
    private Value pointer;
    private Value value;

    public StoreInstr(Value value, Value pointer, BasicBlock basicBlock) {
        super(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList<>(), basicBlock);
        this.pointer = pointer;
        this.value = value;
        super.addOperand(value);
        super.addOperand(pointer);
    }

    public void dump(PrintWriter writer) {
        writer.printf("  store %s %s, %s %s\n",
                value.getTypeOfValue().toString(),
                value.getName(),
                pointer.getTypeOfValue().toString(),
                pointer.getName());
    }
}
