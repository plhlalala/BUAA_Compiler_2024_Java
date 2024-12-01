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
        for (MIPSValue arg : args) {
            sb.append(" ");
            if (arg instanceof MIPSLabel label) {
                sb.append(label.getLabelName());
                int len = label.getLabelName().length();
                if (len < 6) {
                    sb.append(" ".repeat(6 - len));
                }
            } else {
                sb.append(arg.toString());
                int len = arg.toString().length();
                if (len < 6) {
                    sb.append(" ".repeat(6 - len));
                }
            }
            if (args.indexOf(arg) != args.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}
