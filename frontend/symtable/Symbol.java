package frontend.symtable;

import middleend.LLVM_components.Value;

public abstract class Symbol {
    public String ident;
    public SymTable table;
    public Value irValue;
}
