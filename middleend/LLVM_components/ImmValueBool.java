package middleend.LLVM_components;

import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

public class ImmValueBool extends Value {
    private final int value;

    public ImmValueBool(int value) {
        super(new BasicType(BaseTypeEnum.BOOL, 0));
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return Integer.toString(value);
    }
}
