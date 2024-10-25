package frontend.visitor;

import frontend.error.ErrorRecord;
import frontend.error.ErrorType;
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

import java.util.ArrayList;

// 语义分析错误类型： b c d e f g h l m
// 统一遵循符号表的切换在进入新作用域前进行
public class Visitor {
    public ArrayList<ErrorRecord> errorRecords;
    public SymTable symTable = new SymTable();
    public SymTable curTable = symTable;
    public int loopLevel = 0;
    public ArrayList<SymTable> globalTable = new ArrayList<>();

    public Visitor(ArrayList<ErrorRecord> er) {
        errorRecords = er;
        globalTable.add(symTable);
    }

    private static boolean isHasError(VisitResult tmp, FuncSym funcSym, int paramNum) {
        ArrayList<VarType> realTypeList = tmp.paraTypeList;
        ArrayList<VarType> funcParams = funcSym.paramTypeList;
        boolean hasError = false;
        for (int i = 0; i < paramNum; i++) {
            if (realTypeList.get(i).isArray && !funcParams.get(i).isArray) {
                hasError = true;
            } else if (!realTypeList.get(i).isArray && funcParams.get(i).isArray) {
                hasError = true;
            } else if (realTypeList.get(i).isArray && funcParams.get(i).type != realTypeList.get(i).type) {
                hasError = true;
            }
        }
        return hasError;
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
        globalTable.add(curTable);
        VisitResult visitResult = visitBlock(mainFuncDef.block);
        if (!visitResult.hasReturnInLastSentence) {
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_RETURN, mainFuncDef.linenum));
        }
        curTable = curTable.parent;
    }

    public void visitFuncDef(FuncDef funcDef) {
        String ident = funcDef.ident;
        if (curTable.contain(ident)) {
            errorRecords.add(new ErrorRecord(ErrorType.NAME_REDEFINED, funcDef.identLinenum));
            curTable = curTable.createChild(); // 进入函数作用域,切换到新的符号表
            globalTable.add(curTable);
            if (funcDef.funcFParams != null && funcDef.funcFParams.FParams != null) {
                visitFuncFParams(funcDef.funcFParams);
            }
            if (funcDef.block != null) {
                visitBlock(funcDef.block);
            }
            VisitResult visitResult = funcDef.block == null ? new VisitResult() : visitBlock(funcDef.block);
            if (funcDef.funcType.type == LexType.VOIDTK && !visitResult.returnNotVoidLineNumber.isEmpty()) {
                for (int linenum : visitResult.returnNotVoidLineNumber) {
                    errorRecords.add(new ErrorRecord(ErrorType.VOID_RETURN_MISMATCH, linenum));
                }
            }
            if (funcDef.funcType.type != LexType.VOIDTK && !visitResult.hasReturnInLastSentence) {
                errorRecords.add(new ErrorRecord(ErrorType.MISSING_RETURN, funcDef.blockLinenum));
            }

        } else {
            FuncSym funcSym = new FuncSym();
            funcSym.ident = ident;
            funcSym.retType = funcDef.funcType.type;
            curTable.add(funcSym);
            curTable = curTable.createChild(); // 进入函数作用域,切换到新的符号表
            globalTable.add(curTable);
            if (funcDef.funcFParams != null && funcDef.funcFParams.FParams != null) {
                VisitResult tmp = visitFuncFParams(funcDef.funcFParams);
                funcSym.paramTypeList.addAll(tmp.paraTypeList);
            }
            curTable.parent.add(funcSym);
            VisitResult visitResult = (funcDef.block == null) ? new VisitResult() : visitBlock(funcDef.block);
            if (funcDef.funcType.type == LexType.VOIDTK && !visitResult.returnNotVoidLineNumber.isEmpty()) {
                for (int linenum : visitResult.returnNotVoidLineNumber) {
                    errorRecords.add(new ErrorRecord(ErrorType.VOID_RETURN_MISMATCH, linenum));
                }
            }
            if (funcDef.funcType.type != LexType.VOIDTK && !visitResult.hasReturnInLastSentence) {
                errorRecords.add(new ErrorRecord(ErrorType.MISSING_RETURN, funcDef.blockLinenum));
            }
        }
        curTable = curTable.parent;
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
        String ident = constdef.ident;
        if (curTable.contain(ident)) {
            errorRecords.add(new ErrorRecord(ErrorType.NAME_REDEFINED, constdef.linenum));
            if (constdef.constExp != null) {
                visitConstExp(constdef.constExp);
            }
            if (constdef.constInitVal != null) {
                visitConstInitVal(constdef.constInitVal);
            }
        } else {
            VarType varType = new VarType();
            varType.type = type;
            varType.isArray = (constdef.constExp != null);
            VarSym varSym = new VarSym(ident, varType);
            varSym.isConst = true;
            if (constdef.constExp != null) {
                visitConstExp(constdef.constExp);
            }
            if (constdef.constInitVal != null) {
                visitConstInitVal(constdef.constInitVal);
            }
            curTable.add(varSym);
        }
    }

    public void visitConstExp(ConstExp constExp) {
        visitAddExp(constExp.addExp);
    }

    public void visitVarDecl(VarDecl varDecl) {
        LexType type = varDecl.bType.type;
        for (VarDef varDef : varDecl.varDefs) {
            visitVarDef(varDef, type);
        }
    }

    public void visitVarDef(VarDef varDef, LexType type) {
        assert type == LexType.INTTK || type == LexType.CHARTK;
        String ident = varDef.ident;
        if (curTable.contain(ident)) {
            errorRecords.add(new ErrorRecord(ErrorType.NAME_REDEFINED, varDef.linenum));
            if (varDef.constExp != null) {
                visitConstExp(varDef.constExp);
            }
            if (varDef.initVal != null) {
                visitInitVal(varDef.initVal);
            }
        } else {
            VarType varType = new VarType();
            varType.type = type;
            varType.isArray = (varDef.constExp != null);
            VarSym varSym = new VarSym(ident, varType);
            if (varDef.constExp != null) {
                visitConstExp(varDef.constExp);
            }
            if (varDef.initVal != null) {
                visitInitVal(varDef.initVal);
            }
            curTable.add(varSym);
        }
    }

    public VisitResult visitConstInitVal(ConstInitVal constInitVal) {
        VisitResult visitResult = new VisitResult();
        if (constInitVal.constExps != null) {
            for (ConstExp constExp : constInitVal.constExps) {
                visitConstExp(constExp);
            }
        }
        return visitResult;
    }

    public VisitResult visitInitVal(InitVal initVal) {
        VisitResult visitResult = new VisitResult();
        if (initVal.exps != null) {
            for (Exp exp : initVal.exps) {
                visitExp(exp);
            }
        }
        return visitResult;
    }

    public VisitResult visitBlock(Block block) {
        VisitResult visitResult = new VisitResult();
        for (BlockItem blockItem : block.blockItems) {
            VisitResult tmp = visitBlockItem(blockItem);
            visitResult.returnNotVoidLineNumber.addAll(tmp.returnNotVoidLineNumber);
        }
        if (!block.blockItems.isEmpty() &&
                block.blockItems.get(block.blockItems.size() - 1).stmt != null &&
                block.blockItems.get(block.blockItems.size() - 1).stmt instanceof Return_stmt) {
            visitResult.hasReturnInLastSentence = true;
        }
        return visitResult;
    }

    public VisitResult visitBlockItem(BlockItem blockItem) {
        if (blockItem.decl != null) {
            visitDecl(blockItem.decl);
            return new VisitResult();
        } else {
            return visitStmt(blockItem.stmt);
        }
    }

    public VisitResult visitStmt(Stmt stmt) {
        if (stmt instanceof AssignLval_stmt) {
            return visitAssignLval_stmt((AssignLval_stmt) stmt);
        } else if (stmt instanceof Block_stmt) {
            curTable = curTable.createChild();
            globalTable.add(curTable);
            VisitResult visitResult = visitBlock(((Block_stmt) stmt).block);
            curTable = curTable.parent;
            return visitResult;
        } else if (stmt instanceof Break_Continue_stmt) {
            if (loopLevel == 0) {
                errorRecords.add(new ErrorRecord(ErrorType.BREAK_CONTINUE_IN_NON_LOOP, ((Break_Continue_stmt) stmt).linenum));
            }
        } else if (stmt instanceof Exp_stmt) {
            if (((Exp_stmt) stmt).exp != null) {
                return visitExp(((Exp_stmt) stmt).exp);
            }
        } else if (stmt instanceof For_stmt) {
            if (((For_stmt) stmt).fotStmt1 != null) {
                visitForStmt(((For_stmt) stmt).fotStmt1);
            }
            if (((For_stmt) stmt).cond != null) {
                visitCond(((For_stmt) stmt).cond);
            }
            if (((For_stmt) stmt).forStmt2 != null) {
                visitForStmt(((For_stmt) stmt).forStmt2);
            }
            loopLevel += 1;
            VisitResult visitResult = visitStmt(((For_stmt) stmt).stmt);
            loopLevel -= 1;
            return visitResult;
        } else if (stmt instanceof GetChar_stmt || stmt instanceof GetInt_stmt) {
            LVal tmp = stmt instanceof GetChar_stmt ? ((GetChar_stmt) stmt).lval : ((GetInt_stmt) stmt).lval;
            visitLVal(tmp);
            String ident = tmp.ident;
            VarSym varSym = (VarSym) curTable.get(ident);
            if (varSym != null && varSym.isConst) {
                errorRecords.add(new ErrorRecord(ErrorType.MODIFY_CONSTANT, tmp.linenum));
            }
        } else if (stmt instanceof If_stmt) {
            visitCond(((If_stmt) stmt).cond);
            VisitResult tmp = new VisitResult();
            VisitResult ifStmt = visitStmt(((If_stmt) stmt).stmt);
            tmp.returnNotVoidLineNumber.addAll(ifStmt.returnNotVoidLineNumber);
            if (((If_stmt) stmt).elseStmt != null) {
                VisitResult elseStmt = visitStmt(((If_stmt) stmt).elseStmt);
                tmp.returnNotVoidLineNumber.addAll(elseStmt.returnNotVoidLineNumber);
            }
            return tmp;
        } else if (stmt instanceof Print_stmt) {
            if (((Print_stmt) stmt).exps != null) {
                for (Exp exp : ((Print_stmt) stmt).exps) {
                    visitExp(exp);
                }
            }
            int len = ((Print_stmt) stmt).exps == null ? 0 : ((Print_stmt) stmt).exps.size();
            int realLen = ((Print_stmt) stmt).stringConst.split("%d|%c").length - 1;
            if (len != realLen) {
                errorRecords.add(new ErrorRecord(ErrorType.PRINTF_MISMATCH, ((Print_stmt) stmt).linenum));
            }
        } else if (stmt instanceof Return_stmt) {
            VisitResult visitResult = new VisitResult();
            if (((Return_stmt) stmt).exp != null) {
                VisitResult tmp = visitExp(((Return_stmt) stmt).exp);
                visitResult.returnNotVoidLineNumber.add(((Return_stmt) stmt).linenum);
            }
            return visitResult;
        } else {
            throw new RuntimeException("Unknown Stmt type");
        }
        return new VisitResult();
    }

    public void visitForStmt(ForStmt forStmt) {
        visitLVal(forStmt.lval);
        String ident = forStmt.lval.ident;
        VarSym varSym = (VarSym) curTable.get(ident);
        if (varSym != null && varSym.isConst) {
            errorRecords.add(new ErrorRecord(ErrorType.MODIFY_CONSTANT, forStmt.linenum));
        }
        visitExp(forStmt.exp);
    }

    public void visitCond(Cond cond) {
        if (cond.lOrExp != null) {
            visitLOrExp(cond.lOrExp);
        }
    }

    public VisitResult visitAssignLval_stmt(AssignLval_stmt assignLval_stmt) {
        VisitResult visitResult = new VisitResult();
        String ident = assignLval_stmt.lval.ident;
        VarSym varSym = (VarSym) curTable.get(ident);
        if (varSym != null && varSym.isConst) {
            errorRecords.add(new ErrorRecord(ErrorType.MODIFY_CONSTANT, assignLval_stmt.linenum));
        }
        VisitResult tmp = visitLVal(assignLval_stmt.lval);
        VisitResult tmp2 = visitExp(assignLval_stmt.exp);
        return visitResult;
    }

    public void visitLOrExp(LOrExp lOrExp) {
        if (lOrExp.lOrExp != null) {
            visitLOrExp(lOrExp.lOrExp);
        }
        visitLAndExp(lOrExp.lAndExp);
    }

    public void visitLAndExp(LAndExp lAndExp) {
        if (lAndExp.lAndExp != null) {
            visitLAndExp(lAndExp.lAndExp);
        }
        visitEqExp(lAndExp.eqExp);
    }

    public void visitEqExp(EqExp eqExp) {
        if (eqExp.eqExp != null) {
            visitEqExp(eqExp.eqExp);
        }
        visitRelExp(eqExp.relExp);
    }

    public void visitRelExp(RelExp relExp) {
        if (relExp.relExp != null) {
            visitRelExp(relExp.relExp);
        }
        visitAddExp(relExp.addExp);
    }

    public VisitResult visitAddExp(AddExp addExp) {
        VisitResult visitResult = new VisitResult();
        if (addExp.addExp != null) {
            VisitResult tmp = visitAddExp(addExp.addExp);
        }
        VisitResult tmp = visitMulExp(addExp.mulExp);
        visitResult.varType = tmp.varType;
        return visitResult;
    }

    public VisitResult visitMulExp(MulExp mulExp) {
        VisitResult visitResult = new VisitResult();
        if (mulExp.mulExp != null) {
            VisitResult tmp = visitMulExp(mulExp.mulExp);
        }
        VisitResult tmp = visitUnaryExp(mulExp.unaryExp);
        visitResult.varType = tmp.varType;
        return visitResult;
    }

    public VisitResult visitUnaryExp(UnaryExp unaryExp) {
        if (unaryExp.unaryExp != null) {
            return visitUnaryExp(unaryExp.unaryExp);
        } else if (unaryExp.primaryExp != null) {
            return visitPrimaryExp(unaryExp.primaryExp);
        } else {
            Symbol funcSym = curTable.get(unaryExp.ident);
            if (funcSym == null) {
                errorRecords.add(new ErrorRecord(ErrorType.UNDEFINED_NAME, unaryExp.linenum));
                if (unaryExp.funcRParams != null) {
                    return visitFuncRParams(unaryExp.funcRParams);
                } else {
                    return new VisitResult();
                }
            } else {
                int paramNum = ((FuncSym) funcSym).paramTypeList.size();
                int realParamNum = unaryExp.funcRParams == null ? 0 : unaryExp.funcRParams.exps.size();
                if (paramNum != realParamNum) {
                    errorRecords.add(new ErrorRecord(ErrorType.PARAMETER_COUNT_MISMATCH, unaryExp.linenum));
                    return visitFuncRParams(unaryExp.funcRParams);
                }
                // 传递数组给变量。传递变量给数组。传递 char 型数组给 int 型数组。传递 int 型数组给 char 型数组。
                if (unaryExp.funcRParams != null) {
                    VisitResult tmp = visitFuncRParams(unaryExp.funcRParams);
                    boolean hasError = isHasError(tmp, (FuncSym) funcSym, paramNum);
                    if (hasError) {
                        errorRecords.add(new ErrorRecord(ErrorType.PARAMETER_TYPE_MISMATCH, unaryExp.linenum));
                    }
                }
                VisitResult visitResult = new VisitResult();
                visitResult.varType = new VarType();
                visitResult.varType.type = ((FuncSym) funcSym).retType;
                visitResult.varType.isArray = false;
                return visitResult;
            }
        }
    }

    public VisitResult visitPrimaryExp(PrimaryExp primaryExp) {
        VisitResult visitResult = new VisitResult();
        if (primaryExp.exp != null) {
            VisitResult tmp = visitExp(primaryExp.exp);
            visitResult.varType = tmp.varType;
        } else if (primaryExp.lval != null) {
            VisitResult tmp = visitLVal(primaryExp.lval);
            visitResult.varType = tmp.varType;
        } else if (primaryExp.number != null) {
            visitResult.varType = new VarType();
            visitResult.varType.type = LexType.INTTK;
        } else {
            visitResult.varType = new VarType();
            visitResult.varType.type = LexType.CHARTK;
        }
        return visitResult;
    }

    public VisitResult visitLVal(LVal lval) {
        VisitResult visitResult = new VisitResult();
        Symbol varSym = curTable.get(lval.ident);
        if (varSym == null) {
            errorRecords.add(new ErrorRecord(ErrorType.UNDEFINED_NAME, lval.linenum));
        } else {
            visitResult.varType = new VarType();
            visitResult.varType.type = ((VarSym) varSym).varType.type; // int or char
            if (lval.exp != null) {
                visitResult.varType.isArray = false;
                VisitResult tmp = visitExp(lval.exp);
            } else {
                visitResult.varType.isArray = ((VarSym) varSym).varType.isArray;
            }
        }
        return visitResult;
    }

    public VisitResult visitFuncRParams(FuncRParams funcRParams) {
        VisitResult visitResult = new VisitResult();
        for (Exp exp : funcRParams.exps) {
            VisitResult tmp = visitExp(exp);
            visitResult.paraTypeList.add(tmp.varType);
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
            visitResult.paraTypeList = new ArrayList<>();
        }
        return visitResult;
    }

    public VisitResult visitFuncFParam(FuncFParam funcFParam) {
        String ident = funcFParam.ident;
        if (curTable.contain(ident)) {
            errorRecords.add(new ErrorRecord(ErrorType.NAME_REDEFINED, funcFParam.linenum));
            return null;
        }
        VarSym varSym = new VarSym(ident, new VarType());
        LexType type = funcFParam.bType.type;
        boolean isArray = funcFParam.isArray;
        varSym.varType.type = type;
        varSym.varType.isArray = isArray;
        // 如果参数重定义,则认为函数无效,不插入符号表?
        curTable.add(varSym);
        VisitResult visitResult = new VisitResult();
        visitResult.varType = varSym.varType;
        return visitResult;
    }

    public VisitResult visitExp(Exp exp) {
        return visitAddExp(exp.addExp);
    }
}
