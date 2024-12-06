package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.IrValue;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class BrInstr extends Instruction {
    private IrValue cond;
    private BasicBlock trueBranch;
    private BasicBlock falseBranch;
    private BasicBlock dest;

    public BrInstr(BasicBlock dest, BasicBlock parentBasicBlock) {
        super(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList<>(), parentBasicBlock);
        this.dest = dest;
        this.cond = null;
        this.trueBranch = null;
        this.falseBranch = null;
    }

    public BrInstr(IrValue cond, BasicBlock ifTrue, BasicBlock ifFalse, BasicBlock parentBasicBlock) {
        super(new BasicType(BaseTypeEnum.VOID, 0), new ArrayList<>(), parentBasicBlock);
        super.addOperand(cond);
        this.trueBranch = ifTrue;
        this.falseBranch = ifFalse;
        this.dest = null;
    }

    public void dump(PrintWriter writer) {
        getCond();
        if (dest != null) {
            writer.printf("  br label %%%s\n", dest.getName());
        } else {
            //     br i1 %15, label %16, label %36
            writer.printf("  br %s, label %%%s, label %%%s\n", cond.toString(), trueBranch.getName(), falseBranch.getName());
        }
    }

    public String dumpToString() {
        getCond();
        if (dest != null) {
            return String.format("br label %%%s", dest.getName());
        } else {
            return String.format("br %s, label %%%s, label %%%s", cond.toString(), trueBranch.getName(), falseBranch.getName());
        }
    }

    public IrValue getCond() {
        if (super.getOperands().isEmpty()) {
            return null;
        }
        this.cond = super.getOperands().get(0);
        return cond;
    }

    public void setCond(IrValue cond) {
        this.cond = cond;
    }

    public BasicBlock getTrueBranch() {
        return trueBranch;
    }

    public void setTrueBranch(BasicBlock trueBranch) {
        this.trueBranch = trueBranch;
    }

    public BasicBlock getFalseBranch() {
        return falseBranch;
    }

    public void setFalseBranch(BasicBlock falseBranch) {
        this.falseBranch = falseBranch;
    }

    public BasicBlock getDest() {
        return dest;
    }

    public void setDest(BasicBlock dest) {
        this.dest = dest;
    }
}