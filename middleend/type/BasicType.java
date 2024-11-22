package middleend.type;

import java.util.ArrayList;

public class BasicType extends LLVMType {
    private BaseTypeEnum type;

    public BasicType(BaseTypeEnum type, int ptrNum) {
        this.type = type;
        super.setPtrNum(ptrNum);
    }

    @Override
    public BasicType getTypeClone() {
        return new BasicType(type, getPtrNum());
    }

    @Override
    public BaseTypeEnum getBaseType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString() + "*".repeat(getPtrNum());
    }

    @Override
    public String initValuesToString(ArrayList<Integer> initValues) {
        return this.toString() + " " + (initValues.isEmpty() ? 0 : initValues.get(0));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicType other) {
            return this.type.equals(other.type) && this.getPtrNum() == other.getPtrNum();
        } else {
            return false;
        }
    }
}
