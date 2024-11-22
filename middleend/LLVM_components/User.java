package middleend.LLVM_components;

import middleend.type.LLVMType;

import java.util.ArrayList;

public class User extends Value {
    private ArrayList<Value> operands = new ArrayList<>();
    private int pos = 0;

    public User(LLVMType type, ArrayList<Value> operands) {
        super(type);
        for (Value op : operands) {
            if (op != null) {
                op.addUse(this, pos);
            }
            this.operands.add(op);
            pos++;
        }
    }

    public void addOperand(Value operand) {
        operands.add(operand);
        operand.addUse(this, pos);
        pos++;
    }

    public ArrayList<Value> getOperands() {
        return operands;
    }

    public int findOperand(Value operand) {
        return operands.indexOf(operand);
    }

    public void replaceOperand(int pos, Value newOperand) {
        operands.set(pos, newOperand);
    }
}