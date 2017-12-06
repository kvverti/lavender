package kvverti.lavender;

import kvverti.lavender.operators.*;
import kvverti.lavender.Stack;
import kvverti.lavender.runtime.*;
import static kvverti.lavender.operators.Builtin.*;
import static kvverti.lavender.operators.Logic.*;
import static kvverti.lavender.operators.Io.*;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Deque; //for use as a stack
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.NoSuchElementException;
import java.util.BitSet;
import java.util.Comparator;
import java.util.regex.*;
import java.io.*;

public class Expr {    
    
/*     private static final Pattern IDENT;
    private static final Pattern QUAL_IDENT;
    private static final Pattern NUMBER = Pattern.compile("\\d*\\.?\\d+(?:[e][+-]?\\d+)?");
    private static final Pattern SYMBOL;
    private static final Pattern QUAL_SYMBOL;
    private static final Pattern FUNC_SYMBOL;
    private static final Pattern FUNC_VAL;
    private static final Pattern QUAL_FUNC_VAL;
    private static final Pattern STRING;
    static {
        final String id = "[A-Za-z_][A-Za-z_0-9]*";
        final String sym = "[~!%\\^\\&*\\-+=|<>/?,][~!%\\^\\&*\\-+=|<>/?,:]*";
        final String qualId = "((?:" + id + ")?):(" + id + ")";
        final String qualSym = "((?:" + id + ")?):(" + sym + ")";
        IDENT = Pattern.compile(id);
        SYMBOL = Pattern.compile(sym);
        FUNC_SYMBOL = Pattern.compile("[uir]_" + sym);
        QUAL_IDENT = Pattern.compile(qualId);
        QUAL_SYMBOL = Pattern.compile(qualSym);
        FUNC_VAL = Pattern.compile("\\\\(" + id + "|" + sym + ")\\\\?");
        QUAL_FUNC_VAL = Pattern.compile("\\\\(" + qualId + "|" + qualSym + ")\\\\?");
        STRING = Pattern.compile("\"([^\"\\\\]|\\\\[nt'\"\\\\])*?\"");
    }
    
    private static class Token {
        
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
        
        Token(String v, int t) { value = v; type = t; }
        
        public String value() { return value; }
        
        public int type() { return type; }
        
        @Override
        public String toString() { return value; }
    } */
    
/*     public static List<Token> splitTokens(String input) {
        
        List<Token> res = new ArrayList<>();
        int idx = 0;
        Matcher m = WS.matcher(input);
        while(idx < input.length()) {
            
            m.region(idx, m.regionEnd());
            if(input.charAt(idx) == '\'')
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
            } else if(m.usePattern(STRING).lookingAt()) {
                res.add(new Token(m.group(), Token.STRING));
            } else {
                res.add(new Token(input.substring(idx, idx + 1), Token.LITERAL));
                idx++;
                continue;
            }
            idx = m.end();
        }
        return res;
    } */
    
    //increment for splitNameAndArgs
    private static int inc = 0;
    
    /**
     * Splits a named function definition into its name, params, and body.
     * The argument will contain the function bodies.
     * Format of input:
     *  name(p1, p2, ...) => expr
     *  name(p1, p2, ...) => expr; expr [=> expr; expr]
     * eg.
     *  sqrt(t) => t ** 0.5
     *  sine(t) => sin(2 * pi * t)
     *  max(a, b)
     *    => a ; a > b
     *    => b ; a <= b
     * Order of return values:
     *  Index 0: Function name
     *  Index 1-end: Function param names
     */
    public static List<Token> splitNameAndArgs(ListIterator<Token> itr) {
        
        try {
            List<Token> res = new ArrayList<>();
            //get the function name
            Token t = itr.next();
            if(t.value().equals("def") || t.value().equals("=>")) {
                throw new RuntimeException("Expected function name");
            }
            if(t.type() == Token.IDENT || t.type() == Token.FUNC_SYMBOL || t.type() == Token.SYMBOL) {
                //functions can't end with underscores
                if(t.value().endsWith("_"))
                    throw new RuntimeException("Function name may not end with '_'");
                res.add(t);
                itr.remove();
                //get the left paren
                t = itr.next();
            } else
                res.add(new Token("anon$" + inc++, Token.LITERAL));
            if(!t.value().equals("("))
                throw new RuntimeException("Unexpected token: " + t);
            itr.remove();
            //get the args
            while(!(t = itr.next()).value().equals(")")) {
                
                itr.remove();
                if(t.value().equals("=>")) {
                    String mangled = "=>$";
                    t = itr.next();
                    itr.remove();
                    if(t.type() != Token.IDENT)
                        throw new RuntimeException("Unexpected token: " + t);
                    mangled += t.value();
                    res.add(new Token(mangled, Token.IDENT));
                } else if(t.type() == Token.IDENT) {
                    res.add(t);
                } else if(!t.value().equals(","))
                    throw new RuntimeException("Unexpected token: " + t);
            }
            itr.remove();
            return res;
        } catch(NoSuchElementException e) {
            
            throw new RuntimeException("Reached end of statement while parsing");
        }
    }
    
