package frontend.parser;

import frontend.error.ErrorRecord;
import frontend.error.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Lexer;
import frontend.lexer.Token;
import frontend.parser.components.Block;
import frontend.parser.components.BlockItem;
import frontend.parser.components.CompUnit;
import frontend.parser.components.Decl.ConstDecl;
import frontend.parser.components.Decl.ConstDef;
import frontend.parser.components.Decl.Decl;
import frontend.parser.components.Decl.VarDecl;
import frontend.parser.components.Decl.VarDef;
import frontend.parser.components.Exp.AddExp;
import frontend.parser.components.Exp.BType;
import frontend.parser.components.Exp.Character_com;
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
import frontend.parser.components.Exp.Number_com;
import frontend.parser.components.Exp.PrimaryExp;
import frontend.parser.components.Exp.RelExp;
import frontend.parser.components.Exp.UnaryExp;
import frontend.parser.components.Exp.UnaryOp;
import frontend.parser.components.Func.FuncDef;
import frontend.parser.components.Func.FuncFParam;
import frontend.parser.components.Func.FuncFParams;
import frontend.parser.components.Func.FuncType;
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

import java.io.IOException;
import java.util.ArrayList;

// 进入解析函数前，curToken应符合规则的第一个符号，可以通过判断FIRST集的方式来确保。
// 离开解析函数后，curToken应已经推进，指向下一个将要解析的符号，而不是已经处理完毕的部分。

public class Parser {
    ArrayList<ErrorRecord> errorRecords;
    TokenBuf tokenBuf;
    Token curToken;
    Token nextToken;
    Token nextnextToken;

    public Parser(Lexer lexer, ArrayList<ErrorRecord> errorRecords) throws IOException {
        this.errorRecords = errorRecords;
        this.tokenBuf = new TokenBuf(lexer);
    }

    public CompUnit parse() {
        CompUnit compUnit = new CompUnit();
        // {Decl}
        curToken = tokenBuf.get();
        nextToken = tokenBuf.read(1);
        nextnextToken = tokenBuf.read(2);
        while (isDeclToken(curToken, nextToken, nextnextToken)) {
            Decl result = parseDecl();
            compUnit.decls.add(result);
            nextToken = tokenBuf.read(1);
            nextnextToken = tokenBuf.read(2);
        }
        // {FuncDef}
        while (curToken.isFuncType() && nextToken.isNotMatch(LexType.MAINTK)) {
            FuncDef funcDef = parseFuncDef();
            compUnit.funcDefs.add(funcDef);
            nextToken = tokenBuf.read(1);
            nextnextToken = tokenBuf.read(2);
        }
        // {MainFunc}
        MainFuncDef mainFuncDef = parseMainFuncDef();
        assert curToken == null;
        compUnit.mainFuncDef = mainFuncDef;
        return compUnit;
    }

