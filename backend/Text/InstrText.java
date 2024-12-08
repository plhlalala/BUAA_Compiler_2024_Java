package backend.Text;

import backend.Value.MIPSLabel;
import backend.Value.MIPSValue;

import java.util.ArrayList;

public class InstrText extends Text {
    private final String IntstrName;
    private final ArrayList<MIPSValue> args;

    public InstrText(String IntstrName, ArrayList<MIPSValue> args) {
        this.IntstrName = IntstrName;
        this.args = args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    ");
        sb.append(IntstrName);
        int len = 7 - IntstrName.length();
        sb.append(" ".repeat(Math.max(2, len)));

        int maxArgLength = 7;

        for (int i = 0; i < args.size(); i++) {
            MIPSValue arg = args.get(i);

            if (arg instanceof MIPSLabel label) {
                sb.append(label.getLabelName());
                len = label.getLabelName().length();
                if (len < maxArgLength) {
                    sb.append(" ".repeat(maxArgLength - len));
                }
            } else {
                sb.append(arg.toString());
                len = arg.toString().length();
                if (len < maxArgLength) {
                    sb.append(" ".repeat(maxArgLength - len));
                }
            }

            if (i != args.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }


}
