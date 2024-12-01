package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class StoreInstr extends Instruction {
    private IrValue pointer;
    private IrValue irValue;

    public StoreInstr(IrValue irValue, IrValue pointer, BasicBlock basicBlock) {
        super(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList<>(), basicBlock);
        this.pointer = pointer;
        this.irValue = irValue;
        super.addOperand(irValue);
        super.addOperand(pointer);
    }

    public void dump(PrintWriter writer) {
        writer.printf("  store %s %s, %s %s\n",
                irValue.getTypeOfValue().toString(),
                irValue.getName(),
                pointer.getTypeOfValue().toString(),
                pointer.getName());
    }

    public String dumpToString() {
        return String.format("store %s %s, %s %s",
                irValue.getTypeOfValue().toString(),
                irValue.getName(),
                pointer.getTypeOfValue().toString(),
                pointer.getName());
    }

    public IrValue getPointer() {
        return pointer;
    }

    public IrValue getValue() {
        return irValue;
    }
}
