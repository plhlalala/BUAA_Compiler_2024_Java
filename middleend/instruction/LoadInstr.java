package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Value;

import java.io.PrintWriter;
import java.util.ArrayList;

public class LoadInstr extends Instruction {
    private Value pointer;

    public LoadInstr(Value pointer, BasicBlock parentBasicBlock) {
        super(pointer.getTypeOfValue().getTypeClone().subPtr(), new ArrayList<>(), parentBasicBlock);
        this.pointer = pointer;
        super.addOperand(pointer);
    }

    //    %value = load i32, i32* %p
    public void dump(PrintWriter writer) {
        writer.printf("  %s = load %s, %s %s\n",
                this.getName(),
                getTypeOfValue().toString(),
                pointer.getTypeOfValue().toString(),
                pointer.getName());
    }
}
