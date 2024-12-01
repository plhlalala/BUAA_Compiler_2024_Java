package backend.Value;

public class MIPSImmediate extends MIPSValue {
    private final int value;

    public MIPSImmediate(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
