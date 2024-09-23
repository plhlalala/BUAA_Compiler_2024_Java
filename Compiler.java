import frontend.ErrorRecord;
import frontend.Lexer;
import frontend.Token;

import javax.swing.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.FileWriter;
import java.util.ArrayList;


public class Compiler {
    public static void main(String[] args) {
        String inputFilePath = "testfile.txt";  // 输入文件
        String lexerOutputPath = "lexer.txt";   // 词法分析正确输出文件
        String errorOutputPath = "error.txt";   // 错误输出文件

        try (PushbackReader reader = new PushbackReader(new FileReader(inputFilePath));
             PrintWriter lexerWriter = new PrintWriter(new FileWriter(lexerOutputPath));
             PrintWriter errorWriter = new PrintWriter(new FileWriter(errorOutputPath))) {
            ArrayList<ErrorRecord> errorRecords = new ArrayList<>();
            Lexer lexer = new Lexer(reader, errorRecords);
            while (lexer.next()) {
                Token token = lexer.getToken();
                lexerWriter.printf("%s %s\n", token.getType().toString(), token.getValue());
            }
            if (!errorRecords.isEmpty()) {
                for (ErrorRecord errorRecord : errorRecords) {
                    errorWriter.println(errorRecord.getLineNumber() + " " + errorRecord.getErrorType().getCode());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}