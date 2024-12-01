package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class AllocaInstr extends Instruction {

    public AllocaInstr(LLVMType type, BasicBlock parentBasicBlock) {
        super(type.getTypeClone().addPtr(), new ArrayList<>(), parentBasicBlock);
    }


    public LLVMType getAllocatedType() {
        return super.getTypeOfValue().getTypeClone().subPtr();
    }

    //  %val_ptr = alloca i32
    //  %array = alloca [5 x i32]
    public void dump(PrintWriter writer) {
        writer.printf("  %s = alloca %s\n", this.getName(), this.getAllocatedType().toString());
    }

    public String dumpToString() {
        return String.format("%s = alloca %s", this.getName(), this.getAllocatedType().toString());
    }
}