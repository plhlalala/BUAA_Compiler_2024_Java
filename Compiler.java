import backend.Generator;
import frontend.error.ErrorRecord;
import frontend.lexer.Lexer;
import frontend.parser.Parser;
import frontend.parser.components.CompUnit;
import frontend.symtable.SymTable;
import frontend.symtable.Symbol;
import frontend.visitor_Symtable.Visitor_Symtable;
import middleend.LLVM_components.BasicBlock;
import middleend.LLVM_components.Function;
import middleend.LLVM_components.IrModule;
import middleend.Visitor_IR;
import middleend.instruction.BrInstr;
import middleend.instruction.ReturnInstr;
import optimize.OptimizationPipeline;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.Comparator;


public class Compiler {
    public static void main(String[] args) {
        String inputFilePath = "testfile.txt";  // 输入文件
        String lexerOutputPath = "parser.txt";   // 词法分析正确输出文件
        String errorOutputPath = "error.txt";   // 错误输出文件
        String symbolOutputPath = "symbol.txt";   // 符号表输出文件
        String llvmOutputPath = "llvm_ir.txt";   // 生成的LLVM IR文件
        String llvmPhiOutputPath = "llvm_phi.txt";   // 生成的LLVM IR文件 (phi版本）
        String mipsOutputPath = "mips.txt";   // 生成的MIPS汇编文件
        Boolean optimize = true;

        try (PushbackReader reader = new PushbackReader(new FileReader(inputFilePath));
             PrintWriter outputWriter = new PrintWriter(new FileWriter(lexerOutputPath));
             PrintWriter errorWriter = new PrintWriter(new FileWriter(errorOutputPath));
             PrintWriter symbolWriter = new PrintWriter(new FileWriter(symbolOutputPath));
             PrintWriter llvmWriter = new PrintWriter(new FileWriter(llvmOutputPath));
             PrintWriter llvmPhiWriter = new PrintWriter(new FileWriter(llvmPhiOutputPath));
             PrintWriter mipsWriter = new PrintWriter(new FileWriter(mipsOutputPath))
        ) {

            ArrayList<ErrorRecord> lexerErrorRecords = new ArrayList<>();
            ArrayList<ErrorRecord> parserErrorRecords = new ArrayList<>();
            ArrayList<ErrorRecord> visitorErrorRecords = new ArrayList<>();
            ArrayList<ErrorRecord> errorRecords = new ArrayList<>();

            Lexer lexer = new Lexer(reader, lexerErrorRecords);
            Parser parser = new Parser(lexer, parserErrorRecords);

            CompUnit compUnit = parser.parse();
            if (compUnit != null) {
                compUnit.analyze(outputWriter);
            }

            Visitor_Symtable visitor = new Visitor_Symtable(visitorErrorRecords);
            visitor.visitCompUnit(compUnit);

            errorRecords.addAll(lexerErrorRecords);
            errorRecords.addAll(parserErrorRecords);
            errorRecords.addAll(visitorErrorRecords);
            if (!errorRecords.isEmpty()) {
                errorRecords.sort((o1, o2) -> {
                    if (o1.getLineNumber() != o2.getLineNumber()) {
                        return o1.getLineNumber() - o2.getLineNumber();
                    }
                    return 0;
                });
                for (ErrorRecord errorRecord : errorRecords) {
                    errorWriter.println(errorRecord.getLineNumber() + " " + errorRecord.getErrorType().getCode());
                }
            } else {
                Visitor_IR irVisitor = new Visitor_IR();
                irVisitor.visitCompUnit(compUnit);
                IrModule irModule = irVisitor.irModule;
                addRetToBlockAndRemoveUnreachableInstr(irModule);
//                irModule.dump(llvmPhiWriter);
                if (optimize) {
                    new OptimizationPipeline().optimize(irModule);
                }
                irModule.dump(llvmWriter);
                Generator generator = new Generator();
                generator.generate(irModule);
                generator.module.dump(mipsWriter);
            }

            visitor.AllTable.sort(Comparator.comparingInt(o -> o.id));
            for (SymTable table : visitor.AllTable) {
                for (Symbol symbol : table.symbolList) {
                    symbolWriter.println(table.id + " " + symbol.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addRetToBlockAndRemoveUnreachableInstr(IrModule module) {
        for (Function func : module.getFunctionListWithMain()) {
            for (BasicBlock block : func.getBasicBlocks()) {
                if (block.getInstructions().isEmpty() ||
                        !(block.getLastInstruction() instanceof BrInstr) && !(block.getLastInstruction() instanceof ReturnInstr)) {
                    block.getInstructions().add(new ReturnInstr(block));
                } else {
                    boolean canRemove = false;
                    for (int i = 0; i < block.getInstructions().size(); i++) {
                        if (canRemove) {
                            block.getInstructions().remove(i);
                            i--;
                        } else if (block.getInstructions().get(i) instanceof BrInstr || block.getInstructions().get(i) instanceof ReturnInstr) {
                            canRemove = true;
                        }
                    }
                }

            }
        }
    }
}