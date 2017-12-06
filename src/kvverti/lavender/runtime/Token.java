package kvverti.lavender.runtime;

public class Token {
    
    public static final int IDENT = 0;
    public static final int NUMBER = 1;
    public static final int QUAL_IDENT = 2;
    public static final int SYMBOL = 3;
    public static final int QUAL_SYMBOL = 4;
    public static final int FUNC_SYMBOL = 9;
    public static final int LITERAL = 5;
    public static final int FUNC_VAL = 6;
    public static final int QUAL_FUNC_VAL = 7;
    public static final int STRING = 8;
    
    private final String value;
    private final int type;
    
    public Token(String v, int t) { value = v; type = t; }
    
    public String value() { return value; }
    
    public int type() { return type; }
    
    @Override
    public String toString() { return value; }
}