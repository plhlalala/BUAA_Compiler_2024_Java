package middleend;

import frontend.lexer.LexType;
import frontend.parser.components.Block;
import frontend.parser.components.BlockItem;
import frontend.parser.components.CompUnit;
import frontend.parser.components.Decl.ConstDecl;
import frontend.parser.components.Decl.ConstDef;
import frontend.parser.components.Decl.Decl;
import frontend.parser.components.Decl.VarDecl;
import frontend.parser.components.Decl.VarDef;
import frontend.parser.components.Exp.AddExp;
import frontend.parser.components.Exp.Cond;
import frontend.parser.components.Exp.ConstExp;
import frontend.parser.components.Exp.ConstInitVal;
import frontend.parser.components.Exp.EqExp;
import frontend.parser.components.Exp.Exp;
import frontend.parser.components.Exp.FuncRParams;
import frontend.parser.components.Exp.InitVal;
import frontend.parser.components.Exp.LAndExp;
import frontend.parser.components.Exp.LOrExp;
import frontend.parser.components.Exp.LVal;
import frontend.parser.components.Exp.MulExp;
import frontend.parser.components.Exp.PrimaryExp;
import frontend.parser.components.Exp.RelExp;
import frontend.parser.components.Exp.UnaryExp;
import frontend.parser.components.Func.FuncDef;
import frontend.parser.components.Func.FuncFParam;
import frontend.parser.components.Func.FuncFParams;
import frontend.parser.components.Func.MainFuncDef;
import frontend.parser.components.Stmt.AssignLval_stmt;
import frontend.parser.components.Stmt.Block_stmt;
import frontend.parser.components.Stmt.Break_Continue_stmt;
import frontend.parser.components.Stmt.Exp_stmt;
import frontend.parser.components.Stmt.ForStmt;
import frontend.parser.components.Stmt.For_stmt;
import frontend.parser.components.Stmt.GetChar_stmt;
import frontend.parser.components.Stmt.GetInt_stmt;
import frontend.parser.components.Stmt.If_stmt;
import frontend.parser.components.Stmt.Print_stmt;
import frontend.parser.components.Stmt.Return_stmt;
import frontend.parser.components.Stmt.Stmt;
import frontend.symtable.FuncSym;
import frontend.symtable.SymTable;
import frontend.symtable.Symbol;
import frontend.symtable.VarSym;
import frontend.symtable.VarType;
import frontend.visitor.VisitResult;
import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.ImmValueBool;
import middleend.LLVM_components.ImmValueI32;
import middleend.LLVM_components.Module;
import middleend.LLVM_components.Value;
import middleend.instruction.BrInstr;
import middleend.instruction.IcmpCondEnum;
import middleend.type.ArrayType;
import middleend.type.BaseTypeEnum;
import middleend.type.BasicType;
import middleend.type.LLVMType;

import java.util.ArrayList;
import java.util.LinkedList;

// 统一遵循符号表的切换在进入新作用域前进行
public class Visitor_IR {
    public SymTable symTable = new SymTable();
    public SymTable globalTable = symTable;
    public SymTable curTable = symTable;
    public int blockLoopLevel = 0;

    public middleend.LLVM_components.Module module = new Module();
    public Function curFunc = null;
    public BasicBlock curBlock = null;

    public LinkedList<ArrayList<BrInstr>> breakInstr = new LinkedList<>();
    public LinkedList<ArrayList<BrInstr>> continueInstr = new LinkedList<>();

    public Visitor_IR() {
    }

    public void visitCompUnit(CompUnit compUnit) {
        for (Decl decl : compUnit.decls) {
            visitDecl(decl);
        }
        for (FuncDef funcDef : compUnit.funcDefs) {
            visitFuncDef(funcDef);
        }
        visitMainFuncDef(compUnit.mainFuncDef);
    }

    public void visitMainFuncDef(MainFuncDef mainFuncDef) {
        curTable = curTable.createChild(); // 进入main函数作用域,切换到新的符号表
        curFunc = (Function) module.createMainFunc();
        curBlock = curFunc.createBasicBlock();
        visitBlock(mainFuncDef.block);
        curTable = curTable.parent;
    }

