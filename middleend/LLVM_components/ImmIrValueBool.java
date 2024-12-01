package middleend.LLVM_components;

import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

public class ImmIrValueBool extends IrValue {
    private final int value;

    public ImmIrValueBool(int value) {
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
