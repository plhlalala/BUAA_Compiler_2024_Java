package middleend.type;

public enum BaseTypeEnum {
    INT("i32"),
    VOID("void"),
    CHAR("i8"),
    BOOL("i1"),
    ARRAY("[]"),
    LABEL("label");

    private final String value;

    BaseTypeEnum(String s) {
        value = s;
    }

    @Override
    public String toString() {
        return value;
    }
}
