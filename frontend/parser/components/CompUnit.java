package frontend.parser.components;

import frontend.parser.components.Decl.Decl;
import frontend.parser.components.Func.FuncDef;
import frontend.parser.components.Func.MainFuncDef;

import java.io.PrintWriter;
import java.util.ArrayList;

// CompUnit → {Decl} {FuncDef} MainFuncDef
public class CompUnit {
    public ArrayList<Decl> decls = new ArrayList<>();
    public ArrayList<FuncDef> funcDefs = new ArrayList<>();
    public MainFuncDef mainFuncDef;

    public void analyze(PrintWriter writer) {
        for (Decl decl : decls) {
            decl.analyze(writer);
        }
        for (FuncDef funcDef : funcDefs) {
            funcDef.analyze(writer);
        }
        mainFuncDef.analyze(writer);
        writer.println(this);
    }

    @Override
    public String toString() {
        return "<CompUnit>";
    }

}
