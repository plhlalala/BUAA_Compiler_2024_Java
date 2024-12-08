package backend.Data;

import backend.Value.MIPSLabel;

import java.util.ArrayList;

public class Data {

    private final MIPSLabel label;
    private final DataType dataType;
    private final ArrayList<Integer> values;
    private final String string;

    public Data(MIPSLabel label, DataType dataType, ArrayList<Integer> values) {
        this.label = label;
        this.dataType = dataType;
        this.values = values;
        this.string = null;
    }

    public Data(MIPSLabel label, DataType dataType, int value) {
        this.label = label;
        this.dataType = dataType;
        this.values = new ArrayList<>();
        this.values.add(value);
        this.string = null;
    }

    @Override
    public String toString() {
        if (string == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(label);
            int len = (8 - label.toString().length());
            sb.append(" ".repeat(Math.max(2, len)));
            sb.append(dataType.toString()).append("    ");
            for (int i = 0; i < values.size(); i++) {
                sb.append(values.get(i));
                if (i != values.size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }
}