    /**
     * Splits the (possibly piecewise) function body into a list of bodies and conditions.
     * Bodies occupy even locations and conditions occupy odd locations.
     * If the function is not piecewise, the returned list is of length 1.
     */
    public static List<List<Token>> splitBodies(ListIterator<Token> itr) {
        
        List<List<Token>> res = new ArrayList<>();
        Token t = itr.next();
        if(!t.value().equals("=>"))
            throw new RuntimeException("Unexpected token: " + t);
        if(!itr.hasNext())
            throw new RuntimeException("Function body expected");
        //keep track of bracket nesting so we know when to end
        int nesting = 0;
        boolean cond = false;
        List<Token> body = new ArrayList<>();
        parse:
        do {
            t = itr.next();
            if(cond && t.value().equals("=>") && nesting == 0) {
                cond = false;
                res.add(body);
                body = new ArrayList<>();
            } else if(!cond && t.value().equals(";") && nesting == 0) {
                cond = true;
                res.add(body);
                body = new ArrayList<>();
            } else {
                String str = t.value();
                if(str.equals("[") || str.equals("("))
                    nesting++;
                else if(str.equals("]") || str.equals(")")) {
                    nesting--;
                }
                if(nesting >= 0)
                    body.add(t);
                else
                    itr.previous();
            }
        //get the rest of the piecewise parts
        } while(itr.hasNext() && nesting >= 0);
        //add the last one
        if(!body.isEmpty())
            res.add(body);
        if(res.size() > 1 && res.size() % 2 != 0)
            throw new RuntimeException("Must specify definition after separator");
        return res;
    }
    
    private static final Pattern WS = Pattern.compile("\\s+");
    private static final Map<String, Operator> operators;
    private static final Map<String, Operator> functions;
    static {
        
        //operators, expect operands after
        operators = new HashMap<>();
        operators.put(":**", POWER);
        operators.put(":*", TIMES);
        operators.put(":/", DIVIDE);
        operators.put(":%", REMAINDER);
        operators.put(":+", PLUS);
        operators.put(":-", MINUS);
        operators.put(":,", COMMA);
        operators.put(":&", AND);
        operators.put(":|", OR);
        operators.put(":^", XOR);
        operators.put(":=", EQ);
        operators.put(":!=", NE);
        operators.put(":>", GT);
        operators.put(":<", LT);
        operators.put(":>=", GE);
        operators.put(":<=", LE);
        
        //operands, arity 0 expects operator after, others expect operand after
        functions = new HashMap<>();
        functions.put(":abs", ABS);
        functions.put(":sin", SIN);
        functions.put(":cos", COS);
        functions.put(":pi", PI);
        functions.put(":e", E);
        functions.put(":log", LOG);
        functions.put(":ceil", CEIL);
        functions.put(":floor", FLOOR);
        functions.put(":int", INT);
        functions.put(":max", MAX);
        functions.put(":min", MIN);
        functions.put(":str", STR);
        functions.put(":num", NUM);
        functions.put(":+", UPLUS);
        functions.put(":-", UMINUS);
        functions.put(":!", NOT);
        functions.put("io:print", PRINT);
        functions.put("io:getc", GETC);
        functions.put("io:close", CLOSE);
        functions.put("io:open", OPEN);
        functions.put("io:gets", GETS);
        functions.put("io:stdin", new FileObj(true));
        functions.put("io:stdout", new FileObj(false));
    }
    
    /** Marker class for forward declared functions. Will be replaced with real function. */
    private static class FunctionDecl extends Operator {
        
        private final int captured;
        private final BitSet byNameParams;
        
        private FunctionDecl(String name, int arity, BitSet byName, int cap, int fix) {
            
            super(name, arity, 1, fix);
            captured = cap;
            byNameParams = byName;
        }
        
