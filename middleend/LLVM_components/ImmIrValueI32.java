package middleend.LLVM_components;

import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

public class ImmIrValueI32 extends IrValue {
    private final int value;

    public ImmIrValueI32(int value) {
        super(new BasicType(BaseTypeEnum.INT, 0));
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