    private MainFuncDef parseMainFuncDef() {
        MainFuncDef mainFuncDef = new MainFuncDef();
        assert curToken.isMatch(LexType.INTTK);
        curToken = tokenBuf.get();
        assert curToken.isMatch(LexType.MAINTK);
        curToken = tokenBuf.get();
        assert curToken.isMatch(LexType.LPARENT);
        curToken = tokenBuf.get();
        if (curToken.isMatch(LexType.RPARENT)) {
            mainFuncDef.hasRightParen = true;
            curToken = tokenBuf.get();
        } else {
            mainFuncDef.hasRightParen = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_PARENTHESIS, tokenBuf.getPrePreLineNum()));
        }
        mainFuncDef.block = parseBlock();
        mainFuncDef.linenum = tokenBuf.getPreLineNum();
        return mainFuncDef;
    }

    private Block parseBlock() {
        Block block = new Block();
        assert curToken.isMatch(LexType.LBRACE);
        curToken = tokenBuf.get();
        while (curToken.isNotMatch(LexType.RBRACE)) {
            block.blockItems.add(parseBlockItem());
        }
        curToken = tokenBuf.get();
        return block;
    }

    private BlockItem parseBlockItem() {
        BlockItem blockItem = new BlockItem();
        nextToken = tokenBuf.read(1);
        if (curToken.isMatch(LexType.CONSTTK) || (curToken.isBtype() && nextToken.isMatch(LexType.IDENFR))) {
            blockItem.decl = parseDecl();
        } else {
            blockItem.stmt = parseStmt();
        }
        return blockItem;
    }

    private Stmt parseStmt() {
        nextToken = tokenBuf.read(1);
        nextnextToken = tokenBuf.read(2);
        if (matchExpFirstWithoutIdenFR(curToken) || (curToken.isMatch(LexType.IDENFR) && nextToken.isMatch(LexType.LPARENT))) {
            // 还剩LVal → Ident ['[' Exp ']']
            Exp_stmt exp_stmt = new Exp_stmt();
            exp_stmt.exp = parseExp();
            if (curToken.isMatch(LexType.SEMICN)) {
                exp_stmt.hasSemicolon = true;
                curToken = tokenBuf.get();
            } else {
                exp_stmt.hasSemicolon = false;
                errorRecords.add(new ErrorRecord(ErrorType.MISSING_SEMICOLON, tokenBuf.getPrePreLineNum()));
            }
            return exp_stmt;
        } else if (curToken.isMatch(LexType.IDENFR)) { // LVal '=' 3种 和 Lval
            LVal lVal = parseLVal();
            if (curToken.isMatch(LexType.ASSIGN)) {
                curToken = tokenBuf.get();
                if (curToken.isMatch(LexType.GETINTTK)) {
                    return parseGetIntStmt(lVal);
                } else if (curToken.isMatch(LexType.GETCHARTK)) {
                    return parseGetCharStmt(lVal);
                } else {
                    return parseAssignLvalStmt(lVal);
                }
            } else {
                Exp_stmt exp_stmt = new Exp_stmt();
                exp_stmt.exp = parseExp(lVal);
                if (curToken.isMatch(LexType.SEMICN)) {
                    exp_stmt.hasSemicolon = true;
                    curToken = tokenBuf.get();
                } else {
                    exp_stmt.hasSemicolon = false;
                    errorRecords.add(new ErrorRecord(ErrorType.MISSING_SEMICOLON, tokenBuf.getPrePreLineNum()));
                }
                return exp_stmt;
            }
        } else if (curToken.isMatch(LexType.SEMICN)) {
            Exp_stmt exp_stmt = new Exp_stmt();
            exp_stmt.hasSemicolon = true;
            curToken = tokenBuf.get();
            return exp_stmt;
        } else if (curToken.isMatch(LexType.LBRACE)) {
            Block_stmt block_stmt = new Block_stmt();
            block_stmt.block = parseBlock();
            return block_stmt;
        } else if (curToken.isMatch(LexType.IFTK)) {
            return parseIfStmt();
        } else if (curToken.isMatch(LexType.FORTK)) {
            return parseFor_Stmt();
        } else if (curToken.isMatch(LexType.BREAKTK) || curToken.isMatch(LexType.CONTINUETK)) {
            Break_Continue_stmt break_continue_stmt = new Break_Continue_stmt();
            break_continue_stmt.linenum = curToken.getLineNum();
            break_continue_stmt.type = curToken.getType();
            curToken = tokenBuf.get();
            if (curToken.isMatch(LexType.SEMICN)) {
                break_continue_stmt.hasSemicolon = true;
                curToken = tokenBuf.get();
            } else {
                break_continue_stmt.hasSemicolon = false;
                errorRecords.add(new ErrorRecord(ErrorType.MISSING_SEMICOLON, tokenBuf.getPrePreLineNum()));
            }
            return break_continue_stmt;
        } else if (curToken.isMatch(LexType.RETURNTK)) {
            return parseReturnStmt();
        } else if (curToken.isMatch(LexType.PRINTFTK)) {
            return parsePrintfStmt();
        }
        System.out.println("linenum" + curToken.getLineNum() + " " + curToken.getValue());
        for (ErrorRecord errorRecord : errorRecords) {
            System.out.println(errorRecord);
        }
        throw new RuntimeException("Stmt parse error");
    }

    private Stmt parsePrintfStmt() {
        // 'printf''('StringConst {','Exp }    ')'';'
        Print_stmt print_stmt = new Print_stmt();
        print_stmt.linenum = curToken.getLineNum();
        curToken = tokenBuf.get();
        assert curToken.isMatch(LexType.LPARENT);
        curToken = tokenBuf.get();
        print_stmt.stringConst = curToken.getValue();
        curToken = tokenBuf.get();
        while (curToken.isMatch(LexType.COMMA)) {
            curToken = tokenBuf.get();
            print_stmt.exps.add(parseExp());
        }
        if (curToken.isMatch(LexType.RPARENT)) {
            curToken = tokenBuf.get();
            print_stmt.hasRightParen = true;
        } else {
            print_stmt.hasRightParen = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_PARENTHESIS, tokenBuf.getPrePreLineNum()));
        }
        if (curToken.isMatch(LexType.SEMICN)) {
            print_stmt.hasSemicolon = true;
            curToken = tokenBuf.get();
        } else {
            print_stmt.hasSemicolon = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_SEMICOLON, tokenBuf.getPrePreLineNum()));
        }
        return print_stmt;
    }

    private Stmt parseReturnStmt() {
        // ReturnStmt → 'return' [Exp] ';'
        Return_stmt return_stmt = new Return_stmt();
        assert curToken.isMatch(LexType.RETURNTK);
        return_stmt.linenum = curToken.getLineNum();
        curToken = tokenBuf.get();
        if (matchExpFirst(curToken)) {
            return_stmt.exp = parseExp();
        }
        if (curToken.isMatch(LexType.SEMICN)) {
            return_stmt.hasSemicolon = true;
            curToken = tokenBuf.get();
        } else {
            return_stmt.hasSemicolon = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_SEMICOLON, tokenBuf.getPrePreLineNum()));
        }
        return return_stmt;
    }

    private Stmt parseFor_Stmt() {
        //  'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // h
        For_stmt for_stmt = new For_stmt();
        curToken = tokenBuf.get();
        assert curToken.isMatch(LexType.LPARENT);
        curToken = tokenBuf.get();
        if (curToken.isMatch(LexType.IDENFR)) {
            for_stmt.fotStmt1 = parseForStmt();
        }
        assert curToken.isMatch(LexType.SEMICN);
        curToken = tokenBuf.get();
        if (matchCondFirst(curToken)) {
            for_stmt.cond = parseCond();
        }
        assert curToken.isMatch(LexType.SEMICN);
        curToken = tokenBuf.get();
        if (curToken.isMatch(LexType.IDENFR)) {
            for_stmt.forStmt2 = parseForStmt();
        }
        assert curToken.isMatch(LexType.RPARENT);
        curToken = tokenBuf.get();
        for_stmt.stmt = parseStmt();
        return for_stmt;
    }

    private Stmt parseIfStmt() {
        // IfStmt → 'if' '(' Cond ')' Stmt ['Else' Stmt]
        If_stmt if_stmt = new If_stmt();
        curToken = tokenBuf.get();
        assert curToken.isMatch(LexType.LPARENT);
        curToken = tokenBuf.get();
        if_stmt.cond = parseCond();
        if (curToken.isMatch(LexType.RPARENT)) {
            if_stmt.hasRightParent = true;
            curToken = tokenBuf.get();
        } else {
            if_stmt.hasRightParent = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_PARENTHESIS, tokenBuf.getPrePreLineNum()));
        }
        if_stmt.stmt = parseStmt();
        if (curToken.isMatch(LexType.ELSETK)) {
            curToken = tokenBuf.get();
            if_stmt.elseStmt = parseStmt();
        }
        return if_stmt;
    }

    private Stmt parseAssignLvalStmt(LVal lVal) {
        AssignLval_stmt assignLval_stmt = new AssignLval_stmt();
        assignLval_stmt.linenum = lVal.linenum;
        assignLval_stmt.lval = lVal;
        assignLval_stmt.exp = parseExp();
        if (curToken.isMatch(LexType.SEMICN)) {
            assignLval_stmt.hasSemicolon = true;
            curToken = tokenBuf.get();
        } else {
            assignLval_stmt.hasSemicolon = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_SEMICOLON, tokenBuf.getPrePreLineNum()));
        }
        return assignLval_stmt;
    }

    private Stmt parseGetCharStmt(LVal lVal) {
        GetChar_stmt getChar_stmt = new GetChar_stmt();
        getChar_stmt.lval = lVal;
        getChar_stmt.linenum = lVal.linenum;
        assert curToken.isMatch(LexType.GETCHARTK);
        curToken = tokenBuf.get();
        assert curToken.isMatch(LexType.LPARENT);
        curToken = tokenBuf.get();
        if (curToken.isMatch(LexType.RPARENT)) {
            getChar_stmt.hasRightParent = true;
            curToken = tokenBuf.get();
        } else {
            getChar_stmt.hasRightParent = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_PARENTHESIS, tokenBuf.getPrePreLineNum()));
        }
        if (curToken.isMatch(LexType.SEMICN)) {
            getChar_stmt.hasSemicolon = true;
            curToken = tokenBuf.get();
        } else {
            getChar_stmt.hasSemicolon = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_SEMICOLON, tokenBuf.getPrePreLineNum()));
        }
        return getChar_stmt;
    }

    private Stmt parseGetIntStmt(LVal lVal) {
        GetInt_stmt getInt_stmt = new GetInt_stmt();
        getInt_stmt.lval = lVal;
        getInt_stmt.linenum = lVal.linenum;
        assert curToken.isMatch(LexType.GETINTTK);
        curToken = tokenBuf.get();
        assert curToken.isMatch(LexType.LPARENT);
        curToken = tokenBuf.get();
        if (curToken.isMatch(LexType.RPARENT)) {
            getInt_stmt.hasRightParent = true;
            curToken = tokenBuf.get();
        } else {
            getInt_stmt.hasRightParent = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_PARENTHESIS, tokenBuf.getPrePreLineNum()));
        }
        if (curToken.isMatch(LexType.SEMICN)) {
            getInt_stmt.hasSemicolon = true;
            curToken = tokenBuf.get();
        } else {
            getInt_stmt.hasSemicolon = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_SEMICOLON, tokenBuf.getPrePreLineNum()));
        }
        return getInt_stmt;
    }

    private ForStmt parseForStmt() {
        ForStmt forStmt = new ForStmt();
        forStmt.linenum = curToken.getLineNum();
        forStmt.lval = parseLVal();
        assert curToken.isMatch(LexType.ASSIGN);
        curToken = tokenBuf.get();
        forStmt.exp = parseExp();
        return forStmt;
    }

    private Cond parseCond() {
        Cond cond = new Cond();
        cond.lOrExp = parseLOrExp();
        return cond;
    }

    private LOrExp parseLOrExp() {
        LOrExp lOrExp = new LOrExp();
        lOrExp.lAndExp = parseLAndExp();
        while (curToken.isMatch(LexType.OR)) {
            LOrExp newLOrExp = new LOrExp();
            newLOrExp.op = curToken.getValue();
            newLOrExp.lOrExp = lOrExp;
            curToken = tokenBuf.get();
            newLOrExp.lAndExp = parseLAndExp();
            lOrExp = newLOrExp;
        }
        return lOrExp;
    }

    private LAndExp parseLAndExp() {
        LAndExp lAndExp = new LAndExp();
        lAndExp.eqExp = parseEqExp();
        while (curToken.isMatch(LexType.AND)) {
            LAndExp newLAndExp = new LAndExp();
            newLAndExp.op = curToken.getValue();
            newLAndExp.lAndExp = lAndExp;
            curToken = tokenBuf.get();
            newLAndExp.eqExp = parseEqExp();
            lAndExp = newLAndExp;
        }
        return lAndExp;
    }

    private EqExp parseEqExp() {
        EqExp eqExp = new EqExp();
        eqExp.relExp = parseRelExp();
        while (curToken.isMatch(LexType.EQL) || curToken.isMatch(LexType.NEQ)) {
            EqExp newEqExp = new EqExp();
            newEqExp.type = curToken.getType();
            newEqExp.eqExp = eqExp;
            curToken = tokenBuf.get();
            newEqExp.relExp = parseRelExp();
            eqExp = newEqExp;
        }
        return eqExp;
    }

    private RelExp parseRelExp() {
        // RelExp → AddExp [RelOp AddExp] // b g j
        RelExp relExp = new RelExp();
        relExp.addExp = parseAddExp();
        if (curToken.isMatch(LexType.LSS) || curToken.isMatch(LexType.LEQ) ||
                curToken.isMatch(LexType.GRE) || curToken.isMatch(LexType.GEQ)) {
            RelExp newRelExp = new RelExp();
            newRelExp.type = curToken.getType();
            newRelExp.relExp = relExp;
            curToken = tokenBuf.get();
            newRelExp.addExp = parseAddExp();
            relExp = newRelExp;
        }
        return relExp;
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b g j
    private FuncDef parseFuncDef() {
        FuncDef funcDef = new FuncDef();
        funcDef.funcType = parseFuncType();
        funcDef.identLinenum = curToken.getLineNum();
        funcDef.ident = curToken.getValue();
        curToken = tokenBuf.get();
        assert curToken.isMatch(LexType.LPARENT);
        curToken = tokenBuf.get();
        if (curToken.isBtype()) {
            funcDef.funcFParams = parseFuncFParams();
        }
        if (curToken.isMatch(LexType.RPARENT)) {
            funcDef.hasRightParen = true;
            curToken = tokenBuf.get();
        } else {
            funcDef.hasRightParen = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_PARENTHESIS, tokenBuf.getPrePreLineNum()));
        }
        funcDef.block = parseBlock();
        funcDef.blockLinenum = tokenBuf.getPrePreLineNum();
        return funcDef;
    }

    private FuncType parseFuncType() {
        FuncType funcType = new FuncType();
        funcType.type = curToken.getType();
        curToken = tokenBuf.get();
        return funcType;
    }

    private FuncFParams parseFuncFParams() {
        FuncFParams funcFParams = new FuncFParams();
        funcFParams.FParams.add(parseFuncFParam());
        while (curToken.isMatch(LexType.COMMA)) {
            curToken = tokenBuf.get();
            funcFParams.FParams.add(parseFuncFParam());
        }
        return funcFParams;
    }

    //  FuncFParam → BType Ident ['[' ']']
    private FuncFParam parseFuncFParam() {
        FuncFParam funcFParam = new FuncFParam();
        funcFParam.bType = parseBType();
        funcFParam.linenum = curToken.getLineNum();
        funcFParam.ident = curToken.getValue();
        curToken = tokenBuf.get();
        if (curToken.isMatch(LexType.LBRACK)) {
            funcFParam.isArray = true;
            curToken = tokenBuf.get();
            if (curToken.isMatch(LexType.RBRACK)) {
                funcFParam.hasRightBracket = true;
                curToken = tokenBuf.get();
            } else {
                funcFParam.hasRightBracket = false;
                errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_BRACKET, tokenBuf.getPrePreLineNum()));
            }
        }
        return funcFParam;
    }

    private Decl parseDecl() {
        Decl decl = new Decl();
        if (curToken.isMatch(LexType.CONSTTK)) {
            decl.constDecl = parseConstDecl();
        } else {
            decl.varDecl = parseVarDecl();
        }
        return decl;
    }

    private VarDecl parseVarDecl() {
        VarDecl varDecl = new VarDecl();
        varDecl.bType = parseBType();
        varDecl.varDefs.add(parseVarDef());
        while (curToken.isMatch(LexType.COMMA)) {
            curToken = tokenBuf.get();
            varDecl.varDefs.add(parseVarDef());
        }
        if (curToken.isMatch(LexType.SEMICN)) {
            varDecl.hasSemicolon = true;
            curToken = tokenBuf.get();
        } else {
            varDecl.hasSemicolon = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_SEMICOLON, tokenBuf.getPrePreLineNum()));
        }
        return varDecl;
    }

    private VarDef parseVarDef() {
        VarDef varDef = new VarDef();
        varDef.linenum = curToken.getLineNum();
        varDef.ident = curToken.getValue();
        curToken = tokenBuf.get();
        if (curToken.isMatch(LexType.LBRACK)) {
            curToken = tokenBuf.get();
            varDef.constExp = parseConstExp();
            if (curToken.isMatch(LexType.RBRACK)) {
                varDef.hasRBrack = true;
                curToken = tokenBuf.get();
            } else {
                varDef.hasRBrack = false;
                errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_BRACKET, tokenBuf.getPrePreLineNum()));
            }
        }
        if (curToken.isMatch(LexType.ASSIGN)) {
            curToken = tokenBuf.get();
            varDef.initVal = parseInitVal();
        }
        return varDef;
    }

    private InitVal parseInitVal() {
        InitVal initVal = new InitVal();
        if (curToken.isMatch(LexType.LBRACE)) {
            initVal.hasBrace = true;
            nextToken = tokenBuf.read(1);
            if (nextToken.isMatch(LexType.RBRACE)) {
                curToken = tokenBuf.get();
                curToken = tokenBuf.get();
                return initVal;
            }
            do {
                curToken = tokenBuf.get();
                initVal.exps.add(parseExp());
            } while (curToken.isMatch(LexType.COMMA));
            assert curToken.isMatch(LexType.RBRACE);
            curToken = tokenBuf.get();
        } else if (matchExpFirst(curToken)) {
            initVal.hasBrace = false;
            initVal.exps.add(parseExp());
        } else {
            initVal.stringConst = curToken.getValue();
            curToken = tokenBuf.get();
        }
        return initVal;
    }

    private ConstDecl parseConstDecl() {
        ConstDecl constDecl = new ConstDecl();
        assert curToken.isMatch(LexType.CONSTTK);
        curToken = tokenBuf.get();
        constDecl.type = parseBType();
        constDecl.constDefs.add(parseConstDef());
        while (curToken.isMatch(LexType.COMMA)) {
            curToken = tokenBuf.get();
            constDecl.constDefs.add(parseConstDef());
        }
        if (curToken.isMatch(LexType.SEMICN)) {
            constDecl.hasSemicolon = true;
            curToken = tokenBuf.get();
        } else {
            constDecl.hasSemicolon = false;
            errorRecords.add(new ErrorRecord(ErrorType.MISSING_SEMICOLON, tokenBuf.getPrePreLineNum()));
        }
        return constDecl;
    }

    private BType parseBType() {
        BType bType = new BType();
        if (curToken.isMatch(LexType.INTTK)) {
            bType.type = LexType.INTTK;
        } else {
            bType.type = LexType.CHARTK;
        }
        curToken = tokenBuf.get();
        return bType;
    }

    private ConstDef parseConstDef() {
        ConstDef constDef = new ConstDef();
        constDef.linenum = curToken.getLineNum();
        constDef.ident = curToken.getValue();
        curToken = tokenBuf.get();
        if (curToken.isMatch(LexType.LBRACK)) {
            curToken = tokenBuf.get();
            constDef.constExp = parseConstExp();
            if (curToken.isMatch(LexType.RBRACK)) {
                constDef.hasRightBrack = true;
                curToken = tokenBuf.get();
            } else {
                constDef.hasRightBrack = false;
                errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_BRACKET, tokenBuf.getPrePreLineNum()));
            }
        }
        assert curToken.isMatch(LexType.ASSIGN);
        curToken = tokenBuf.get();
        constDef.constInitVal = parseConstInitVal();
        return constDef;
    }

    private ConstExp parseConstExp() {
        ConstExp constExp = new ConstExp();
        constExp.addExp = parseAddExp();
        return constExp;
    }

    private ConstInitVal parseConstInitVal() {
        ConstInitVal constInitVal = new ConstInitVal();
        if (curToken.isMatch(LexType.STRCON)) {
            constInitVal.stringConst = curToken.getValue();
            curToken = tokenBuf.get();
        } else if (curToken.isMatch(LexType.LBRACE)) {
            constInitVal.hasBrace = true;
            nextToken = tokenBuf.read(1);
            if (nextToken.isMatch(LexType.RBRACE)) {
                curToken = tokenBuf.get();
                curToken = tokenBuf.get();
                return constInitVal;
            }
            do {
                curToken = tokenBuf.get();
                constInitVal.constExps.add(parseConstExp());
            } while (curToken.isMatch(LexType.COMMA));
            assert curToken.isMatch(LexType.RBRACE);
            curToken = tokenBuf.get();
        } else {
            constInitVal.hasBrace = false;
            constInitVal.constExps.add(parseConstExp());
        }
        return constInitVal;
    }

    private AddExp parseAddExp() {
        // 构造符合文法要求的语法树
        AddExp addExp = new AddExp();
        addExp.mulExp = parseMulExp();
        while (curToken.isMatch(LexType.PLUS) || curToken.isMatch(LexType.MINU)) {
            AddExp newAddExp = new AddExp();
            newAddExp.op = curToken.getType();
            newAddExp.addExp = addExp;
            curToken = tokenBuf.get();
            newAddExp.mulExp = parseMulExp();
            addExp = newAddExp;
        }
        return addExp;
    }

    private AddExp parseAddExp(LVal lVal) {
        AddExp addExp = new AddExp();
        addExp.mulExp = parseMulExp(lVal);
        while (curToken.isMatch(LexType.PLUS) || curToken.isMatch(LexType.MINU)) {
            AddExp newAddExp = new AddExp();
            newAddExp.op = curToken.getType();
            newAddExp.addExp = addExp;
            curToken = tokenBuf.get();
            newAddExp.mulExp = parseMulExp();
            addExp = newAddExp;
        }
        return addExp;
    }

    private MulExp parseMulExp() {
        MulExp mulExp = new MulExp();
        mulExp.unaryExp = parseUnaryExp();
        while (curToken.isMatch(LexType.MULT) || curToken.isMatch(LexType.DIV) || curToken.isMatch(LexType.MOD)) {
            MulExp newMulExp = new MulExp();
            newMulExp.op = curToken.getType();
            newMulExp.mulExp = mulExp;
            curToken = tokenBuf.get();
            newMulExp.unaryExp = parseUnaryExp();
            mulExp = newMulExp;
        }
        return mulExp;
    }

    private MulExp parseMulExp(LVal lVal) {
        MulExp mulExp = new MulExp();
        mulExp.unaryExp = parseUnaryExp(lVal);
        while (curToken.isMatch(LexType.MULT) || curToken.isMatch(LexType.DIV) || curToken.isMatch(LexType.MOD)) {
            MulExp newMulExp = new MulExp();
            newMulExp.op = curToken.getType();
            newMulExp.mulExp = mulExp;
            curToken = tokenBuf.get();
            newMulExp.unaryExp = parseUnaryExp();
            mulExp = newMulExp;
        }
        return mulExp;
    }

    // 一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    private UnaryExp parseUnaryExp() {
        UnaryExp unaryExp = new UnaryExp();
        nextToken = tokenBuf.read(1);
        if (curToken.isMatch(LexType.IDENFR) && nextToken.isMatch(LexType.LPARENT)) {
            unaryExp.linenum = curToken.getLineNum();
            unaryExp.ident = curToken.getValue();
            curToken = tokenBuf.get();
            assert curToken.isMatch(LexType.LPARENT);
            curToken = tokenBuf.get();
            if (matchExpFirst(curToken)) {
                unaryExp.funcRParams = parseFuncRParams();
            }
            if (curToken.isMatch(LexType.RPARENT)) {
                unaryExp.hasRightParent = true;
                curToken = tokenBuf.get();
            } else {
                unaryExp.hasRightParent = false;
                errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_PARENTHESIS, tokenBuf.getPrePreLineNum()));
            }
        } else if (curToken.isMatch(LexType.PLUS) || curToken.isMatch(LexType.MINU) || curToken.isMatch(LexType.NOT)) {
            unaryExp.op = parseUnaryOp();
            unaryExp.unaryExp = parseUnaryExp();
        } else {
            unaryExp.primaryExp = parsePrimaryExp();
        }
        return unaryExp;
    }

    private UnaryExp parseUnaryExp(LVal lVal) {
        UnaryExp unaryExp = new UnaryExp();
        unaryExp.primaryExp = parsePrimaryExp(lVal);
        return unaryExp;
    }

    //函数实参表 FuncRParams → Exp { ',' Exp }
    private FuncRParams parseFuncRParams() {
        FuncRParams funcRParams = new FuncRParams();
        funcRParams.exps.add(parseExp());
        while (curToken.isMatch(LexType.COMMA)) {
            curToken = tokenBuf.get();
            funcRParams.exps.add(parseExp());
        }
        return funcRParams;
    }

    private UnaryOp parseUnaryOp() {
        UnaryOp unaryOp = new UnaryOp();
        unaryOp.type = curToken.getType();
        curToken = tokenBuf.get();
        return unaryOp;
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number | Character// j
    private PrimaryExp parsePrimaryExp() {
        PrimaryExp primaryExp = new PrimaryExp();
        if (curToken.isMatch(LexType.LPARENT)) {
            curToken = tokenBuf.get();
            primaryExp.exp = parseExp();
            if (curToken.isMatch(LexType.RPARENT)) {
                primaryExp.hasRightParent = true;
                curToken = tokenBuf.get();
            } else {
                primaryExp.hasRightParent = false;
                errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_PARENTHESIS, tokenBuf.getPrePreLineNum()));
            }
        } else if (curToken.isMatch(LexType.IDENFR)) {
            primaryExp.lval = parseLVal();
        } else if (curToken.isMatch(LexType.INTCON)) {
            primaryExp.number = parseNumber();
        } else if (curToken.isMatch(LexType.CHRCON)) {
            primaryExp.character = parseCharacter();
        } else {
            System.out.println(curToken.getLineNum() + " " + curToken.getValue());
            throw new RuntimeException("PrimaryExp parse error");
        }
        return primaryExp;
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number | Character// j
    private PrimaryExp parsePrimaryExp(LVal lVal) {
        PrimaryExp primaryExp = new PrimaryExp();
        primaryExp.lval = lVal;
        return primaryExp;
    }

    private LVal parseLVal() {
        LVal lVal = new LVal();
        lVal.linenum = curToken.getLineNum();
        lVal.ident = curToken.getValue();
        curToken = tokenBuf.get();
        if (curToken.isMatch(LexType.LBRACK)) {
            curToken = tokenBuf.get();
            lVal.exp = parseExp();
            if (curToken.isMatch(LexType.RBRACK)) {
                lVal.hasRightBracket = true;
                curToken = tokenBuf.get();
            } else {
                lVal.hasRightBracket = false;
                errorRecords.add(new ErrorRecord(ErrorType.MISSING_RIGHT_BRACKET, tokenBuf.getPrePreLineNum()));
            }
        }
        return lVal;
    }

    private Number_com parseNumber() {
        Number_com number = new Number_com();
        number.intConst = curToken.getValue();
        curToken = tokenBuf.get();
        return number;
    }

    private Character_com parseCharacter() {
        Character_com character = new Character_com();
        character.charConst = curToken.getValue();
        curToken = tokenBuf.get();
        return character;
    }

    private Exp parseExp() {
        Exp exp = new Exp();
        exp.addExp = parseAddExp();
        return exp;
    }

    private Exp parseExp(LVal lVal) {
        Exp exp = new Exp();
        exp.addExp = parseAddExp(lVal);
        return exp;
    }

    /**
     * @param Token (,+,-,INTCON,CHARCON,IDENFR
     */
    private boolean matchExpFirst(Token Token) {
        return Token.isMatch(LexType.LPARENT) || Token.isMatch(LexType.IDENFR) ||
                Token.isMatch(LexType.INTCON) || Token.isMatch(LexType.CHRCON) ||
                Token.isMatch(LexType.PLUS) || Token.isMatch(LexType.MINU);
    }

    private boolean matchExpFirstWithoutIdenFR(Token Token) {
        return Token.isMatch(LexType.LPARENT) ||
                Token.isMatch(LexType.INTCON) || Token.isMatch(LexType.CHRCON) ||
                Token.isMatch(LexType.PLUS) || Token.isMatch(LexType.MINU);
    }


    private boolean matchCondFirst(Token token) {
        return token.isMatch(LexType.LPARENT) || token.isMatch(LexType.IDENFR) ||
                token.isMatch(LexType.INTCON) || token.isMatch(LexType.CHRCON) ||
                token.isMatch(LexType.PLUS) || token.isMatch(LexType.MINU) || token.isMatch(LexType.NOT);
    }

    private boolean isDeclToken(Token currentToken, Token nextToken, Token nextnextToken) {
        return currentToken.isMatch(LexType.CONSTTK) ||
                (currentToken.isBtype() && nextToken.isMatch(LexType.IDENFR) && nextnextToken.isNotMatch(LexType.LPARENT));
    }
}