        public static Operator add(String name, List<Token> params, int captured, int fix) {
            
            BitSet byName = new BitSet();
            for(int i = 0; i < params.size(); i++) {
                byName.set(i, params.get(i).value().startsWith("=>$"));
            }
            Operator op = new FunctionDecl(name, params.size(), byName, captured, fix);
            //don't override possible definitions
            if(fix == Operator.PREFIX)
                functions.putIfAbsent(name, op);
            else
                operators.putIfAbsent(name, op);
            return op;
        }
        
        @Override
        public Operator resolve() {
            
            Operator res;
            if(fixing() == Operator.PREFIX)
                res = functions.get(toString());
            else
                res = operators.get(toString());
            assert res != null : this;
            if(res == this)
                throw new RuntimeException("Linkage error: " + toString() + " is not defined");
            return res;
        }
        
        @Override
        public void eval(Operator[] params, Stack stack) {
            
            throw new RuntimeException("Linkage error: " + toString() + " is not defined");
        }
        
        @Override
        public int captured() {
            
            return captured;
        }
        
        @Override
        public boolean isByNameParam(int i) {
            
            return byNameParams.get(i);
        }
    }
    
    private static Operator wrap(boolean wrap, Operator op) {
        
        if(wrap && op.arity() > 0)
            return new FunctionValue(op);
        return op;
    }
    
    private static Operator getUnqualified(String domain, String name, Map<String, Operator> map) {
        
        //check namespaces
        Operator o = map.get(domain + ":" + name);
        int idx = domain.lastIndexOf("$");
        while(o == null && idx >= 0) {
            //go through nested namespaces
            domain = domain.substring(0, idx);
            o = map.get(domain + ":" + name);
            idx = domain.lastIndexOf("$");
        }
        if(o == null) {
            //check imports
            o = map.get(importedFunctionNames.get(name));
            if(o == null) {
                //check global namespaces
                o = map.get("global:" + name);
                if(o == null) {
                    o = map.get(":" + name);
                }
            }
        }
        return o;
    }
    
    private static Operator addFuncDecl(String domain, String name, List<Token> args, int captured) {
        
        int fix;
        if(name.startsWith("i_")) {
            if(args.isEmpty())
                throw new RuntimeException("Value may not be infix");
            name = name.substring(2);
            fix = Operator.LEFT_INFIX;
        } else if(name.startsWith("r_")) {
            if(args.isEmpty())
                throw new RuntimeException("Value may not be infix");
            name = name.substring(2);
            fix = Operator.RIGHT_INFIX;
        } else {
            if(name.startsWith("u_"))
                name = name.substring(2);
            fix = Operator.PREFIX;
        }
        return FunctionDecl.add(domain + ":" + name, args, captured, fix);
    }
    
    private static Operator parseFunction(String domain, List<Token> capture, ListIterator<Token> expr) {
        
        List<Token> args = splitNameAndArgs(expr);
        String name = args.remove(0).value();
        args.addAll(capture);
        int fix;
        {
            Operator op = addFuncDecl(domain, name, args, capture.size());
            fix = op.fixing();
            name = op.toString().substring(domain.length() + 1);
        }
        String domainAndName = domain + ":" + name;
        BitSet byName = new BitSet();
        for(int i = 0; i < args.size(); i++) {
            if(args.get(i).value().startsWith("=>$")) {
                byName.set(i, true);
                args.set(i, new Token(args.get(i).value().substring(3), Token.IDENT));
            }
        }
        {
            //check consistency
            Map<String, Operator> map = fix == Operator.PREFIX ? functions : operators;
            Operator op = map.get(domainAndName);
            assert op != null : domainAndName;
            if(!(op instanceof FunctionDecl))
                throw new RuntimeException("Duplicate definition of " + domainAndName);
            for(int i = 0; i < args.size(); i++) {
                if(byName.get(i) != op.isByNameParam(i))
                    throw new RuntimeException("Mismatched parameter passing: " + i);
            }
        }
        List<List<Token>> funcBodies = splitBodies(expr);
        assert funcBodies.size() > 0 : "funcBodies.size() == 0";
        Operator proc;
        if(funcBodies.size() > 1) {
            //make a piecewise definition
            List<Queue<Operator>> conds = new ArrayList<>();
            List<Queue<Operator>> codes = new ArrayList<>();
            for(int i = 0; i < funcBodies.size(); i += 2) {
                
                List<Operator> ls =
                    convert(fix, domain, name, args, capture.size(), funcBodies.get(i).listIterator());
                codes.add(parse(ls, args.size()));
                ls = convert(fix, domain, name, args, capture.size(), funcBodies.get(i + 1).listIterator());
                conds.add(parse(ls, args.size()));
            }
            proc = new PiecewiseStackProcedure(
                domainAndName, args.size(), capture.size(), fix, conds, codes, byName);
        } else {
            
            List<Operator> ls =
                convert(fix, domain, name, args, capture.size(), funcBodies.get(0).listIterator());
            Queue<Operator> out = parse(ls, args.size());
            proc = new StackProcedure(domainAndName, args.size(), capture.size(), fix, out, byName);
        }
        if(proc.fixing() == Operator.PREFIX)
            functions.put(proc.toString(), proc);
        else
            operators.put(proc.toString(), proc);
        return proc;
    }
    
