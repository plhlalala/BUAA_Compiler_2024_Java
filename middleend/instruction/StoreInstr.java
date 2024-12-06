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
        super.addOperand(irValue);
        super.addOperand(pointer);
    }

    public void dump(PrintWriter writer) {
        getPointer();
        getValue();
        writer.printf("  store %s %s, %s %s\n",
                irValue.getTypeOfValue().toString(),
                irValue.getName(),
                pointer.getTypeOfValue().toString(),
                pointer.getName());
    }

    public String dumpToString() {
        getPointer();
        getValue();
        return String.format("store %s %s, %s %s",
                irValue.getTypeOfValue().toString(),
                irValue.getName(),
                pointer.getTypeOfValue().toString(),
                pointer.getName());
    }

    public IrValue getPointer() {
        this.pointer = super.getOperands().get(1);
        return pointer;
    }

    public IrValue getValue() {
        this.irValue = super.getOperands().get(0);
        return irValue;
    }
}
