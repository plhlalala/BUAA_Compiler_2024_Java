package middleend.LLVM_components;

import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class Module extends Value {
    private final ArrayList<GlobalValue> globalVariables;
    private final ArrayList<Function> functions;
    private Function mainFunction;

    public Module() {
        super(new BasicType(BaseTypeEnum.VOID, 1));
        globalVariables = new ArrayList<>();
        functions = new ArrayList<>();
        mainFunction = null;
    }

    public Value createGlobalValue(LLVMType type, ArrayList<Integer> initVals) {
        GlobalValue globalValue = new GlobalValue(type, initVals);
        globalVariables.add(globalValue);
        return globalValue;
    }

    public Value createFunction(LLVMType returnType, ArrayList<LLVMType> params, String name) {
        ArrayList<FunctionParam> functionParams = new ArrayList<>();
        for (LLVMType param : params) {
            functionParams.add(new FunctionParam(param));
        }
        Function function = new Function(returnType, functionParams, name);
        functions.add(function);
        return function;
    }

    public Value createMainFunc() {
        Function function = new Function(new BasicType(BaseTypeEnum.INT, 0), new ArrayList<>(), "main");
        mainFunction = function;
        return function;
    }

    public void dump(PrintWriter writer) {
        writer.println("declare i32 @getint()");
        writer.println("declare i32 @getchar()");
        writer.println("declare void @putint(i32)");
        writer.println("declare void @putch(i32)");
        writer.println("declare void @putstr(i8*)");
        writer.println();

        for (GlobalValue globalValue : globalVariables) {
            globalValue.dump(writer);
        }
        writer.println();
        for (Function function : functions) {
            function.dump(writer);
            writer.println();
        }
        mainFunction.dump(writer);
    }
}
