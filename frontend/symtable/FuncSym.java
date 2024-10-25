package frontend.symtable;

import frontend.lexer.LexType;

import java.util.ArrayList;

public class FuncSym extends Symbol {
    public ArrayList<VarType> paramTypeList = new ArrayList<>(); // int or char
    public LexType retType; // int or char or void

    @Override
    public String toString() {
        if (retType == LexType.VOIDTK) {
            return ident + " VoidFunc";
        } else if (retType == LexType.INTTK) {
            return ident + " IntFunc";
        } else {
            return ident + " CharFunc";
        }
    }
}