    /**
     * Splits an expression into its component operations. These expressions
     * are returned in infix order, and may be sorted by a call to parse().
     */
    public static List<Operator> convert(int fix,
        String domain,
        String name,
        List<Token> params,
        int numCapture,
        ListIterator<Token> expr) {
        
        List<Operator> res = new ArrayList<>();
        boolean operand = true;
        //track paren nesting, so we know when to stop parsing
        //this (possibly nested) def
        int nesting = 0;
        Operator THIS = (fix == Operator.PREFIX ? functions : operators).get(domain + ":" + name);
        assert THIS != null || name.isEmpty() : domain + ":" + name;
        parse:
        while(expr.hasNext()) {
            
            Token t = expr.next();
            String value = t.value();
            boolean wrap = false;
            switch(t.type()) {
                
            case Token.FUNC_VAL:
                wrap = true;
                operand = !value.endsWith("\\");
                value = value.substring(1, operand ? value.length() : value.length() - 1);
                //fallthrough
                
            case Token.IDENT:
                //ilk, using continue...
                if(operand && !wrap) {
                    //is it a nested def?
                    if(value.equals("def")) {
                        Operator def = parseFunction(
                            name.isEmpty() ? domain : domain + "$" + name.replace(":", "@"), params, expr);
                        if(params.size() > 0) {
                            res.add(CAPTURE);
                            res.add(LEFT_PAREN);
                        }
                        //add new function
                        res.add(wrap(true, def));
                        if(params.size() > 0) {
                            //add params for capture
                            for(int i = 0; i < params.size(); i++) {
                                res.add(new Parameter(i));
                            }
                            res.add(RIGHT_PAREN);
                        }
                        operand = false;
                        continue parse;
                    } else {
                        //try a parameter
                        for(int i = 0; i < params.size(); i++) {
                            if(params.get(i).value().equals(value)) {
                                res.add(new Parameter(i));
                                //expect an operator after a value
                                operand = false;
                                continue parse;
                            }
                        }
                    }
                }
                //fallthrough
                
            case Token.SYMBOL:
                //recursion check
                if((operand == (fix == Operator.PREFIX)) && value.equals(name)) {
                    if(numCapture > 0) {
                        if(!wrap) {
                            //add captured params to the stack
                            res.add(LEFT_BRACKET);
                            for(int i = params.size() - numCapture; i < params.size(); i++)
                                res.add(new Parameter(i));
                            //force wrapping
                            //res.add(new This(params.size(), fix, true));
                            res.add(THIS.asValue());
                            res.add(RIGHT_BRACKET);
                        } else {
                            //create a capture instance to return
                            res.add(CAPTURE);
                            res.add(LEFT_PAREN);
                            res.add(THIS.asValue());
                            for(int i = params.size() - numCapture; i < params.size(); i++)
                                res.add(new Parameter(i));
                            res.add(RIGHT_PAREN);
                        }
                    } else
                        res.add(wrap(wrap, THIS));
                    operand = (operand ? params.size() > 0 : params.size() > 1) && !wrap;
                } else {
                    //check namespaces
                    Operator o = getUnqualified(domain, value, operand ? functions : operators);
                    if(o == null)
                        throw new RuntimeException("Unqualified name not found: " + value);
                    if(o.captured() == 0)
                        res.add(wrap(wrap, o));
                    else {
                        //add implicit captured environment
                        //see above code for self-recursive version
                        if(!wrap) {
                            res.add(LEFT_BRACKET);
                            for(int i = params.size() - o.captured(); i < params.size(); i++)
                                res.add(new Parameter(i));
                            res.add(wrap(true, o));
                            res.add(RIGHT_BRACKET);
                        } else {
                            res.add(CAPTURE);
                            res.add(LEFT_PAREN);
                            res.add(wrap(true, o));
                            for(int i = params.size() - o.captured(); i < params.size(); i++)
                                res.add(new Parameter(i));
                            res.add(RIGHT_PAREN);
                        }
                    }
                    int formal = o.arity() - o.captured();
                    operand = (operand ? (formal > 0) : (formal > 1)) && !wrap;
                }
                break;
                
            case Token.QUAL_FUNC_VAL:
                wrap = true;
                operand = !value.endsWith("\\");
                value = value.substring(1, operand ? value.length() : value.length() - 1);
                //fallthrough
                
            case Token.QUAL_SYMBOL:
            case Token.QUAL_IDENT:
                if(value.equals(domain + ":" + name)) {
                    assert numCapture == 0 : value;
                    res.add(wrap(wrap, THIS));
                    operand = (operand ? params.size() > 0 : params.size() > 1) && !wrap;
                } else {
                    //check known functions
                    //we don't worry about capturing since
                    //qualified names are always top level
                    Map<String, Operator> map = operand ? functions : operators;
                    Operator o = map.get(value);
                    if(o == null)
                        throw new RuntimeException("Function define not found: " + value);
                    res.add(wrap(wrap, o));
                    operand = (operand  ? (o.arity() > 0) : (o.arity() > 1)) && !wrap;
                }
                break;
                
            case Token.NUMBER:
                if(!operand)
                    throw new RuntimeException("Unexpected token: " + value);
                res.add(Constant.of(Double.parseDouble(value)));
                operand = false;
                break;
                
            case Token.STRING:
                if(!operand)
                    throw new RuntimeException("Unexpected token: " + value);
                res.add(StringOp.of(value).asValue());
                operand = false;
                break;
                
            case Token.LITERAL:
                if(operand && value.equals("(")) {
                    res.add(LEFT_PAREN);
                } else if(!operand && value.equals(")")) {
                    res.add(RIGHT_PAREN);
                } else if(operand && value.equals("[")) {
                    res.add(LEFT_BRACKET);
                } else if(!operand && value.equals("]")) {
                    res.add(RIGHT_BRACKET);
                    operand = true;
                } else if(!operand && value.equals(",")) {
                    //add nothing
                    operand = true;
                } else
                    throw new RuntimeException("Unexpected token: " + value);
                break;
                
            default:
                throw new RuntimeException("Unexpected token: " + value);
            }
        }
        if(nesting != 0)
            throw new RuntimeException("Unbalanced brackets");
        return res;
    }
    
