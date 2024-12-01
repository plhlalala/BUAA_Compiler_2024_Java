package middleend.type;

import java.util.ArrayList;

public class ArrayType extends LLVMType {
    private BasicType basicType;
    private int arraysize;

    public ArrayType(BasicType basicType, int size, int ptrNum) {
        this.basicType = basicType;
        this.arraysize = size;
        super.setPtrNum(ptrNum);
    }

    public int getArraysize() {
        return arraysize;
    }

    @Override
    public ArrayType getTypeClone() {
        return new ArrayType(basicType.getTypeClone(), arraysize, getPtrNum());
    }

    @Override
    public BaseTypeEnum getBaseType() {
        return basicType.getBaseType();
    }

    @Override
    public String toString() {
        return "[" + arraysize + " x " + basicType.toString() + "]" + "*".repeat(getPtrNum());
    }

    @Override
    public String initValuesToString(ArrayList<Integer> initVals) { //未初始化的部分自动补全为0，但不会存储在initVals中
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(arraysize).append(" x ").append(basicType.toString()).append("] ");
        if (isAllZero(initVals)) {
            sb.append("zeroinitializer");
            return sb.toString();
        }
        sb.append("[");
        for (int i = 0; i < arraysize; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            if (i < initVals.size()) {
                sb.append(basicType.toString()).append(" ").append(initVals.get(i));
            } else {
                sb.append(basicType.toString()).append(" 0");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private boolean isAllZero(ArrayList<Integer> initVals) {
        for (int i : initVals) {
            if (i != 0) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayType other) {
            return this.basicType.equals(other.basicType) && this.arraysize == other.arraysize && this.getPtrNum() == other.getPtrNum();
        } else {
            return false;
        }
    }

    public BasicType getBasicTypeClone() {
        return new BasicType(basicType.getBaseType(), basicType.getPtrNum());
    }
}
