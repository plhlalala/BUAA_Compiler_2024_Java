package frontend.parser.components.Func;

import frontend.lexer.LexType;

import java.io.PrintWriter;

// FuncType → 'void' | 'int' | 'char'
public class FuncType {
    public LexType type;

    public void analyze(PrintWriter writer) {
        assert type == LexType.VOIDTK || type == LexType.INTTK || type == LexType.CHARTK;
        writer.println(type);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<FuncType>";
    }
}