    private static final Operator BEGIN_ARGS = new Operator("PIPE", 0, 0, Operator.NA){};
    
    /**
     * An implementation of Dijkstra's shunting-yard algorithm, modified slightly to
     * support the comma separator. Yes, there are better parses, but apparently
     * this is "fast."
     */
    public static Queue<Operator> parse(List<Operator> in, int numParams) {
        
        Deque<Operator> out = new ArrayDeque<>();
        Deque<Operator> ops = new ArrayDeque<>();
        for(Operator op : in) {
            
            // System.out.println("op: " + op);
            if(op.arity() == 0)
                //we'll have to shunt it over anyway
                out.add(op);
            else if(op == LEFT_PAREN)
                //just push it
                ops.push(op);
            else if(op == LEFT_BRACKET) {
                //push token onto operator stack AND output stack
                out.add(op);
                ops.push(op);
            } else if(op == RIGHT_BRACKET) {
                //pop output onto operator stack until [
                //then push ] onto operator to mark
                while(out.peekLast() != LEFT_BRACKET)
                    ops.push(out.removeLast());
                out.removeLast();
                out.add(BEGIN_ARGS);
                ops.push(op);
            } else if(op == RIGHT_PAREN) {
                //shunt over all the operators until we hit left paren
                //we don't check bounds, correct syntax will not underflow
                while(ops.peek() != LEFT_PAREN) {
                    //handle right bracket rules
                    if(ops.peek() == RIGHT_BRACKET)
                        handleRightBracket(out, ops);
                    else
                        out.add(ops.pop());
                }
                //pop the left paren, don't push right paren
                ops.pop();
            } else {
                //shunt over all the ops of > precedence
                //if right assoc. >= if left assoc.
                int sub = op.rightAssoc() ? 0 : 1;
                while(!ops.isEmpty() && Operator.BY_PRECEDENCE.compare(op, ops.peek()) - sub < 0) {
                    //handle right bracket rules
                    if(ops.peek() == RIGHT_BRACKET)
                        handleRightBracket(out, ops);
                    else
                        out.add(ops.pop());
                }
                //..and push the operator on...unless it's comma, lol
                if(op != COMMA) {
                    if(op.arity() == -1)
                        out.add(BEGIN_ARGS);
                    ops.push(op);
                }
            }
            // System.out.println("out: " + out);
            // System.out.println("ops: " + ops);
            // System.out.println("============");
        }
        //shunt the leftover operators onto output stack
        //this *will* work properly even if you didn't close your parens
        while(!ops.isEmpty())
            if(ops.peek() == RIGHT_BRACKET)
                handleRightBracket(out, ops);
            else
                out.add(ops.pop());
        //elements of out are in postfix order
        if(debug) {
            System.out.println(out);
        }
        ExprTree tree = new ExprTree(out, numParams);
        out = tree.flattenPostOrder();
        if(debug) {
            tree.print(System.out);
            System.out.println(out);
        }
        //out = foldConstants(out);
        return out;
    }
    
