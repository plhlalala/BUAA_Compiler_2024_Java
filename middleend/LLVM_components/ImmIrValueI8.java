package middleend.LLVM_components;

import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

public class ImmIrValueI8 extends IrValue { // TODO:i8 常量传播等待完成
    private final int value;

    public ImmIrValueI8(int value) {
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
