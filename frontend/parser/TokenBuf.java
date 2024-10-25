package frontend.parser;

import frontend.lexer.Lexer;
import frontend.lexer.Token;

import java.io.IOException;
import java.util.LinkedList;

public class TokenBuf {
    private final boolean PRINTINFO = false;
    private Lexer lexer;
    private LinkedList<Token> buffer = new LinkedList<>();
    private Token preToken = null; // the last token that has been read
    private Token prepreToken = null; // the token before the last token that has been read

    public TokenBuf(Lexer lexer) throws IOException {
        this.lexer = lexer;
    }

    public Token get() {
        try {
            if (buffer.isEmpty()) {
                if (lexer.next()) {
                    Token token = lexer.getToken();
                    upDatePreToken(token);
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
                upDatePreToken(token);
                if (PRINTINFO)
                    System.out.println(token.getLineNum() + " " + token.getType().getTypename() + " " + token.getValue());
                return token;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Token read(int index) {
        // 下标从1开始
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unget(Token token) {
        // 暂时只考虑unget一个token
        buffer.addFirst(token);
        preToken = prepreToken;
        prepreToken = null;
    }

    private void upDatePreToken(Token token) {
        prepreToken = preToken;
        preToken = token;
    }

    public int getPrePreLineNum() {
        return prepreToken.getLineNum();
    }

    public int getPreLineNum() {
        return preToken.getLineNum();
    }
}
