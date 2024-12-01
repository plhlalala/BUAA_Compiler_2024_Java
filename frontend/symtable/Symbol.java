package frontend.symtable;

import middleend.LLVM_components.IrValue;

public abstract class Symbol {
    public String ident;
    public SymTable table;
    public IrValue irValue;
}
