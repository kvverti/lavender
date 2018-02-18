package kvverti.lavender;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.BiFunction;

import kvverti.lavender.operators.Operator;
import kvverti.lavender.operators.Logic;
import kvverti.lavender.runtime.Interpreter;
import kvverti.lavender.runtime.FunctionDecl;

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
    private final Interpreter intr;
    
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
        intr = new Interpreter(this);
        //builtin infix functions
        operators.put(":**", Logic.POWER);
        operators.put(":*", Logic.TIMES);
        operators.put(":/", Logic.DIVIDE);
        operators.put(":%", Logic.REMAINDER);
        operators.put(":+", Logic.PLUS);
        operators.put(":-", Logic.MINUS);
        // operators.put(":&", Logic.AND);
        // operators.put(":|", Logic.OR);
        // operators.put(":^", Logic.XOR);
        operators.put(":=", Logic.EQ);
        operators.put(":!=", Logic.NE);
        operators.put(":>", Logic.GT);
        operators.put(":<", Logic.LT);
        operators.put(":>=", Logic.GE);
        operators.put(":<=", Logic.LE);
        //builtin prefix functions and values
        functions.put(":abs", Logic.ABS);
        functions.put(":sin", Logic.SIN);
        functions.put(":cos", Logic.COS);
        functions.put(":pi", Logic.PI);
        functions.put(":e", Logic.E);
        functions.put(":log", Logic.LOG);
        functions.put(":ceil", Logic.CEIL);
        functions.put(":floor", Logic.FLOOR);
        functions.put(":int", Logic.INT);
        functions.put(":max", Logic.MAX);
        functions.put(":min", Logic.MIN);
        functions.put(":str", Logic.STR);
        functions.put(":num", Logic.NUM);
        functions.put(":+", Logic.UPLUS);
        functions.put(":-", Logic.UMINUS);
        // functions.put(":!", Logic.NOT);
        functions.put(":strhash", Logic.STR_HASH);
    }
    
    public boolean debug() { return debug; }
    
    public String filepath() { return filepath; }
    
    public void loadFile(String file) {
        
        File f = new File(filepath + "/" + file);
        try(InputStream input = new FileInputStream(f)) {
            intr.parseInput(input);
        } catch(IOException e) {
            System.err.println("Could not open file: " + file);
        }
    }
    
    public void link() {
     
        BiFunction<String, Operator, Operator> resolve = (k, v) -> v.resolve();
        functions.replaceAll(resolve);
        operators.replaceAll(resolve);
    }
    
    public List<String> getNamesInNamespace(String namespace) {
        
        List<String> res = new ArrayList<>();
        for(String name : functions.keySet()) {
            String[] split = name.split(":", 2);
            assert split.length == 2 : split.length;
            if(split[0].equals(namespace))
                res.add(split[1]);
        }
        for(String name : operators.keySet()) {
            String[] split = name.split(":", 2);
            assert split.length == 2 : split.length;
            if(split[0].equals(namespace))
                res.add(split[1]);
        }
        return res;
    }
    
    public Operator getPrefixFunction(String name) {
        
        return functions.get(name);
    }
    
    public Operator getInfixFunction(String name) {
        
        return operators.get(name);
    }
    
    public boolean addPrefixFunction(String name, Operator function) {
        
        Operator op = functions.get(name);
        if(op == null || op instanceof FunctionDecl) {
            functions.put(name, function);
            return true;
        }
        return false;
    }
    
    public boolean addInfixFunction(String name, Operator function) {
        
        Operator op = operators.get(name);
        if(op == null || op instanceof FunctionDecl) {
            operators.put(name, function);
            return true;
        }
        return false;
    }
    
    public boolean unbind(String name) {
        
        return operators.remove(name) != null || functions.remove(name) != null;
    }
    
    public void dump() {
     
        System.out.println("Lavender runtime information:");
        System.out.print("Debug mode: ");
        System.out.println(debug);
        System.out.print("Library filepath: ");
        System.out.println(filepath);
        System.out.println("Declared functions:");
        for(String s : functions.keySet()) {
            System.out.print(s);
            System.out.println(" (prefix)");
        }
        for(String s : operators.keySet()) {
            System.out.print(s);
            System.out.println(" (infix)");
        }
        intr.dump();
    }
    
    @Override
    public void run() {
        
        while(true) {
            String res = intr.repl(System.in);
            if(!res.isEmpty())
                System.out.println(res);
        }
    }
}