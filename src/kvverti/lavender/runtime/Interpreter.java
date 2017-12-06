package kvverti.lavender.runtime;

import java.io.InputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.List;

import kvverti.lavender.Lavender;
import kvverti.lavender.Stack;
import kvverti.lavender.operators.Operator;

/**
 * The actual interpreter for Lavender. Instances of
 * this class evaluate expressions written in the Lavender
 * language.
 */
public class Interpreter {
    
    private final Lavender environment;
    private final Stack theStack;
    
    public Interpreter(Lavender env) {
        
        environment = env;
        theStack = new Stack();
    }
    
    public Operator evaluate(Operator op) {
        
        op.eval(null, theStack);
        return theStack.popOp();
    }
    
    /**
     * Parses and evaluates all contents of the input stream.
     */
    public void parseInput(InputStream in) {
        
        throw new AbstractMethodError();
    }
    
    /**
     * Reads one expression from the input stream
     * and returns a string representation of the result
     * of its evaluation.
     */
    public String repl(InputStream in) {
        
        Scanner sc = new Scanner(in);
        return kvverti.lavender.Expr.doRepl(sc);
    }
    
    private void getLine(Scanner in, int[] nesting, List<? super Token> res) {
        
        List<Token> tmp = Tokens.split(in.nextLine());
        for(Token t : tmp) {
            if(t.type() == Token.LITERAL) {
                switch(t.value().charAt(0)) {
                    case '[':
                        nesting[1]++;
                        break;
                    case ']':
                        nesting[1]--;
                        break;
                    case '(':
                        nesting[0]++;
                        break;
                    case ')':
                        nesting[0]--;
                        break;
                }
            }
        }
        res.addAll(tmp);
    }
}