package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.User;
import middleend.LLVM_components.Value;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class Instruction extends User {
    private BasicBlock parentBasicBlock;

    public Instruction(LLVMType type, ArrayList<Value> operands, BasicBlock parentBasicBlock) {
        super(type, operands);
        this.parentBasicBlock = parentBasicBlock;
    }

    public void addOperands(ArrayList<Value> operands) {
        for (Value operand : operands) {
            super.addOperand(operand);
        }
    }

    public BasicBlock getParentBasicBlock() {
        return parentBasicBlock;
    }

    public void setParentBasicBlock(BasicBlock parentBasicBlock) {
        this.parentBasicBlock = parentBasicBlock;
    }

    @Override
    public String getName() {
        return "%t" + super.getName();
    }

    public void dump(PrintWriter writer) {

    }
}
