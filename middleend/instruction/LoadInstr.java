package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;

import java.io.PrintWriter;
import java.util.ArrayList;

public class LoadInstr extends Instruction {
    private IrValue pointer;

    public LoadInstr(IrValue pointer, BasicBlock parentBasicBlock) {
        super(pointer.getTypeOfValue().getTypeClone().subPtr(), new ArrayList<>(), parentBasicBlock);
        super.addOperand(pointer);
    }

    //    %value = load i32, i32* %p
    public void dump(PrintWriter writer) {
        getPointer();
        writer.printf("  %s = load %s, %s %s\n",
                this.getName(),
                getTypeOfValue().toString(),
                pointer.getTypeOfValue().toString(),
                pointer.getName());
    }

    public String dumpToString() {
        getPointer();
        return String.format("%s = load %s, %s %s",
                this.getName(),
                getTypeOfValue().toString(),
                pointer.getTypeOfValue().toString(),
                pointer.getName());
    }

    public IrValue getPointer() {
        this.pointer = super.getOperands().get(0);
        return pointer;
    }
}
