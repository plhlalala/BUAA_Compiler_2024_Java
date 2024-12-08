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

    public void replaceOneOperand(int pos, IrValue newOperand) {
        if (operands.get(pos) != null) {
            operands.get(pos).deleteUse(this, pos);
        }
        operands.set(pos, newOperand);
        newOperand.addUse(this, pos);
    }

    public void replaceOneAndNotModifyUseValue(int pos, IrValue newOperand) { // 用于replaceAllUse,让原来的值自己删除uselist
        operands.set(pos, newOperand);
        newOperand.addUse(this, pos);
    }
}