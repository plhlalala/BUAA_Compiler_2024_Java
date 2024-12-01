package backend.Text;

public class LabelText extends Text {
    private final String labelName;

    public LabelText(String labelName) {
        this.labelName = labelName;
    }

    @Override
    public String toString() {
        if (labelName.equals(" ")) {
            return " ";
        }
        return labelName + ":";
    }
}
