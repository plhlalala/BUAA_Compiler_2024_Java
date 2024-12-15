package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;
import middleend.LLVM_components.User;
import middleend.type.BaseTypeEnum;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class Instruction extends User {
    private BasicBlock parentBasicBlock;

    public Instruction(LLVMType type, ArrayList<IrValue> operands, BasicBlock parentBasicBlock) {
        super(type, operands);
        this.parentBasicBlock = parentBasicBlock;
    }

    public void addOperands(ArrayList<IrValue> operands) {
        for (IrValue operand : operands) {
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

    public String dumpToString() {
        return "instruction";
    }

    public static boolean judgeIsValue(Instruction instr) {
        if (instr instanceof AllocaInstr || instr instanceof BinaryInstr || instr instanceof GetelementptrInstr
                || instr instanceof LoadInstr || instr instanceof TruncInstr || instr instanceof ZextInstr
                || instr instanceof PhiInstr || instr instanceof IcmpInstr) {
            return true;
        }
        if (instr instanceof CallInstr callInstr) {
            return callInstr.getFunc().getReturnBaseType() != BaseTypeEnum.VOID;
        }
        return false;
    }
}
