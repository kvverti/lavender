package kvverti.lavender.runtime;

import java.util.*;
import java.util.function.Function;

import kvverti.lavender.Lavender;
import kvverti.lavender.Stack;
import kvverti.lavender.operators.*;

class Parser {
    
    //increment for splitNameAndArgs
    private static int inc = 0;
    static final Operator BEGIN_ARGS = new Operator("PIPE", 0, 0, Operator.NA){};
    
    private Lavender lav;
    private Map<String, String> importedFunctionNames;
    private String domain;
    
    public Parser() { }
    
    public Operator parseExpr(List<Token> tokens, Lavender env, Map<String, String> imports, String domain) {
        
        this.lav = env;
        this.importedFunctionNames = imports;
        this.domain = domain;
        List<Operator> ls =
            convert(Operator.PREFIX, domain, "", Collections.emptyList(), 0, tokens.listIterator());
        Queue<Operator> q = parse(ls, 0);
        return new StackProcedure(domain, 0, 0, Operator.PREFIX, q, new BitSet());
    }
    
    public void declareFunction(List<Token> tokens, Lavender env, String domain) {
        
        this.lav = env;
        this.domain = domain;
        List<Token> args = splitNameAndArgs(tokens.listIterator());
        String name = args.remove(0).value();
        addFuncDecl(domain, name, args, 0);
    }
    
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
    private List<Token> splitNameAndArgs(ListIterator<Token> itr) {
        
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
    private List<List<Token>> splitBodies(ListIterator<Token> itr) {
        
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
    
    private Operator wrap(boolean wrap, Operator op) {
        
        if(wrap && op.arity() > 0)
            return new FunctionValue(op);
        return op;
    }
    
    private Operator getUnqualified(String domain, String name, boolean prefix) {
        
        //check namespaces
        Lavender lav = Lavender.getRuntime();
        Function<String, Operator> f = prefix ? lav::getPrefixFunction : lav::getInfixFunction;
        Operator o = f.apply(domain + ":" + name);
        int idx = domain.lastIndexOf("$");
        while(o == null && idx >= 0) {
            //go through nested namespaces
            domain = domain.substring(0, idx);
            o = f.apply(domain + ":" + name);
            idx = domain.lastIndexOf("$");
        }
        if(o == null) {
            //check imports
            o = f.apply(importedFunctionNames.get(name));
            if(o == null) {
                //check global namespaces
                o = f.apply("global:" + name);
                if(o == null) {
                    o = f.apply(":" + name);
                }
            }
        }
        return o;
    }
    
    private Operator addFuncDecl(String domain, String name, List<Token> args, int captured) {
        
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
    
    private Operator parseFunction(String domain, List<Token> capture, ListIterator<Token> expr) {
        
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
            Operator op = fix == Operator.PREFIX ? lav.getPrefixFunction(domainAndName) : lav.getInfixFunction(domainAndName);
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
            lav.addPrefixFunction(proc.toString(), proc);
        else
            lav.addInfixFunction(proc.toString(), proc);
        return proc;
    }
    
    /**
     * Splits an expression into its component operations. These expressions
     * are returned in infix order, and may be sorted by a call to parse().
     */
    private List<Operator> convert(int fix,
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
        Lavender lav = Lavender.getRuntime();
        Operator THIS = fix == Operator.PREFIX ? lav.getPrefixFunction(domain + ":" + name) : lav.getInfixFunction(domain + ":" + name);
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
                            res.add(Builtin.CAPTURE);
                            res.add(Builtin.LEFT_PAREN);
                        }
                        //add new function
                        res.add(wrap(true, def));
                        if(params.size() > 0) {
                            //add params for capture
                            for(int i = 0; i < params.size(); i++) {
                                res.add(new Parameter(i));
                            }
                            res.add(Builtin.RIGHT_PAREN);
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
                            res.add(Builtin.LEFT_BRACKET);
                            for(int i = params.size() - numCapture; i < params.size(); i++)
                                res.add(new Parameter(i));
                            //force wrapping
                            //res.add(new This(params.size(), fix, true));
                            res.add(THIS.asValue());
                            res.add(Builtin.RIGHT_BRACKET);
                        } else {
                            //create a capture instance to return
                            res.add(Builtin.CAPTURE);
                            res.add(Builtin.LEFT_PAREN);
                            res.add(THIS.asValue());
                            for(int i = params.size() - numCapture; i < params.size(); i++)
                                res.add(new Parameter(i));
                            res.add(Builtin.RIGHT_PAREN);
                        }
                    } else
                        res.add(wrap(wrap, THIS));
                    operand = (operand ? params.size() > 0 : params.size() > 1) && !wrap;
                } else {
                    //check namespaces
                    Operator o = getUnqualified(domain, value, operand);
                    if(o == null)
                        throw new RuntimeException("Unqualified name not found: " + value);
                    if(o.captured() == 0)
                        res.add(wrap(wrap, o));
                    else {
                        //add implicit captured environment
                        //see above code for self-recursive version
                        if(!wrap) {
                            res.add(Builtin.LEFT_BRACKET);
                            for(int i = params.size() - o.captured(); i < params.size(); i++)
                                res.add(new Parameter(i));
                            res.add(wrap(true, o));
                            res.add(Builtin.RIGHT_BRACKET);
                        } else {
                            res.add(Builtin.CAPTURE);
                            res.add(Builtin.LEFT_PAREN);
                            res.add(wrap(true, o));
                            for(int i = params.size() - o.captured(); i < params.size(); i++)
                                res.add(new Parameter(i));
                            res.add(Builtin.RIGHT_PAREN);
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
                    Operator o = operand ? lav.getPrefixFunction(value) : lav.getInfixFunction(value);
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
                    res.add(Builtin.LEFT_PAREN);
                } else if(!operand && value.equals(")")) {
                    res.add(Builtin.RIGHT_PAREN);
                } else if(operand && value.equals("[")) {
                    res.add(Builtin.LEFT_BRACKET);
                } else if(!operand && value.equals("]")) {
                    res.add(Builtin.RIGHT_BRACKET);
                    operand = true;
                } else if(!operand && value.equals(",")) {
                    res.add(Builtin.COMMA);
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
    
    /**
     * An implementation of Dijkstra's shunting-yard algorithm, modified slightly to
     * support the comma separator. Yes, there are better parses, but apparently
     * this is "fast."
     */
    private Queue<Operator> parse(List<Operator> in, int numParams) {
        
        Deque<Operator> out = new ArrayDeque<>();
        Deque<Operator> ops = new ArrayDeque<>();
        for(Operator op : in) {
            
            // System.out.println("op: " + op);
            if(op.arity() == 0)
                //we'll have to shunt it over anyway
                out.add(op);
            else if(op == Builtin.LEFT_PAREN)
                //just push it
                ops.push(op);
            else if(op == Builtin.LEFT_BRACKET) {
                //push token onto operator stack AND output stack
                out.add(op);
                ops.push(op);
            } else if(op == Builtin.RIGHT_BRACKET) {
                //pop output onto operator stack until [
                //then push ] onto operator to mark
                while(out.peekLast() != Builtin.LEFT_BRACKET)
                    ops.push(out.removeLast());
                out.removeLast();
                out.add(BEGIN_ARGS);
                ops.push(op);
            } else if(op == Builtin.RIGHT_PAREN) {
                //shunt over all the operators until we hit left paren
                //we don't check bounds, correct syntax will not underflow
                while(ops.peek() != Builtin.LEFT_PAREN) {
                    //handle right bracket rules
                    if(ops.peek() == Builtin.RIGHT_BRACKET)
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
                    if(ops.peek() == Builtin.RIGHT_BRACKET)
                        handleRightBracket(out, ops);
                    else
                        out.add(ops.pop());
                }
                //..and push the operator on, unless it's comma
                if(op != Builtin.COMMA) {
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
            if(ops.peek() == Builtin.RIGHT_BRACKET)
                handleRightBracket(out, ops);
            else
                out.add(ops.pop());
        //elements of out are in postfix order
        if(lav.debug()) {
            System.out.println(out);
        }
        ExprTree tree = new ExprTree(out, numParams);
        out = tree.flattenPostOrder();
        if(lav.debug()) {
            tree.print(System.out);
            System.out.println(out);
        }
        //out = foldConstants(out);
        return out;
    }
    
    private void handleRightBracket(Queue<Operator> out, Deque<Operator> ops) {
        
        assert ops.peek() == Builtin.RIGHT_BRACKET : ops.peek();
        //pop ]
        ops.pop();
        //shunt over operators
        while(ops.peek() != Builtin.LEFT_BRACKET)
            if(ops.peek() == Builtin.RIGHT_BRACKET)
                handleRightBracket(out, ops);
            else
                out.add(ops.pop());
        //add Builtin.CALL operator
        out.add(Builtin.CALL);
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
    private Deque<Operator> foldConstants(Deque<Operator> func) {
        
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
}