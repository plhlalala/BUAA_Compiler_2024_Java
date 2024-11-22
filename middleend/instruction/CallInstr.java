package middleend.instruction;

import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.Value;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class CallInstr extends Instruction {
    private Function func;
    private ArrayList<Value> args;

    public CallInstr(Function func, ArrayList<Value> args, BasicBlock parentBasicBlock) {
        super(new BasicType(func.getReturnBaseType(), 0), args, parentBasicBlock);
        this.func = func;
        this.args = args;
    }

    // %call = call i32 @f(i32 %3, i32 %4)
    public void dump(PrintWriter writer) {
        writer.print("  ");
        if (super.getTypeOfValue().getTypeClone().getBaseType() != BaseTypeEnum.VOID) {
            writer.printf("%s = ", this.getName());
        }
        writer.printf("call %s %s(", super.getTypeOfValue().getTypeClone().toString(), func.toString());
        for (int i = 0; i < args.size(); i++) {
            if (i != 0) {
                writer.print(", ");
            }
            writer.printf("%s %s", args.get(i).getTypeOfValue(), args.get(i).getName());
        }
        writer.println(")");
    }


    public Function getFunc() {
        return func;
    }

    public ArrayList<Value> getArgs() {
        return args;
    }
}
