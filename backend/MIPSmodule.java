package backend;

import backend.Data.Data;
import backend.Text.Text;

import java.io.PrintWriter;
import java.util.ArrayList;

public class MIPSmodule {
    private final ArrayList<Data> data;
    private final ArrayList<Text> text;

    public MIPSmodule() {
        this.data = new ArrayList<>();
        this.text = new ArrayList<>();
    }

    public void addData(Data data) {
        this.data.add(data);
    }

    public void addText(Text text) {
        this.text.add(text);
    }

    public void dump(PrintWriter writer) {
        writer.println(".data");
        for (Data data : this.data) {
            writer.println(data.toString());
        }
        writer.println(".text");
        for (Text text : this.text) {
            writer.println(text.toString());
        }
    }
}