    public void visitFuncDef(FuncDef funcDef) {
        String ident = funcDef.ident;
        FuncSym funcSym = new FuncSym();
        funcSym.ident = ident;
        funcSym.retType = funcDef.funcType.type;
        curTable.add(funcSym);
        curTable = curTable.createChild(); // 进入函数作用域,切换到新的符号表
        if (funcDef.funcFParams != null && funcDef.funcFParams.FParams != null) { // 疑似可以删除后一个null的判断
            VisitResult tmp = visitFuncFParams(funcDef.funcFParams);
            funcSym.paramTypeList.addAll(tmp.paraTypeList);
        }
        // LLVM IR 创建function的Value并切换Function和BasicBlock
        ArrayList<LLVMType> functionParamsType = new ArrayList<>();
        for (VarType varType : funcSym.paramTypeList) {
            if (varType.isArray) {
//                    functionParamsType.add(new ArrayType(new BasicType(varType.type == LexType.INTTK ? BaseTypeEnum.INT : BaseTypeEnum.CHAR, 0), 114, 0));
                // 大小为0数组的一维指针 和 普通变量的一维指针 有区别吗？
                functionParamsType.add(new BasicType(varType.type == LexType.INTTK ? BaseTypeEnum.INT : BaseTypeEnum.CHAR, 1));
            } else {
                functionParamsType.add(new BasicType(varType.type == LexType.INTTK ? BaseTypeEnum.INT : BaseTypeEnum.CHAR, 0));
            }
        }
        LLVMType retType = funcSym.retType.equals(LexType.VOIDTK) ? new BasicType(BaseTypeEnum.VOID, 0) :
                new BasicType(funcSym.retType.equals(LexType.INTTK) ? BaseTypeEnum.INT : BaseTypeEnum.CHAR, 0);
        curFunc = (Function) (module.createFunction(retType, functionParamsType, funcSym.ident));
        funcSym.irValue = curFunc;
        curBlock = curFunc.createBasicBlock();
        // 创建参数的Value,并为Table中的VarSym赋IrValue
        if (funcDef.funcFParams != null) {
            for (int i = 0; i < funcSym.paramTypeList.size(); i++) {
                Value varValue = curFunc.getParams().get(i);
                VarType varType = funcSym.paramTypeList.get(i);
                BasicType basicType = new BasicType(varType.type == LexType.INTTK ? BaseTypeEnum.INT : BaseTypeEnum.CHAR, 0);
                Value valuePtr;
                if (varType.isArray) {
//                        functionParamsType.add(new BasicType(varType.type == LexType.INTTK ? BaseTypeEnum.INT : BaseTypeEnum.CHAR, 1));
                    valuePtr = curFunc.getBasicBlocks().get(0).createAllocatInstrInFront(basicType.getTypeClone().addPtr());
                    // alloca 会为 i32* 分配空间, ptr是i32**类型
                } else {
                    valuePtr = curFunc.getBasicBlocks().get(0).createAllocatInstrInFront(basicType);
                    // alloca 会为 i32 分配空间, ptr是i32*类型
                }
                curBlock.createStoreInstr(varValue, valuePtr);
                String paraIdent = funcDef.funcFParams.FParams.get(i).ident;
                curTable.get(paraIdent).irValue = valuePtr;
            }
        }
        if ((funcDef.block == null)) {
            new VisitResult();
        } else {
            visitBlock(funcDef.block);
        }
        curTable = curTable.parent;
        curBlock = null;
        curFunc = null;
    }

    public void visitDecl(Decl decl) {
        if (decl.constDecl != null) {
            visitConstDecl(decl.constDecl);
        } else {
            visitVarDecl(decl.varDecl);
        }
    }

    public void visitConstDecl(ConstDecl constDecl) {
        LexType type = constDecl.type.type;
        for (ConstDef constDef : constDecl.constDefs) {
            visitConstDef(constDef, type);
        }
    }

    public void visitConstDef(ConstDef constdef, LexType type) {
        assert type == LexType.INTTK || type == LexType.CHARTK;
        VarSym varSym = AddVarSymToTable(type, constdef.ident, constdef.constExp);
        varSym.isConst = true;
        curTable.add(varSym);
        checkAndInitArraySize(varSym, constdef.constExp);
        LLVMType llvmType = getllvmTypeOfVarSym(type, varSym);
        // LLVM IR
        if (curTable.equals(globalTable)) {
            if (constdef.constInitVal != null) {
                VisitResult tmp = visitConstInitVal(constdef.constInitVal);
                varSym.valueList.addAll(tmp.integerList);
                if (type == LexType.CHARTK && varSym.varType.isArray) {
                    int len = varSym.arraySize - tmp.integerList.size();
                    for (int i = 0; i < len; i++) {
                        varSym.valueList.add(0);
                    }
                }
            }
            Value globalValue = module.createGlobalValue(llvmType, varSym.valueList);
            globalValue.setName(varSym.ident);
            varSym.irValue = globalValue;
        } else {
            varSym.irValue = curFunc.getBasicBlocks().get(0).createAllocatInstrInFront(llvmType);
            if (constdef.constInitVal != null) {
                VisitResult tmp = visitConstInitVal(constdef.constInitVal);
                varSym.valueList.addAll(tmp.integerList);
                if (type == LexType.CHARTK && varSym.varType.isArray) {
                    int len = varSym.arraySize - tmp.integerList.size();
                    for (int i = 0; i < len; i++) {
                        varSym.valueList.add(0);
                    }
                }
                initConstAndVar(varSym, tmp);
            }
        }
    }

    private VarSym AddVarSymToTable(LexType type, String ident, ConstExp constExp) {
        assert type == LexType.INTTK || type == LexType.CHARTK;
        VarType varType = new VarType();
        varType.type = type;
        varType.isArray = (constExp != null);
        return new VarSym(ident, varType);
    }

    private LLVMType getllvmTypeOfVarSym(LexType type, VarSym varSym) {
        BasicType basicType = new BasicType(type == LexType.INTTK ? BaseTypeEnum.INT : BaseTypeEnum.CHAR, 0);
        return varSym.varType.isArray ? new ArrayType(basicType, varSym.arraySize, 0) : basicType;
    }

    private void initConstAndVar(VarSym varSym, VisitResult tmp) {
        if (varSym.varType.isArray) {
            for (int i = 0; i < tmp.irValueList.size(); i++) {
                Value ptr = curBlock.createGetElementPtrInstr(varSym.irValue, getArrayStartFromZero(new ImmValueI32(i)));
                Value value = tmp.irValueList.get(i);
                typeConvertAndStore(ptr, value);
            }
        } else {
            Value ptr = varSym.irValue;
            Value value = tmp.irValueList.get(0);
            typeConvertAndStore(ptr, value);
        }
    }

