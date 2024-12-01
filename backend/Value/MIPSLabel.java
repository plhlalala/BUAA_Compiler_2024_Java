package backend.Value;

public class MIPSLabel extends MIPSValue {
    private final String labelName;

    public MIPSLabel(String labelName) {
        this.labelName = labelName;
    }

    @Override
    public String toString() {
        return labelName + ":";
    }

    public String getLabelName() {
        return labelName;
    }
}
