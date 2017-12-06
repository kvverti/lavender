package kvverti.lavender;

import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.BiFunction;

import kvverti.lavender.operators.Operator;
import kvverti.lavender.runtime.Interpreter;

/**
 * The execution environment for the Lavender runtime.
 * Instances of this class store defined functions and
 * environment variables (command line arguments).
 */
public class Lavender extends Thread {
    
    private final boolean debug;
    private final String filepath;
    private final Map<String, Operator> functions;
    private final Map<String, Operator> operators;
    
    public static class Args {
        
        private boolean debug = false;
        private String filepath = ".";
        
        public Args() { }
        
        public boolean debug() { return debug; }
        
        public void setDebug(boolean db) { debug = db; }
        
        public String filepath() { return filepath; }
        
        public void setFilepath(String fp) { filepath = fp; }
    }
    
    public static Lavender getRuntime() {
        
        Thread t = Thread.currentThread();
        return t instanceof Lavender ? (Lavender) t : null;
    }
    
    public Lavender(Args args) {
        
        super("LAVENDER-RUNTIME");
        debug = args.debug();
        filepath = args.filepath();
        functions = new HashMap<>();
        operators = new HashMap<>();
    }
    
    public boolean debug() { return debug; }
    
    public String filepath() { return filepath; }
    
    @Deprecated
    public Map<String, Operator> functions() { return functions; }
    
    @Deprecated
    public Map<String, Operator> operators() { return operators; }
    
    public void loadFile(String file) {
        
        throw new AbstractMethodError();
    }
    
    public void resolveAll() {
     
        BiFunction<String, Operator, Operator> resolve = (k, v) -> v.resolve();
        functions.replaceAll(resolve);
        operators.replaceAll(resolve);
    }
    
    public Map<String, Operator> getFunctionsInNamespace(String namespace) {
        
        throw new AbstractMethodError();
    }
    
    @Override
    public void run() {
        
        Interpreter intr = new Interpreter(this);
        Expr.debug = debug;
        Expr.cp = filepath;
        while(true) {
            String res = intr.repl(System.in);
            if(!res.isEmpty())
                System.out.println(res);
        }
    }
}