    private void checkAndInitArraySize(VarSym varSym, ConstExp constExp) {
        if (varSym.varType.isArray) {
            varSym.arraySize = visitConstExp(constExp).constInt;
        }
    }

    private void typeConvertAndStore(Value ptr, Value value) {
        if (ptr.getTypeOfValue().getBaseType() != value.getTypeOfValue().getBaseType()) {
            if (ptr.getTypeOfValue().getBaseType() == BaseTypeEnum.INT) {
                value = curBlock.createZextInstr(new BasicType(BaseTypeEnum.INT, 0), value);
            } else {
                value = curBlock.createTruncInstr(new BasicType(BaseTypeEnum.CHAR, 0), value);
            }
        }
        curBlock.createStoreInstr(value, ptr);
    }

    public VisitResult visitConstExp(ConstExp constExp) {
        return visitAddExp(constExp.addExp);
    }

    public void visitVarDecl(VarDecl varDecl) {
        LexType type = varDecl.bType.type;
        for (VarDef varDef : varDecl.varDefs) {
            visitVarDef(varDef, type);
        }
    }

    public void visitVarDef(VarDef varDef, LexType type) {
        assert type == LexType.INTTK || type == LexType.CHARTK;
        VarSym varSym = AddVarSymToTable(type, varDef.ident, varDef.constExp);
        varSym.isConst = false;
        curTable.add(varSym);
        checkAndInitArraySize(varSym, varDef.constExp);
        LLVMType llvmType = getllvmTypeOfVarSym(type, varSym);
        // LLVM IR
        if (curTable.equals(globalTable)) {
            if (varDef.initVal != null) {
                VisitResult tmp = visitInitVal(varDef.initVal);
                varSym.valueList.addAll(tmp.integerList);
            }
            Value globalValue = module.createGlobalValue(llvmType, varSym.valueList);
            globalValue.setName(varSym.ident);
            varSym.irValue = globalValue;
        } else {
            varSym.irValue = curFunc.getBasicBlocks().get(0).createAllocatInstrInFront(llvmType);
            if (varDef.initVal != null) {
                VisitResult tmp = visitInitVal(varDef.initVal);
                varSym.valueList.addAll(tmp.integerList);
                initConstAndVar(varSym, tmp);
            }
        }
    }

    public VisitResult visitConstInitVal(ConstInitVal constInitVal) {
        VisitResult visitResult = new VisitResult();
        if (constInitVal.stringConst != null) {
            for (char c : constInitVal.stringConst.toCharArray()) {
                visitResult.integerList.add((int) c);
                visitResult.irValueList.add(new ImmValueI32(c));
            }
        } else if (constInitVal.constExps != null) {
            for (ConstExp constExp : constInitVal.constExps) {
                VisitResult tmp = visitConstExp(constExp);
                visitResult.integerList.add(tmp.constInt);
                visitResult.irValueList.add(tmp.irValue);
            }
        } else {
            return null;
        }
        return visitResult;
    }

    public VisitResult visitInitVal(InitVal initVal) {
        VisitResult visitResult = new VisitResult();
        if (initVal.stringConst != null) {
            for (char c : initVal.stringConst.toCharArray()) {
                visitResult.integerList.add((int) c);
                visitResult.irValueList.add(new ImmValueI32(c));
            }
        } else if (initVal.exps != null) {
            for (Exp exp : initVal.exps) {
                VisitResult tmp = visitExp(exp);
                visitResult.integerList.add(tmp.constInt);
                visitResult.irValueList.add(tmp.irValue);
            }
        } else {
            return null;
        }
        return visitResult;
    }

    public VisitResult visitBlock(Block block) {
        for (BlockItem blockItem : block.blockItems) {
            visitBlockItem(blockItem);
        }
        return new VisitResult();
    }

    public void visitBlockItem(BlockItem blockItem) {
        if (blockItem.decl != null) {
            visitDecl(blockItem.decl);
        } else {
            visitStmt(blockItem.stmt);
        }
    }

    public VisitResult visitStmt(Stmt stmt) {
        if (stmt instanceof AssignLval_stmt) {
            return visitAssignLval_stmt((AssignLval_stmt) stmt);
        } else if (stmt instanceof Block_stmt) {
            curTable = curTable.createChild();
            VisitResult visitResult = visitBlock(((Block_stmt) stmt).block);
            curTable = curTable.parent;
            return visitResult;
        } else if (stmt instanceof Break_Continue_stmt stmt1) {
            if (blockLoopLevel == 0) {
                System.out.println("break or continue not in loop");
            } else {
                BrInstr brInstr = (BrInstr) (curBlock.createBrInstr(null));
                if (stmt1.type == LexType.BREAKTK) {
                    breakInstr.getLast().add(brInstr);
                } else {
                    continueInstr.getLast().add(brInstr);
                }
                curBlock = curFunc.createBasicBlock();
            }
        } else if (stmt instanceof Exp_stmt) {
            if (((Exp_stmt) stmt).exp != null) {
                return visitExp(((Exp_stmt) stmt).exp);
            }
        } else if (stmt instanceof For_stmt) {
            return visitLoopStmt((For_stmt) stmt);
        } else if (stmt instanceof GetChar_stmt || stmt instanceof GetInt_stmt) {
            visitInputStmt(stmt);
        } else if (stmt instanceof If_stmt) {
            return visitIfStmt((If_stmt) stmt);
        } else if (stmt instanceof Print_stmt) {
            visitPrintStmt(stmt);
        } else if (stmt instanceof Return_stmt return_stmt) {
            VisitResult expResult = null;
            if (return_stmt.exp != null) {
                expResult = visitExp(return_stmt.exp);
                BaseTypeEnum ret = curFunc.getReturnBaseType();
                if (ret != null) {
                    BaseTypeEnum valueTypeEnum = expResult.irValue.getTypeOfValue().getBaseType();
                    if (ret != valueTypeEnum) {
                        if (ret == BaseTypeEnum.INT) {
                            expResult.irValue = curBlock.createZextInstr(new BasicType(BaseTypeEnum.INT, 0), expResult.irValue);
                        } else {
                            expResult.irValue = curBlock.createTruncInstr(new BasicType(BaseTypeEnum.CHAR, 0), expResult.irValue);
                        }
                    }
                }
            }
            curBlock.createReturnInstr(expResult == null ? null : expResult.irValue);
        } else {
            throw new RuntimeException("Unknown Stmt type");
        }
        return new VisitResult();
    }

