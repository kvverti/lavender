package kvverti.lavender.operators;

import java.util.Comparator;

import kvverti.lavender.Stack;

/**
 * Operator precedence: Operators may be binary infix, or unary prefix. Technically n-ary
 * prefix, but parsed as unary due to the behavior of the comma (,) operator. For parsing
 * purposes, parentheses have the lowest precedence, ie. they bind furthest.
 * Precedence table:
 * Function:        [dom:]name     n-ary prefix
 * Postfix function:               unary postfix
 * Exponentiation:  **             binary right infix
 * Symbol prefix:   + - !          unary prefix
 * Multiplication:  * / %          binary left infix
 * Addition:        + -            binary left infix
 * Comparison:      = != < > <= >= binary left infix
 * Boolean:         & | ^          binary left infix
 * Infix function:                 n-ary left infix
 * Separator:       ,              binary left infix
 * Brackets:        ( )            grouping
 */
public abstract class Operator {
    
    public static final int NA = -1;
    public static final int PREFIX = 0;
    public static final int LEFT_INFIX = 1;
    public static final int RIGHT_INFIX = 2;
    
    private final String str;
    private final int arity;
    private final int returns;
    private final int fixing;
    
    protected Operator(String dbname, int ar, int ret, int f) {
        
        str = dbname;
        arity = ar;
        returns = ret;
        fixing = f;
    }
    
    /** Operator arity. Returns a value >= 0 */
    public int arity() { return arity; }
    
    /** Returns the number of captured "free" variables */
    public int captured() { return 0; }
    
    public int returns() { return returns; }
    
    public int fixing() { return fixing; }
    
    public boolean rightAssoc() { return fixing == PREFIX || fixing == RIGHT_INFIX; }
    
    public boolean isByNameParam(int i) {
        
        assert i >= 0 && (arity() < 0 || i < arity()) : i;
        return false;
    }
    
    public Operator asValue() {
        
        return arity() == 0 ? this : new FunctionValue(this);
    }
    
    /** No-op by default, should override. */
    public void eval(Operator[] params, Stack stack) { }
    
    public Operator resolve() { return this; }
    
    @Override
    public String toString() { return str; }
    
    /**
     * Precedence rules for functions. Precedence depends on a function's arity,
     * fixing, and name. All values (zero-arity functions) have the same highest arity.
     * All prefix functions have the next highest arity. Infix operators have lower
     * precedence than prefix operators, based on their names. Postfix operators
     * have lower precedence than infix operators.
     */
    public static final Comparator<Operator> BY_PRECEDENCE = new Comparator<Operator>() {
      
        @Override
        public int compare(Operator a, Operator b) {
            
            //values have highest precedence
            {
                int ar = a.arity() == 0 ? 1 : 0;
                int br = b.arity() == 0 ? 1 : 0;
                if(ar + br != 0)
                    return ar - br;
                assert a.arity() != 0 && b.arity() != 0 : a.arity() + " " + b.arity();
            }
            //closing groupers have next highest
            String aname = a.toString();
            String bname = b.toString();
            {
                int ac = aname.equals(")") || aname.equals("]") ? 1 : 0;
                int bc = bname.equals(")") || bname.equals("]") ? 1 : 0;
                if(ac + bc != 0)
                    return ac - bc;
                assert ac == 0 && bc == 0 : aname + " " + bname;
            }
            //opening groups have the lowest precedence
            if(aname.equals("(") || aname.equals("["))
                return -1;
            if(bname.equals("(") || bname.equals("["))
                return 1;
            //prefix > infix = postfix
            int afix;
            int bfix;
            {
                if(a.fixing() == Operator.PREFIX)
                    afix = 2;
                //infix
                else
                    afix = 0;
                if(b.fixing() == Operator.PREFIX)
                    bfix = 2;
                else
                    bfix = 0;
                if(afix > bfix)
                    return 1;
                else if(afix < bfix)
                    return -1;
            }
            assert afix == bfix;
            if(afix != 0) //not infix
                return 0;
            //operators are infix - compare names with modified Scala ordering
            //note that '**' has greater precedence than other multiplicative
            //combinations
            aname = aname.split(":", 2)[1];
            bname = bname.split(":", 2)[1];
            int ap = getLexicographicPrecedence(aname.charAt(0));
            int bp = getLexicographicPrecedence(bname.charAt(0));
            if(ap > bp)
                return 1;
            else if(ap < bp)
                return -1;
            //precedences are equal
            //test for exponentials '**'
            if(aname.startsWith("**") && !bname.startsWith("**"))
                return 1;
            if(bname.startsWith("**") && !aname.startsWith("**"))
                return -1;
            //exact same precedence!
            return 0;
        }
        
        private int getLexicographicPrecedence(char c) {
            
            //precedence as follows from greatest to least
            switch(c) {
                
                case '|': return 1;
                case '^': return 2;
                case '&': return 3;
                case '!':
                case '=': return 4;
                case '>':
                case '<': return 5;
                case ':': return 6;
                case '-':
                case '+': return 7;
                case '%':
                case '/':
                case '*': return 8;
                case '~':
                case '?': return 9;
                default:  return 0;
            }
        }
    };
}