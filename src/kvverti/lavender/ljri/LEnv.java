package kvverti.lavender.ljri;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import kvverti.lavender.Lavender;
import kvverti.lavender.Stack;
import kvverti.lavender.operators.Operator;
import kvverti.lavender.operators.Capture;

public final class LEnv {
    
    /**
     * Gets the function with the given name. Names are formatted starting
     * with "u_" for prefix functions and "i_" for infix functions. The full
     * name of the function follows, including namespaces.
     */
    public LFunction getFunction(String name) {
        
        if(name.length() < 3)
            throw new IllegalArgumentException("Name too short");
        char prefix = name.charAt(0);
        name = name.substring(2);
        if(prefix == 'u') {
            Operator tmp = Lavender.getRuntime().getPrefixFunction(name);
            if(tmp == null)
                throw new IllegalArgumentException("No such function: " + name);
            return tmp;
        } else if(prefix == 'i') {
            Operator tmp = Lavender.getRuntime().getInfixFunction(name);
            if(tmp == null)
                throw new IllegalArgumentException("No such function: " + name);
            return tmp;
        } else
            throw new IllegalArgumentException("Invalid prefix");
    }
    
    /** Returns the arity of the given function. */
    public int arity(LFunction func) {
        
        return ((Operator) func).arity();
    }
    
    /** Applies the given function with the given arguments and returns the result. */
    public LFunction apply(LFunction func, LFunction... args) {
        
        Stack stack = new Stack();
        for(LFunction lf : args)
            stack.push((Operator) lf);
        ((Operator) func).eval(null, stack);
        return stack.popOp();
    }
    
    /**
     * Returns a function that calls the given method using the given receiver object.
     * The method's first parameter must be of type "LEnv" and the remaining parameters
     * must be of type "LFunction". Additionally, the method should be annotated with
     * the @LavenderFunction annotation, which will provide the function's name within the Lavender
     * runtime.
     */
    public LFunction makeFunction(Object cxt, Method m) {
        
        //preconditions
        Class<?>[] p = m.getParameterTypes();
        if(p.length == 0)
            throw new IllegalArgumentException("Invalid function length");
        for(int i = 0; i < p.length; i++) {
            
            if((i == 0 && p[i] != LEnv.class)
                || (i != 0 && p[i] != LFunction.class)) {
                throw new IllegalArgumentException("Invalid parameter type: " + p[i].getSimpleName());
            }
        }
        if(m.getReturnType() != LFunction.class)
            throw new IllegalArgumentException("Invalid return type: " + m.getReturnType().getSimpleName());
        LavenderFunction name = m.getAnnotation(LavenderFunction.class);
        if(name == null)
            throw new IllegalArgumentException("Not a lavender function");
        //end preconditions
        return new Operator(name.value(), p.length - 1, 1, Operator.NA) {
            
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                Object[] params = new Object[arity() + 1];
                for(int i = arity(); i > 0; i--)
                    params[i] = stack.popOp();
                params[0] = LEnv.this;
                Operator res;
                try { res = (Operator) m.invoke(cxt, params); }
                catch(IllegalAccessException|InvocationTargetException e) {
                    throw new LinkageError("Lavender function access: " + m, e);
                }
                stack.push(res);
            }
        };
    }
    
    /**
     * Returns an LFunction in which the last parameters of the given function
     * are bound to the given values.
     */
    public LFunction capture(LFunction func, LFunction... args) {
        
        return new Capture((Operator) func, Arrays.copyOf(args, args.length, Operator[].class));
    }
}