    private void visitPrintStmt(Stmt stmt) {
        Print_stmt print_stmt = (Print_stmt) stmt;
        ArrayList<Value> args = new ArrayList<>();
        if (print_stmt.exps != null) {
            for (Exp exp : print_stmt.exps) {
                args.add(visitExp(exp).irValue);
            }
        }
        int index = 0;
        for (int i = 0; i < print_stmt.stringConst.length(); i++) {
            if (print_stmt.stringConst.charAt(i) == '%') {
                if (args.get(index).getTypeOfValue().getTypeClone().getBaseType() == BaseTypeEnum.CHAR) {
                    args.set(index, curBlock.createZextInstr(new BasicType(BaseTypeEnum.INT, 0), args.get(index)));
                }
                if (print_stmt.stringConst.charAt(i + 1) == 'd') {
                    curBlock.createCallInstr(Function.IRFUNC_PUTINT, getArray(args.get(index)));
                } else if (print_stmt.stringConst.charAt(i + 1) == 'c') {
                    curBlock.createCallInstr(Function.IRFUNC_PUTCHAR, getArray(args.get(index)));
                }
                index++;
                i++;
            } else {
                curBlock.createCallInstr(Function.IRFUNC_PUTCHAR, getArray(new ImmValueI32(print_stmt.stringConst.charAt(i))));
            }
        }
    }

    private VisitResult visitIfStmt(If_stmt stmt) {
        VisitResult result = new VisitResult();
        VisitResult condResult = visitCond(stmt.cond);

        BasicBlock trueEntry = curBlock;
        visitStmt(stmt.stmt);  // 访问True语句块
        BasicBlock trueOut = curBlock;
        curBlock = curFunc.createBasicBlock();

        BasicBlock falseEntry = null;
        BasicBlock falseOut = null;
        if (stmt.elseStmt != null) {
            falseEntry = curBlock;
            visitStmt(stmt.elseStmt); // 访问False语句块
            falseOut = curBlock;
            curBlock = curFunc.createBasicBlock();
        }

        BasicBlock endBlock = curBlock;
        trueOut.createBrInstr(endBlock);
        if (falseOut != null) {
            falseOut.createBrInstr(endBlock);
        }

        for (BasicBlock block : condResult.ifTrueNeedToJumpOut) {
            ((BrInstr) (block.getLastInstruction())).setTrueBranch(trueEntry);
        }
        for (BasicBlock block : condResult.ifFalseNeedToJumpNext) {
            ((BrInstr) (block.getLastInstruction())).setFalseBranch(falseEntry == null ? endBlock : falseEntry);
        }
        return result;
    }

    private void visitInputStmt(Stmt stmt) {
        LVal input = stmt instanceof GetChar_stmt ? ((GetChar_stmt) stmt).lval : ((GetInt_stmt) stmt).lval;
        VisitResult lvalResult = visitLVal(input);
        Value ptr = lvalResult.irValue;
        String ident = input.ident;
        VarSym varSym = (VarSym) curTable.get(ident);
        if (stmt instanceof GetInt_stmt) {
            Value value = curBlock.createCallInstr(Function.IRFUNC_GETINT, new ArrayList<>());
            if (varSym != null) {
                curBlock.createStoreInstr(value, ptr);
            }
        } else {
            Value value = curBlock.createCallInstr(Function.IRFUNC_GETCHAR, new ArrayList<>());
            if (varSym != null) {
                value = curBlock.createTruncInstr(new BasicType(BaseTypeEnum.CHAR, 0), value);
                curBlock.createStoreInstr(value, varSym.irValue);
            }
        }
    }

