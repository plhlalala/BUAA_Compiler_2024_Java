package frontend.parser.components.Func;

import frontend.lexer.LexType;

import java.io.PrintWriter;
import java.util.ArrayList;

// FuncFParams → FuncFParam { ',' FuncFParam }
public class FuncFParams {
    public ArrayList<FuncFParam> FParams = new ArrayList<>();

    public void analyze(PrintWriter writer) {
        for (FuncFParam funcFParam : FParams) {
            if (!FParams.get(0).equals(funcFParam)) {
                writer.println(LexType.COMMA);
            }
            funcFParam.analyze(writer);
        }
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<FuncFParams>";
    }
}
