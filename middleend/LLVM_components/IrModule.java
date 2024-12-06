package middleend.LLVM_components;

import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;
import middleend.type.LLVMType;

import java.io.PrintWriter;
import java.util.ArrayList;

public class IrModule extends IrValue {
    private final ArrayList<GlobalIrValue> globalVariables;
    private final ArrayList<Function> functions;
    private Function mainFunction;

    public IrModule() {
        super(new BasicType(BaseTypeEnum.VOID, 1));
        globalVariables = new ArrayList<>();
        functions = new ArrayList<>();
        mainFunction = null;
    }

    public ArrayList<GlobalIrValue> getGlobalVariables() {
        return globalVariables;
    }

    public ArrayList<Function> getFunctionListWithMain() {
        ArrayList<Function> functionList = new ArrayList<>();
        functionList.addAll(functions);
        functionList.add(mainFunction);
        return functionList;
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public Function getMainFunction() {
        return mainFunction;
    }

    public IrValue createGlobalValue(LLVMType type, ArrayList<Integer> initVals) {
        GlobalIrValue globalValue = new GlobalIrValue(type, initVals);
        globalVariables.add(globalValue);
        return globalValue;
    }

    public IrValue createFunction(LLVMType returnType, ArrayList<LLVMType> params, String name) {
        ArrayList<FunctionParam> functionParams = new ArrayList<>();
        for (LLVMType param : params) {
            functionParams.add(new FunctionParam(param));
        }
        Function function = new Function(returnType, functionParams, name);
        functions.add(function);
        return function;
    }

    public IrValue createMainFunc() {
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

        for (GlobalIrValue globalValue : globalVariables) {
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