    private VisitResult visitLoopStmt(For_stmt stmt) {
        breakInstr.add(new ArrayList<>());
        continueInstr.add(new ArrayList<>());

        BasicBlock entryBlock = curBlock;
        if (stmt.fotStmt1 != null) {
            visitForStmt(stmt.fotStmt1);
        }

        curBlock = curFunc.createBasicBlock();
        BasicBlock condEntryBlock = curBlock;
        VisitResult condResult = new VisitResult();
        if (stmt.cond != null) {
            condResult = visitCond(stmt.cond);
        }

        BasicBlock bodyBlock = curBlock;
        blockLoopLevel += 1;
        VisitResult visitResult = visitStmt(stmt.stmt);
        blockLoopLevel -= 1;
        BasicBlock bodyOutBlock = curBlock;

        curBlock = curFunc.createBasicBlock();
        BasicBlock incEntryBlock = curBlock;
        if (stmt.forStmt2 != null) {
            visitForStmt(stmt.forStmt2);
        }

        curBlock = curFunc.createBasicBlock();
        BasicBlock endBlock = curBlock;

        entryBlock.createBrInstr(condEntryBlock);
        bodyOutBlock.createBrInstr(incEntryBlock);
        incEntryBlock.createBrInstr(condEntryBlock);

        for (BasicBlock block : condResult.ifTrueNeedToJumpOut) {
            BrInstr brInstr = (BrInstr) (block.getLastInstruction());
            brInstr.setTrueBranch(bodyBlock);
        }
        for (BasicBlock block : condResult.ifFalseNeedToJumpNext) {
            BrInstr brInstr = (BrInstr) (block.getLastInstruction());
            brInstr.setFalseBranch(endBlock);
        }

        for (BrInstr breakBrInstr : breakInstr.getLast()) {
            breakBrInstr.setDest(endBlock);
        }
        breakInstr.removeLast();
        for (BrInstr continueBrInstr : continueInstr.getLast()) {
            continueBrInstr.setDest(incEntryBlock);
        }
        continueInstr.removeLast();
        return visitResult;
    }

    public void visitForStmt(ForStmt forStmt) {
        VisitResult lvalResult = visitLVal(forStmt.lval);
        VisitResult expResult = visitExp(forStmt.exp);
        Value ptr = lvalResult.irValue;
        Value value = expResult.irValue;
        typeConvertAndStore(ptr, value);
    }

    public VisitResult visitCond(Cond cond) {
        if (cond.lOrExp != null) { //可能可以删除
            return visitLOrExp(cond.lOrExp);
        } else {
            return new VisitResult();
        }
    }

    public VisitResult visitAssignLval_stmt(AssignLval_stmt assignLval_stmt) {
        VisitResult tmp = visitLVal(assignLval_stmt.lval);
        if (tmp.irValue != null) {
            Value lvalPtr = tmp.irValue;
            VisitResult tmp2 = visitExp(assignLval_stmt.exp);
            Value value = tmp2.irValue;
            typeConvertAndStore(lvalPtr, value);
        }
        return new VisitResult();
    }

    public VisitResult visitLOrExp(LOrExp lOrExp) {
        if (lOrExp.lOrExp == null) {
            VisitResult result = new VisitResult();
            VisitResult tmp = visitLAndExp(lOrExp.lAndExp);
            result.ifTrueNeedToJumpOut.add(tmp.getLastAndBlock());
            result.ifFalseNeedToJumpNext.addAll(tmp.andBlocks);
            return result;
        } else {
            VisitResult visitResult = new VisitResult();
            VisitResult tmp = visitLOrExp(lOrExp.lOrExp);
            VisitResult tmp2 = visitLAndExp(lOrExp.lAndExp);

            visitResult.ifTrueNeedToJumpOut.addAll(tmp.ifTrueNeedToJumpOut);
            visitResult.ifTrueNeedToJumpOut.add(tmp2.getLastAndBlock());
            visitResult.ifFalseNeedToJumpNext.addAll(tmp2.andBlocks);

            BasicBlock next = tmp2.andBlocks.get(0);
            for (BasicBlock block : tmp.ifFalseNeedToJumpNext) {
                BrInstr brInstr1 = (BrInstr) (block.getLastInstruction());
                brInstr1.setFalseBranch(next);
            }
            return visitResult;
        }
    }

    public VisitResult visitLAndExp(LAndExp lAndExp) {
        if (lAndExp.lAndExp == null) {
            VisitResult result = new VisitResult();
            VisitResult tmp = visitEqExp(lAndExp.eqExp);
            if (tmp.irValue.getTypeOfValue().getBaseType() != BaseTypeEnum.BOOL) {
                if (tmp.constInt != null) {
                    tmp.irValue = tmp.constInt == 0 ? new ImmValueBool(0) : new ImmValueBool(1);
                } else {
                    tmp.irValue = curBlock.createICmpInstr(IcmpCondEnum.NE, tmp.irValue, new ImmValueI32(0));
                }
            }
            curBlock.createBrInstr(null, null, tmp.irValue);
            result.andBlocks.add(curBlock);
            curBlock = curFunc.createBasicBlock(); // 一个And对应一个Block
            return result;
        } else {
            VisitResult visitResult = new VisitResult();
            VisitResult tmp = visitLAndExp(lAndExp.lAndExp); // 递归调用,返回后已位于新的Block
            VisitResult tmp2 = visitEqExp(lAndExp.eqExp);

            BasicBlock last = tmp.getLastAndBlock();
            BrInstr brInstr = (BrInstr) (last.getLastInstruction());
            brInstr.setTrueBranch(curBlock);
            if (tmp2.irValue.getTypeOfValue().getBaseType() != BaseTypeEnum.BOOL) {
                if (tmp2.constInt != null) {
                    tmp2.irValue = tmp2.constInt == 0 ? new ImmValueBool(0) : new ImmValueBool(1);
                } else {
                    tmp2.irValue = curBlock.createICmpInstr(IcmpCondEnum.NE, tmp2.irValue, new ImmValueI32(0));
                }
            }
            curBlock.createBrInstr(null, null, tmp2.irValue);
            visitResult.andBlocks.addAll(tmp.andBlocks);
            visitResult.andBlocks.add(curBlock);
            curBlock = curFunc.createBasicBlock(); // 处理完一个And后,新建一个Block
            return visitResult;
        }
    }

