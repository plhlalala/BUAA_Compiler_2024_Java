package middleend.LLVM_components;

import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class GlobalValue extends Value {
    private final ArrayList<Integer> initVals = new ArrayList<>();
    private LLVMType type;

    public GlobalValue(LLVMType type, ArrayList<Integer> initVals) {
        super(type.getTypeClone().addPtr());
        this.type = type;
        this.initVals.addAll(initVals);
    }

    @Override
    public String getName() {
        return "@" + super.getName();
    }

    public void dump(PrintWriter writer) {
        writer.printf("%s = dso_local global %s\n", getName(), type.initValuesToString(initVals));
    }
}
