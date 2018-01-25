package kvverti.lavender.operators;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

import kvverti.lavender.Stack;
import kvverti.lavender.Lavender;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.collectingAndThen;

public class FileObj extends Operator implements Closeable {
    
    private final Map<Operator, Operator> methods = new HashMap<>();
    private File file;
    private OutputStream out;
    private Scanner in;
    private String delim = " ";
    private int state;
    
    private static final int
        UNINIT = 0,
        INPUT = 1,
        OUTPUT = 2,
        ERRORED = 3;
    
    public FileObj(boolean sysin) {
        
        super(":$FILE", 0, 1, Operator.NA);
        file = null;
        if(sysin) {
            out = null;
            in = new Scanner(System.in);
            state = INPUT;
        } else {
            out = System.out;
            in = null;
            state = OUTPUT;
        }
    }
    
    /** Creates a file with the given name */
    public FileObj(String name) {
        
        super(":FILE[" + name + "]", 1, 1, Operator.NA);
        file = new File(name);
    }
    
    public boolean openRead() {
        
        if(state == UNINIT) {
            try {
                InputStream tmp = new FileInputStream(file);
                in = new Scanner(tmp);
                state = INPUT;
                return true;
            } catch(IOException e) {
                state = ERRORED;
                return false;
            }
        }
        return state == INPUT;
    }
    
    public StringOp nextToken() {
        
        openRead();
        String str = in.next();
        return StringOp.ofLiteral(str);
    }
    
    @Override
    public void eval(Operator[] p, Stack stack) {
        
        stack.push(this);
    }
    
    @Override
    public void close() {
        
        //built in stream
        if(file == null)
            return;
        try {
            if(out != null)
                out.close();
        } catch(IOException e) { }
        if(in != null)
            in.close();
        System.out.println("close");
    }
    
    private void fillInMethodTable() {
        
        Lavender env = Lavender.getRuntime();
        methods.put(env.getInfixFunction("io:lines"),
            new Operator(":$FILE$lines", 0, 1, Operator.PREFIX) {
           
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                if(state == UNINIT) {
                    try {
                        Operator[] lines = Files.lines(file.toPath())
                            .collect(toList())
                            .stream()
                            .map(StringOp::ofLiteral)
                            .collect(collectingAndThen(toList(), ls -> ls.toArray(new Operator[0])));
                        stack.push(Vector.of(lines));
                    } catch(IOException e) {
                        stack.push(Builtin.NAN);
                    }
                } else
                    stack.push(Builtin.NAN);
            }
        });
        methods.put(env.getInfixFunction("io:next"),
            new Operator(":$FILE$next", 1, 1, Operator.PREFIX) {
                
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                Operator func = stack.popOp();
                StringOp tok = nextToken();
                stack.push(tok);
                func.eval(d, stack);
                stack.popOp();
                stack.push(this);
            }
        });
        methods.put(env.getInfixFunction("algorithm:map"),
            new Operator(":$FILE$map", 1, 1, Operator.PREFIX) {
           
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                Operator func = stack.popOp();
                if(openRead()) {
                    List<Operator> ls = new ArrayList<>();
                    while(in.hasNext()) {
                        stack.push(StringOp.ofLiteral(in.next()));
                        func.eval(d, stack);
                        ls.add(stack.popOp());
                    }
                    stack.push(Vector.of(ls.toArray(new Operator[0])));
                } else {
                    stack.push(Vector.of(new Operator[0]));
                }
            }
        });
    }
}