    public VisitResult visitEqExp(EqExp eqExp) {
        if (eqExp.eqExp == null) {
            return visitRelExp(eqExp.relExp);
        } else {
            VisitResult visitResult = new VisitResult();
            VisitResult tmp = visitEqExp(eqExp.eqExp);
            VisitResult tmp2 = visitRelExp(eqExp.relExp);
            if (tmp.constInt != null && tmp2.constInt != null) {
                if (eqExp.type.equals(LexType.EQL)) {
                    visitResult.constInt = tmp.constInt.compareTo(tmp2.constInt) == 0 ? 1 : 0;
                    visitResult.irValue = new ImmValueBool(visitResult.constInt);
                } else if (eqExp.type.equals(LexType.NEQ)) {
                    visitResult.constInt = tmp.constInt.compareTo(tmp2.constInt) == 0 ? 0 : 1;
                    visitResult.irValue = new ImmValueBool(visitResult.constInt);
                }
            } else {
                convertToI32(tmp);
                convertToI32(tmp2);
                if (eqExp.type.equals(LexType.EQL)) {
                    visitResult.irValue = curBlock.createICmpInstr(IcmpCondEnum.EQ, tmp.irValue, tmp2.irValue);
                } else if (eqExp.type.equals(LexType.NEQ)) {
                    visitResult.irValue = curBlock.createICmpInstr(IcmpCondEnum.NE, tmp.irValue, tmp2.irValue);
                }
            }
            return visitResult;
        }
    }

    public VisitResult visitRelExp(RelExp relExp) {
        if (relExp.relExp == null) {
            return visitAddExp(relExp.addExp);
        } else {
            VisitResult visitResult = new VisitResult();
            VisitResult tmp = visitRelExp(relExp.relExp);
            VisitResult tmp2 = visitAddExp(relExp.addExp);
            if (tmp.constInt != null && tmp2.constInt != null) {
                if (relExp.type.equals(LexType.LSS)) {
                    visitResult.constInt = tmp.constInt < tmp2.constInt ? 1 : 0;
                    visitResult.irValue = new ImmValueBool(visitResult.constInt);
                } else if (relExp.type.equals(LexType.LEQ)) {
                    visitResult.constInt = tmp.constInt <= tmp2.constInt ? 1 : 0;
                    visitResult.irValue = new ImmValueBool(visitResult.constInt);
                } else if (relExp.type.equals(LexType.GRE)) {
                    visitResult.constInt = tmp.constInt > tmp2.constInt ? 1 : 0;
                    visitResult.irValue = new ImmValueBool(visitResult.constInt);
                } else if (relExp.type.equals(LexType.GEQ)) {
                    visitResult.constInt = tmp.constInt >= tmp2.constInt ? 1 : 0;
                    visitResult.irValue = new ImmValueBool(visitResult.constInt);
                }
            } else {
                convertToI32(tmp);
                convertToI32(tmp2);
                if (relExp.type.equals(LexType.LSS)) {
                    visitResult.irValue = curBlock.createICmpInstr(IcmpCondEnum.SLT, tmp.irValue, tmp2.irValue);
                } else if (relExp.type.equals(LexType.LEQ)) {
                    visitResult.irValue = curBlock.createICmpInstr(IcmpCondEnum.SLE, tmp.irValue, tmp2.irValue);
                } else if (relExp.type.equals(LexType.GRE)) {
                    visitResult.irValue = curBlock.createICmpInstr(IcmpCondEnum.SGT, tmp.irValue, tmp2.irValue);
                } else if (relExp.type.equals(LexType.GEQ)) {
                    visitResult.irValue = curBlock.createICmpInstr(IcmpCondEnum.SGE, tmp.irValue, tmp2.irValue);
                }
            }
            return visitResult;
        }
    }

    public VisitResult visitAddExp(AddExp addExp) {
        if (addExp.addExp == null) {
            return visitMulExp(addExp.mulExp);
        } else {
            VisitResult visitResult = new VisitResult();
            VisitResult tmp = visitAddExp(addExp.addExp);
            VisitResult tmp2 = visitMulExp(addExp.mulExp);
            visitResult.varType = tmp.varType;
            if (tmp.constInt != null && tmp2.constInt != null) {
                if (addExp.op.equals(LexType.PLUS)) {
                    visitResult.constInt = tmp.constInt + tmp2.constInt;
                    visitResult.irValue = new ImmValueI32(visitResult.constInt);
                } else if (addExp.op.equals(LexType.MINU)) {
                    visitResult.constInt = tmp.constInt - tmp2.constInt;
                    visitResult.irValue = new ImmValueI32(visitResult.constInt);
                }
            } else {
                convertToI32(tmp);
                convertToI32(tmp2);
                if (addExp.op.equals(LexType.PLUS)) {
                    visitResult.irValue = curBlock.createAddInstr(tmp.irValue, tmp2.irValue);
                } else if (addExp.op.equals(LexType.MINU)) {
                    visitResult.irValue = curBlock.createSubInstr(tmp.irValue, tmp2.irValue);
                }
            }
            return visitResult;
        }
    }

