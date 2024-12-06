package middleend.LLVM_components;

public class CloneValue extends IrValue {
    private IrValue originalValue;

    public CloneValue(IrValue originalValue) {
        super(originalValue.getTypeOfValue().getTypeClone());
        this.originalValue = originalValue;
    }

    public IrValue getOriginalValue() {
        return originalValue;
    }

    @Override
    public String getName() {
        return originalValue.getName() + "_clone";
    }

    @Override
    public String toString() {
        return getTypeOfValue().toString() + " " + getName();
    }
}