    private static void handleRightBracket(Queue<Operator> out, Deque<Operator> ops) {
        
        assert ops.peek() == RIGHT_BRACKET : ops.peek();
        //pop ]
        ops.pop();
        //shunt over operators
        while(ops.peek() != LEFT_BRACKET)
            if(ops.peek() == RIGHT_BRACKET)
                handleRightBracket(out, ops);
            else
                out.add(ops.pop());
        //add CALL operator
        out.add(CALL);
        //get rid of [
        ops.pop();
    }
    
    /**
     * Folds constants in the specified function. Constants include 0-arity functions
     * and operations on numeric constants. Runs in O(nk) where n is the number of operations
     * and k is the average arity.
     * Examples:
     *  2 pi * t * :sin_1 -> 6.28... t * sin
     *  t :log_1 2 :log_1 / -> t :log_1 0.693... /
     *  mc:twopi_0 t * -> 6.28... t *
     */
    private static Deque<Operator> foldConstants(Deque<Operator> func) {
        
        //System.out.println(func);
        Deque<Operator> res = new ArrayDeque<>();
        //set up a stack
        Stack s = new Stack();
        //iterate through func, stopping at an operator
        for(Operator op : func) {
            
            if(!(op instanceof Parameter)
                && s.size() >= op.arity()) {
                //we have enough args to fold
                op.eval(null, s);
            } else {
                //empty the stack into res
                Operator[] tmp = new Operator[s.size()];
                for(int i = s.size() - 1; i >= 0; i--)
                    tmp[i] = s.popOp();
                for(Operator d : tmp) {
                    if(d.arity() != 0)
                        res.add(new FunctionValue(d));
                    res.add(d);
                }
                res.add(op);
            }
        }
        //add the end result
        Operator[] tmp = new Operator[s.size()];
        for(int i = s.size() - 1; i >= 0; i--)
            tmp[i] = s.popOp();
        for(Operator d : tmp)
            res.add(d);
        System.out.println("Folded: " + res);
        return res;
    }
    
    public static boolean debug = false;
    public static String cp = ".";
    
    private static void readFile(String filename) {
        
        try {
            Scanner in = new Scanner(new File(cp + "/" + filename));
            while(in.hasNextLine())
                doRepl(in);
            in.close();
        } catch(IOException e) {
            System.err.println("Could not open file: " + cp + "/" + filename);
        } finally {
            //reset domain
            domain = "global";
            importedFunctionNames.clear();
        }
    }
    
    public static String doRepl(Scanner in) {
        
        try {
            return repl(in);
        } catch(Throwable e) {
            if(debug)
                e.printStackTrace();
            return e.getMessage();
        }
    }
    
    private static String domain = "global";
    
    private static Map<String, String> importedFunctionNames = new HashMap<>();
    
    private static String repl(Scanner in) {
        
        List<Token> tokens = getInput(in);
        if(tokens.isEmpty())
            return "";
        else if(tokens.get(0).value().equals("@")) {
            runCommand(tokens);
            return "";
        } else {
            //System.out.println("Tokens: " + tokens);
            List<Operator> ls =
                convert(Operator.PREFIX, domain, "", Collections.emptyList(), 0, tokens.listIterator());
            Queue<Operator> q = parse(ls, 0);
            StackProcedure proc = new StackProcedure(domain, 0, 0, Operator.PREFIX, q, new BitSet());
            proc.eval(null, THE_STACK);
            return THE_STACK.popOp().toString();
        }
    }
    