    public VisitResult visitMulExp(MulExp mulExp) {
        if (mulExp.mulExp == null) {
            return visitUnaryExp(mulExp.unaryExp);
        } else {
            VisitResult visitResult = new VisitResult();
            VisitResult tmp = visitMulExp(mulExp.mulExp);
            VisitResult tmp2 = visitUnaryExp(mulExp.unaryExp);
            visitResult.varType = tmp.varType;

            if (tmp.constInt != null && tmp2.constInt != null) {
                if (mulExp.op.equals(LexType.MULT)) {
                    visitResult.constInt = tmp.constInt * tmp2.constInt;
                    visitResult.irValue = new ImmValueI32(visitResult.constInt);
                } else if (mulExp.op.equals(LexType.DIV)) {
                    visitResult.constInt = tmp.constInt / tmp2.constInt;
                    visitResult.irValue = new ImmValueI32(visitResult.constInt);
                } else if (mulExp.op.equals(LexType.MOD)) {
                    visitResult.constInt = tmp.constInt % tmp2.constInt;
                    visitResult.irValue = new ImmValueI32(visitResult.constInt);
                }
            } else {
                convertToI32(tmp);
                convertToI32(tmp2);
                if (mulExp.op.equals(LexType.MULT)) {
                    visitResult.irValue = curBlock.createMulInstr(tmp.irValue, tmp2.irValue);
                } else if (mulExp.op.equals(LexType.DIV)) {
                    visitResult.irValue = curBlock.createSDivInstr(tmp.irValue, tmp2.irValue);
                } else if (mulExp.op.equals(LexType.MOD)) {
                    visitResult.irValue = curBlock.createSRemInstr(tmp.irValue, tmp2.irValue);
                }
            }
            return visitResult;
        }
    }

    public void convertToI32(VisitResult visitResult) {
        if (visitResult.irValue.getTypeOfValue().getBaseType() != BaseTypeEnum.INT) {
            visitResult.irValue = curBlock.createZextInstr(new BasicType(BaseTypeEnum.INT, 0), visitResult.irValue);
        }
    }

    public VisitResult visitUnaryExp(UnaryExp unaryExp) {
        if (unaryExp.unaryExp != null) { // UnaryOp UnaryExp
            VisitResult result = visitUnaryExp(unaryExp.unaryExp);
            if (result.constInt != null) {
                if (unaryExp.op.type.equals(LexType.MINU)) {
                    result.constInt = -result.constInt;
                    result.irValue = new ImmValueI32(result.constInt);
                } else if (unaryExp.op.type.equals(LexType.NOT)) {
                    result.constInt = result.constInt == 0 ? 1 : 0;
                    result.irValue = new ImmValueI32(result.constInt);
                } else {
                    result.irValue = new ImmValueI32(result.constInt);
                }
            } else {
                if (unaryExp.op.type.equals(LexType.MINU)) {
                    result.irValue = curBlock.createSubInstr(new ImmValueI32(0), result.irValue);
                } else if (unaryExp.op.type.equals(LexType.NOT)) {
                    result.irValue = curBlock.createICmpInstr(IcmpCondEnum.EQ, result.irValue, new ImmValueI32(0));
                }
            }
            return result;
        } else if (unaryExp.primaryExp != null) {  // PrimaryExp
            return visitPrimaryExp(unaryExp.primaryExp);
        } else { // FuncCall
            Symbol sym = curTable.get(unaryExp.ident);
            FuncSym funcSym = (FuncSym) sym;

            VisitResult tmp = new VisitResult();
            if (unaryExp.funcRParams != null) {
                tmp = visitFuncRParams(unaryExp.funcRParams);
            }
            VisitResult visitResult = new VisitResult();
            visitResult.varType = new VarType();
            visitResult.varType.type = funcSym.retType;
            visitResult.varType.isArray = false;
            for (int i = 0; i < tmp.irValueList.size(); i++) { // 针对int8_t和int32_t的转换
                Value value = tmp.irValueList.get(i);
                BaseTypeEnum argType = value.getTypeOfValue().getBaseType();
                BaseTypeEnum paramType = funcSym.paramTypeList.get(i).type == LexType.INTTK ? BaseTypeEnum.INT : BaseTypeEnum.CHAR;
                if (argType != paramType) {
                    if (argType == BaseTypeEnum.INT) {
                        value = curBlock.createTruncInstr(new BasicType(BaseTypeEnum.CHAR, 0), value);
                    } else {
                        value = curBlock.createZextInstr(new BasicType(BaseTypeEnum.INT, 0), value);
                    }
                    tmp.irValueList.set(i, value);
                }
            }
            visitResult.irValue = curBlock.createCallInstr(((Function) funcSym.irValue), tmp.irValueList);
            return visitResult;
        }
    }

    public VisitResult visitPrimaryExp(PrimaryExp primaryExp) {
        VisitResult visitResult = new VisitResult();
        if (primaryExp.exp != null) {
            return visitExp(primaryExp.exp);
        } else if (primaryExp.lval != null) {
            VisitResult tmp = visitLVal(primaryExp.lval);
            if (curTable.get(primaryExp.lval.ident) instanceof VarSym varSym) {
                if (varSym.varType.isArray && primaryExp.lval.exp == null) {
                    // 返回的是数组首元素的指针，可能用作函数参数，不需要Load
                } else if (varSym.isConst && primaryExp.lval.exp != null) {
                    VisitResult a = visitExp(primaryExp.lval.exp);
                    if (a.constInt == null) {
                        tmp.irValue = curBlock.createLoadInstr(tmp.irValue);
                    } else {
                        tmp.irValue = new ImmValueI32(varSym.valueList.get(a.constInt));
                        tmp.constInt = varSym.valueList.get(a.constInt);
                    }
                } else if (varSym.isConst) {
                    tmp.irValue = new ImmValueI32(varSym.valueList.get(0));
                    tmp.constInt = varSym.valueList.get(0);
                } else {
                    tmp.irValue = curBlock.createLoadInstr(tmp.irValue);
                }
            }
            return tmp;
        } else if (primaryExp.number != null) {
            visitResult.varType = new VarType();
            visitResult.varType.type = LexType.INTTK;
            visitResult.constInt = Integer.parseInt(primaryExp.number.intConst);
            visitResult.irValue = new ImmValueI32(visitResult.constInt);
        } else {
            visitResult.varType = new VarType();
            visitResult.varType.type = LexType.CHARTK;
            visitResult.constInt = (int) primaryExp.character.charConst.charAt(0);
            visitResult.irValue = new ImmValueI32(visitResult.constInt);
        }
        return visitResult;
    }

