package frontend.parser;

import frontend.lexer.Lexer;
import frontend.lexer.Token;

import java.io.IOException;
import java.util.LinkedList;

public class TokenBuf {
    private final boolean PRINTINFO = false;
    private Lexer lexer;
    private LinkedList<Token> buffer = new LinkedList<>();
    //    private Token preToken = null; // the last token that has been read
    //    private Token prepreToken = null; // the token before the last token that has been read
    private boolean inRecovery = false;
    private LinkedList<Token> processedBuf = new LinkedList<>();
    private LinkedList<Token> recoveryBuf = new LinkedList<>();
    private LinkedList<Token> trytoParseBuf = new LinkedList<>();

    public TokenBuf(Lexer lexer) throws IOException {
        this.lexer = lexer;
    }

    public Token get() {
        try {
            if (inRecovery == false) {
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
            } else {
                if (trytoParseBuf.isEmpty()) {
                    if (lexer.next()) {
                        Token token = lexer.getToken();
                        recoveryBuf.add(token);
                        if (PRINTINFO)
                            System.out.println(token.getLineNum() + " " + token.getType().getTypename() + " " + token.getValue());
                        return token;
                    } else {
                        if (PRINTINFO)
                            System.out.println("EOF");
                        return null;
                    }
                } else {
                    Token token = trytoParseBuf.removeFirst();
                    recoveryBuf.add(token);
                    if (PRINTINFO)
                        System.out.println(token.getLineNum() + " " + token.getType().getTypename() + " " + token.getValue());
                    return token;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Token read(int index) {
        try {
            if (inRecovery == false) {
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
            } else {
                if (trytoParseBuf.size() < index) {
                    while (trytoParseBuf.size() < index) {
                        if (lexer.next()) {
                            Token token = lexer.getToken();
                            trytoParseBuf.add(token);
                        } else {
                            return null;
                        }
                    }
                }
                return trytoParseBuf.get(index - 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

//    public void unget(Token token) {
//        // 暂时只考虑unget一个token
//        buffer.addFirst(token);
//        preToken = prepreToken;
//        prepreToken = null;
//    }

//    private void upDatePreToken(Token token) {
//        prepreToken = preToken;
//        preToken = token;
//    }

    public int getPrePreLineNum() {
        if (inRecovery) {
            if (recoveryBuf.size() == 0) {
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
            if (recoveryBuf.size() == 0) {
                return processedBuf.peekLast().getLineNum();
            }
            return recoveryBuf.peekLast().getLineNum();
        } else {
            return processedBuf.peekLast().getLineNum();
        }
    }
}
