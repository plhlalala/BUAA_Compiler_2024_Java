package frontend;

public class ErrorRecord {
    private final ErrorType errorType;
    private final int lineNumber;

    public ErrorRecord(ErrorType errorType, int lineNumber) {
        this.errorType = errorType;
        this.lineNumber = lineNumber;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return String.format("ErrorType: %s, LineNumber: %d", errorType.getCode(), lineNumber);
    }
}