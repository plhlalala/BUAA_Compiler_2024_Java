package middleend.type;

import java.util.ArrayList;

public abstract class LLVMType {
    private int ptrNum = 0;

    public int getPtrNum() {
        return ptrNum;
    }

    public void setPtrNum(int num) {
        this.ptrNum = num;
    }


    public LLVMType getTypeClone() {
        return null;
    }

    public BaseTypeEnum getBaseType() {
        return null;
    }

    public LLVMType addPtr() {
        ptrNum++;
        return this;
    }

    public LLVMType subPtr() {
        ptrNum--;
        return this;
    }

    public abstract String initValuesToString(ArrayList<Integer> initValues);
}
