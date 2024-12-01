package middleend.LLVM_components;

import middleend.type.LLVMType;

import java.io.PrintWriter;

public class FunctionParam extends IrValue {

    public FunctionParam(LLVMType type) {
        super(type);
    }

    @Override
    public String getName() {
        return "%t" + super.getName();
    }

    public void dump(PrintWriter writer) {
        writer.printf("%s %s", getTypeOfValue().toString(), getName());
    }
}
