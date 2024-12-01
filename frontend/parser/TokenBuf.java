package frontend.parser;

import frontend.lexer.Lexer;
import frontend.lexer.Token;

import java.io.IOException;
import java.util.LinkedList;

public class TokenBuf {
    private final Lexer lexer;
    private final LinkedList<Token> buffer = new LinkedList<>();
    private final LinkedList<Token> processedBuf = new LinkedList<>();
    private final LinkedList<Token> recoveryBuf = new LinkedList<>();
    private final LinkedList<Token> trytoParseBuf = new LinkedList<>();
    private boolean inRecovery = false;

    public TokenBuf(Lexer lexer) {
        this.lexer = lexer;
    }

    public Token get() {
        try {
            boolean PRINTINFO = false;
            if (!inRecovery) {
                return getToken(PRINTINFO, buffer, processedBuf);
            } else {
                return getToken(PRINTINFO, trytoParseBuf, recoveryBuf);

            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Token getToken(boolean PRINTINFO, LinkedList<Token> buffer, LinkedList<Token> processedBuf) throws IOException {
        if (buffer.isEmpty()) {
            if (lexer.next()) {
                Token token = lexer.getToken();
                processedBuf.add(token);
                if (PRINTINFO)
                    System.out.println(token.getLineNum() + " " + token.getType().getTypename() + " " + token.getValue());
                return token;
            } else {
                if (PRINTINFO)
                    System.out.println("EOF");
                return null;
            }
        } else {
            Token token = buffer.removeFirst();
            processedBuf.add(token);
            if (PRINTINFO)
                System.out.println(token.getLineNum() + " " + token.getType().getTypename() + " " + token.getValue());
            return token;
        }
    }

    public Token read(int index) {
        try {
            if (!inRecovery) {
                return getTokenToRead(index, buffer);
            } else {
                return getTokenToRead(index, trytoParseBuf);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Token getTokenToRead(int index, LinkedList<Token> buffer) throws IOException {
        if (buffer.size() < index) {
            while (buffer.size() < index) {
                if (lexer.next()) {
                    Token token = lexer.getToken();
                    buffer.add(token);
                } else {
                    return null;
                }
            }
        }
        return buffer.get(index - 1);
    }

    public void startRecovery() {
        inRecovery = true;
        trytoParseBuf.clear();
        trytoParseBuf.addAll(buffer);
        buffer.clear();
        recoveryBuf.clear();
        recoveryBuf.add(processedBuf.peekLast());
        processedBuf.pollLast();
    }

    public void doneRecovery() {
        inRecovery = false;
        processedBuf.addAll(recoveryBuf);
        recoveryBuf.clear();
        buffer.addAll(trytoParseBuf);
        trytoParseBuf.clear();
    }

    public void abortRecovery() {
        inRecovery = false;
        buffer.clear();
        buffer.addAll(recoveryBuf); //包含了第一个冲突的token
        buffer.addAll(trytoParseBuf);
        trytoParseBuf.clear();
        recoveryBuf.clear();
    }

    public int getPrePreLineNum() {
        if (inRecovery) {
            if (recoveryBuf.isEmpty()) {
                return processedBuf.get(processedBuf.size() - 2).getLineNum();
            } else if (recoveryBuf.size() == 1) {
                return processedBuf.peekLast().getLineNum();
            } else {
                return recoveryBuf.get(recoveryBuf.size() - 2).getLineNum();
            }
        } else {
            return processedBuf.get(processedBuf.size() - 2).getLineNum();
        }
    }

    public int getPreLineNum() {
        if (inRecovery) {
            if (recoveryBuf.isEmpty()) {
                return processedBuf.peekLast().getLineNum();
            }
            return recoveryBuf.peekLast().getLineNum();
        } else {
            return processedBuf.peekLast().getLineNum();
        }
    }
}
