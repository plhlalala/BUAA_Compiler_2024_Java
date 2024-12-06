package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class PhiInstr extends Instruction {
    private ArrayList<BasicBlock> preBlocks;

    public PhiInstr(LLVMType type, ArrayList<BasicBlock> preBlocks, BasicBlock parentBasicBlock) {
        super(type, new ArrayList<>(), parentBasicBlock);
        this.preBlocks = preBlocks;
        for (int i = 0; i < preBlocks.size(); i++) {
            super.addOperand(null);
        }
    }

    public ArrayList<BasicBlock> getPreBlocks() {
        return preBlocks;
    }

    public ArrayList<IrValue> getOperands() {
        return super.getOperands();
    }

    public void replaceOperand(IrValue value, BasicBlock block) {
        int pos = preBlocks.indexOf(block);
        super.replaceOperand(pos, value);
        value.addUse(this, pos);
    }

    public void setOperand(BasicBlock block, IrValue value) {
        int pos = preBlocks.indexOf(block);
        super.replaceOperand(pos, value);
        value.addUse(this, pos);
    }

    // %x = phi i32 [ 1, %block1 ], [ 2, %block2 ]
    @Override
    public void dump(PrintWriter writer) {
        StringBuilder operands = new StringBuilder();
        for (int i = 0; i < preBlocks.size(); i++) {
            operands.append(String.format("[%s, %%%s], ", getOperands().get(i).getName(), preBlocks.get(i).getName()));
        }
        writer.printf("  %s = phi %s %s\n",
                this.getName(),
                getTypeOfValue().toString(),
                operands.substring(0, operands.length() - 2));
    }

    @Override
    public String dumpToString() {
        StringBuilder operands = new StringBuilder();
        for (int i = 0; i < preBlocks.size(); i++) {
            operands.append(String.format("[%s, %%%s], ", getOperands().get(i).getName(), preBlocks.get(i).getName()));
        }
        return String.format("%s = phi %s %s",
                this.getName(),
                getTypeOfValue().toString(),
                operands.substring(0, operands.length() - 2));
    }
}
