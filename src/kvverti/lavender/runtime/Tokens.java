package kvverti.lavender.runtime;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Tokens {
    
    private static final Pattern WS;
    private static final Pattern IDENT;
    private static final Pattern QUAL_IDENT;
    private static final Pattern NUMBER;
    private static final Pattern SYMBOL;
    private static final Pattern QUAL_SYMBOL;
    private static final Pattern FUNC_SYMBOL;
    private static final Pattern FUNC_VAL;
    private static final Pattern QUAL_FUNC_VAL;
    private static final Pattern STRING;
    
    public static List<Token> split(String str) {
        
        List<Token> res = new ArrayList<>();
        int idx = 0;
        int end = 0;
        Matcher m = WS.matcher(str);
        while(idx < str.length()) {
            
            m.region(idx, m.regionEnd());
            if(str.charAt(idx) == '\'')
                return res;
            if(m.usePattern(WS).lookingAt()) {
                ; //eat it up!
            } else if(m.usePattern(QUAL_IDENT).lookingAt()) {
                res.add(new Token(m.group(), Token.QUAL_IDENT));
            } else if(m.usePattern(FUNC_SYMBOL).lookingAt()) {
                res.add(new Token(m.group(), Token.FUNC_SYMBOL));
            } else if(m.usePattern(NUMBER).lookingAt()) {
                res.add(new Token(m.group(), Token.NUMBER));
            } else if(m.usePattern(QUAL_SYMBOL).lookingAt()) {
                res.add(new Token(m.group(), Token.QUAL_SYMBOL));
            } else if(m.usePattern(IDENT).lookingAt()) {
                res.add(new Token(m.group(), Token.IDENT));
            } else if(m.usePattern(SYMBOL).lookingAt()) {
                res.add(new Token(m.group(), Token.SYMBOL));
            } else if(m.usePattern(QUAL_FUNC_VAL).lookingAt()) {
                res.add(new Token(m.group(), Token.QUAL_FUNC_VAL));
            } else if(m.usePattern(FUNC_VAL).lookingAt()) {
                res.add(new Token(m.group(), Token.FUNC_VAL));
            } else if((end = getStringToken(str, idx)) > idx) {
                res.add(new Token(str.substring(idx, end), Token.STRING));
                idx = end;
                continue;
            } else {
                res.add(new Token(str.substring(idx, idx + 1), Token.LITERAL));
                idx++;
                continue;
            }
            idx = m.end();
        }
        return res;
    }
    
    /** Returns the bounds for the next string token starting at idx. */
    private static int getStringToken(String s, int idx) {
        
        assert idx < s.length();
        char c = s.charAt(idx);
        if(c != '\"')
            return idx;
        int i = idx;
        i++;
        while(i < s.length() && (c = s.charAt(i)) != '\"') {
            
            assert c != '\"';
            //check escape sequences
            if(c == '\\') {
                i++;
                if(i < s.length()) {
                    switch(s.charAt(i)) {
                        //valid escape sequence?
                        //note we don't assign this character to c
                        case 'n':
                        case 't':
                        case '\'':
                        case '\"':
                        case '\\': i++; break;
                        default: return idx;
                    }
                } else
                    return idx;
            } else {
                i++;
            }
        }
        if(i >= s.length())
            return idx;
        assert c == '\"' : c;
        return i + 1;
    }
    
    static {
        
        final String id = "[A-Za-z_][A-Za-z_0-9]*";
        final String sym = "[~!%\\^\\&*\\-+=|<>/?][~!%\\^\\&*\\-+=|<>/?:]*";
        final String qualId = "((?:" + id + ")?):(" + id + ")";
        final String qualSym = "((?:" + id + ")?):(" + sym + ")";
        WS = Pattern.compile("\\s+");
        IDENT = Pattern.compile(id);
        SYMBOL = Pattern.compile(sym);
        NUMBER = Pattern.compile("\\d*\\.?\\d+(?:[e][+-]?\\d+)?");
        FUNC_SYMBOL = Pattern.compile("[uir]_" + sym);
        QUAL_IDENT = Pattern.compile(qualId);
        QUAL_SYMBOL = Pattern.compile(qualSym);
        FUNC_VAL = Pattern.compile("\\\\(" + id + "|" + sym + ")\\\\?");
        QUAL_FUNC_VAL = Pattern.compile("\\\\(" + qualId + "|" + qualSym + ")\\\\?");
        STRING = Pattern.compile("\"([^\"\\\\]|\\\\[nt'\"\\\\])*?\"");
    } 
}