    private static final java.util.Scanner in = new java.util.Scanner(System.in);
    
    private static List<Token> getInput(Scanner in) {
        
        List<Token> res = new ArrayList<>();
        int bracket = 0;
        int paren = 0;
        do {
            System.out.print("> ");
            List<Token> tmp = Tokens.split(in.nextLine());
            for(Token t : tmp) {
                if(t.type() == Token.LITERAL)
                    switch(t.value().charAt(0)) {
                        case '[':
                            bracket++;
                            break;
                        case ']':
                            bracket--;
                            break;
                        case '(':
                            paren++;
                            break;
                        case ')':
                            paren--;
                            break;
                    }
            }
            if(bracket < 0 || paren < 0) {
                System.err.println("Unbalanced brackets in input");
                return Collections.emptyList();
            }
            res.addAll(tmp);
        } while((bracket + paren) > 0);
        return res;
    }
    
    private static void runCommand(List<Token> input) {
        
        assert input.get(0).value().equals("@") : input;
        if(input.size() < 2)
            System.err.println("No command entered");
        switch(input.get(1).value()) {
            
            case "quit":
                System.exit(0);
                break;
            case "delete":
                String prefix;
                String func;
                if(input.size() == 3) {
                    prefix = "all";
                    func = input.get(2).value();
                } else if(input.size() >= 4) {
                    prefix = input.get(2).value();
                    func = input.get(3).value();
                } else {
                    System.err.println("Usage: @delete [all|prefix|infix] <function>");
                    return;
                }
                if(func.startsWith(":"))
                    System.err.println("Cannot delete global function");
                else {
                    boolean deleted = true;
                    switch(prefix) {
                        
                        case "all":
                            deleted = functions.remove(func) != null | operators.remove(func) != null;
                            break;
                        case "prefix":
                            deleted = functions.remove(func) != null;
                            break;
                        case "infix":
                            deleted = operators.remove(func) != null;
                            break;
                        default:
                            System.err.println("Mode not recognized");
                            return;
                    }
                    if(deleted)
                        System.out.println("Deleted function");
                    else
                        System.err.println("Function not found");
                }
                break;
            case "list":
                System.out.println("Prefix functions:");
                for(String name : functions.keySet())
                    System.out.print(name + " ");
                System.out.println();
                System.out.println("Infix functions:");
                for(String name : operators.keySet())
                    System.out.print(name + " ");
                System.out.println();
                break;
            case "namespace":
                if(input.size() < 3)
                    System.out.println("Current namespace: " + domain);
                else {
                    if(input.get(2).type() == Token.IDENT) {
                        domain = input.get(2).value();
                        importedFunctionNames.clear();
                        System.out.println("Changed namespace to " + domain);
                    } else
                        System.err.println("Not a valid namespace");
                }
                break;
            case "forward":
                List<Token> def = input.subList(2, input.size());
                List<Token> args = splitNameAndArgs(def.listIterator());
                String name = args.remove(0).value();
                addFuncDecl(domain, name, args, 0);
                break;
            case "resolve":
                if(input.size() > 2) {
                    Token[] tokens = new Token[input.size() - 2];
                    for(int i = 2; i < input.size(); i++) {
                        tokens[i - 2] = input.get(i);
                        if(tokens[i - 2].type() != Token.STRING) {
                            System.err.println("Invalid file");
                            return;
                        }
                    }
                    for(Token t : tokens) {
                        String s = t.value();
                        readFile(s.substring(1, s.length() - 1));
                    }
                }
                for(Operator op : functions.values())
                    op.resolve();
                for(Operator op : operators.values())
                    op.resolve();
                System.out.println("Successfully resolved dependencies");
                break;
            case "import":
                name = input.get(2).value();
                if(input.get(2).type() != Token.STRING)
                    System.err.println("Invalid file");
                else {
                    name = name.substring(1, name.length() - 1);
                    readFile(name);
                }
                break;
            case "using":
                if(input.size() == 3) {
                    //one arg
                    Token arg = input.get(2);
                    switch(arg.type()) {
                    case Token.IDENT:
                    case Token.SYMBOL:
                        //using namespace
                        String namespace = arg.value();
                        for(String key : functions.keySet()) {
                            String[] split = key.split(":", 2);
                            assert split.length == 2 : split.length;
                            if(split[0].equals(namespace))
                                importedFunctionNames.put(split[1], key);
                        }
                        for(String key : operators.keySet()) {
                            String[] split = key.split(":", 2);
                            assert split.length == 2 : split.length;
                            if(split[0].equals(namespace))
                                importedFunctionNames.put(split[1], key);
                        }
                        break;
                    case Token.QUAL_IDENT:
                    case Token.QUAL_SYMBOL:
                        //using element
                        String qual = arg.value();
                        String fname = qual.split(":", 2)[1];
                        importedFunctionNames.put(fname, qual);
                        break;
                    default:
                        System.err.println("Invalid arguments");
                        return;
                    }
                    System.out.println("Using " + arg.value());
                } else {
                    System.err.println("Invalid arguments");
                }
                break;
            default:
                System.err.println("No command exists");
                break;
        }
    }
    