    public VisitResult visitLVal(LVal lval) {  // 返回的value都是指针,constInt是访问Const的数组或者变量时的才有的
        VisitResult visitResult = new VisitResult();
        Symbol sym = curTable.get(lval.ident);
        VarSym varSym = (VarSym) sym;
        visitResult.varType = new VarType();
        visitResult.varType.type = varSym.varType.type; // int or char
        if (lval.exp != null) { // 数组的元素 或者 某个元素的指针加上偏移
            // 函数中使用参数数组,作为参数的数组IrValue是i32*,申请了一个地址为I32**的变量来存储数组首地址的指针
            // 返回的是数组的某个元素的指针, 根据需要后面再load
            visitResult.varType.isArray = false;
            VisitResult lvalExpResult = visitExp(lval.exp);
            if (lvalExpResult.constInt != null && varSym.isConst) { // 常量数组, 不会处理把常量数组当作参数传进去的情况
                visitResult.constInt = varSym.valueList.get(lvalExpResult.constInt);
                visitResult.irValue = new ImmValueI32(visitResult.constInt);
            } else {
                Value symIrValue = varSym.irValue;
                if (symIrValue.getTypeOfValue().getPtrNum() == 1) { // 如果是1,比如直接使用数组的某个元素
                    visitResult.irValue = curBlock.createGetElementPtrInstr(symIrValue, getArrayStartFromZero(lvalExpResult.irValue));
                } else {  // 如果是2,比如作为函数参数传递的数组,需要先load
                    Value load = curBlock.createLoadInstr(symIrValue);
                    visitResult.irValue = curBlock.createGetElementPtrInstr(load, getArray(lvalExpResult.irValue));
                }
            }
        } else if (varSym.varType.isArray) { //返回指针,要么是数组的首地址,要么是某个元素的指针
            visitResult.varType.isArray = true;
            if (varSym.irValue.getTypeOfValue().getPtrNum() == 2) { // 数组作为形参后,alloca 一个i32**指针,要取真实地址需要Load
                visitResult.irValue = curBlock.createLoadInstr(varSym.irValue);
            } else if (varSym.irValue.getTypeOfValue().getPtrNum() == 1) {  // 需要根据数组整体的起始地址得到首地址
                visitResult.irValue = curBlock.createGetElementPtrInstr(varSym.irValue, getArrayZeroToZero());
            } else {
                System.out.println("error in lval 数组指针数异常?");
                return visitResult;
            }
        } else { // 变量,返回的是变量的指针,根据需要后面再load
            visitResult.varType.isArray = false;
            if (varSym.isConst) {
                visitResult.irValue = new ImmValueI32(varSym.valueList.get(0));
            } else {
                visitResult.irValue = varSym.irValue;
                // 返回的是变量的指针
            }
        }
        return visitResult;
    }

    public VisitResult visitFuncRParams(FuncRParams funcRParams) {
        VisitResult visitResult = new VisitResult();
        for (Exp exp : funcRParams.exps) {
            VisitResult tmp = visitExp(exp);
            visitResult.paraTypeList.add(tmp.varType);
            visitResult.irValueList.add(tmp.irValue);
        }
        return visitResult;
    }

    public VisitResult visitFuncFParams(FuncFParams funcFParams) {
        VisitResult visitResult = new VisitResult();
        if (funcFParams.FParams != null) {
            for (FuncFParam funcFParam : funcFParams.FParams) {
                VisitResult tmp = visitFuncFParam(funcFParam);
                if (tmp != null && tmp.varType != null) {
                    visitResult.paraTypeList.add(tmp.varType);
                }
            }
        } else {
            System.out.println("funcFParams is null");
        }
        return visitResult;
    }

    public VisitResult visitFuncFParam(FuncFParam funcFParam) {
        String ident = funcFParam.ident;
        VarType varType = new VarType();
        varType.type = funcFParam.bType.type;
        varType.isArray = funcFParam.isArray;
        VarSym varSym = new VarSym(ident, varType);
        curTable.add(varSym);
        VisitResult visitResult = new VisitResult();
        visitResult.varType = varSym.varType;
        return visitResult;
    }

    public VisitResult visitExp(Exp exp) {
        return visitAddExp(exp.addExp);
    }

    public ArrayList<Value> getArrayStartFromZero(Value array) {
        ArrayList<Value> index = new ArrayList<>();
        index.add(new ImmValueI32(0));
        index.add(array);
        return index;
    }

    public ArrayList<Value> getArrayZeroToZero() {
        ArrayList<Value> index = new ArrayList<>();
        index.add(new ImmValueI32(0));
        index.add(new ImmValueI32(0));
        return index;
    }

    public ArrayList<Value> getArray(Value ele) {
        ArrayList<Value> indexList = new ArrayList<>();
        indexList.add(ele);
        return indexList;
    }
}
