package middleend.LLVM_components;

import middleend.type.LLVMType;

import java.util.ArrayList;

public class User extends IrValue {
    private ArrayList<IrValue> operands = new ArrayList<>();
    private int pos = 0;

    public User(LLVMType type, ArrayList<IrValue> operands) {
        super(type);
        for (IrValue op : operands) {
            if (op != null) {
                op.addUse(this, pos);
            }
            this.operands.add(op);
            pos++;
        }
    }

    public void addOperand(IrValue operand) {
        this.operands.add(operand);
        if (operand == null) {
            pos++;
            return; // 占位
        }
        operand.addUse(this, pos);
        pos++;
    }

    public ArrayList<IrValue> getOperands() {
        return operands;
    }

    public int findOperand(IrValue operand) {
        return operands.indexOf(operand);
    }

    public void replaceOperand(int pos, IrValue newOperand) {
        operands.set(pos, newOperand);
        newOperand.addUse(this, pos);
    }
}