    public static class ExprTree {
    
        private Node root;
        private int params;
        
        public ExprTree(Collection<Operator> operations, int numParams) {
            
            assert !operations.isEmpty() : "operations empty";
            params = numParams;
            Deque<Node> tmpNodes = new ArrayDeque<>();
            for(Operator op : operations) {
                
                Node node = new Node();
                node.value = op;
                if(op.arity() == 0)
                    tmpNodes.push(node);
                else if(op.arity() == -1) {
                    Deque<Node> deq = new ArrayDeque<>();
                    while(tmpNodes.peek().value != BEGIN_ARGS)
                        deq.push(tmpNodes.pop());
                    tmpNodes.pop();
                    node.children = new Node[deq.size() + 1];
                    for(int i = 0; i < node.children.length - 1; i++)
                        node.children[i] = deq.pop();
                    Node len = new Node();
                    len.value = Constant.of(node.children.length - 1);
                    node.children[node.children.length - 1] = len;
                    tmpNodes.push(node);
                } else {
                    assert op.arity() > 0 : op.arity() + " " + op;
                    node.children = new Node[op.arity()];
                    for(int i = op.arity() - 1; i >= 0; i--) {
                        if(tmpNodes.isEmpty())
                            throw new RuntimeException("Invalid stack");
                        node.children[i] = tmpNodes.pop();
                    }
                    tmpNodes.push(node);
                }
            }
            if(tmpNodes.size() != 1)
                throw new RuntimeException("Invalid stack");
            root = tmpNodes.pop();
        }
        
        public Deque<Operator> flattenPostOrder() {
            
            Deque<Operator> ops = new ArrayDeque<>();
            fillOps(ops, root);
            return ops;
        }
        
        private void fillOps(Queue<Operator> ops, Node node) {
            
            assert node != null : "Node is null";
            if(node.children != null) {
                for(int i = 0; i < node.children.length; i++) {
                    Node n = node.children[i];
                    //if param is lazy, wrap in a (unevaluated) zero-arity capturing function
                    if(node.value.isByNameParam(i)) {
                        if(n.value.arity() != 0) {
                            Queue<Operator> tmp = new ArrayDeque<>();
                            fillOps(tmp, n);
                            Operator byName = new StackProcedure(
                                node.value + "$byName$" + inc++, params, params, Operator.PREFIX, tmp, new BitSet());
                            //prevent evaluation when captured
                            byName = new FunctionValue(byName);
                            ops.add(byName);
                            if(params > 0) {
                                for(int j = 0; j < params; j++)
                                    ops.add(new Parameter(j, true));
                                ops.add(Constant.of(-params + 1));
                                ops.add(CAPTURE);
                            }
                        } else
                            ops.add(n.value.asValue());
                    } else
                        fillOps(ops, n);
                }
            }
            ops.add(node.value);
        }
        
        public void print(PrintStream out) {
            
            printImpl(out, root, 0);
        }
        
        private void printImpl(PrintStream out, Node node, int level) {
            
            assert node != null : "node null";
            for(int i = 0; i < level; i++)
                out.print("  ");
            out.println(node.value);
            if(node.children != null) {
                for(Node n : node.children) {
                    
                    printImpl(out, n, level + 1);
                }
            }
        }
        
        private static class Node {
            
            Operator value;
            Node[] children;
        }
    }
    
    private static final Stack THE_STACK = new Stack();
}