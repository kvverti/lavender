package kvverti.lavender.ljri;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import kvverti.lavender.Lavender;
import kvverti.lavender.Stack;
import kvverti.lavender.operators.Operator;
import kvverti.lavender.operators.Capture;
import kvverti.lavender.operators.Constant;
import kvverti.lavender.operators.StringOp;
import kvverti.lavender.operators.Builtin;
import kvverti.lavender.operators.Logic;

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
        
        if(arity(func) != args.length)
            return Builtin.NAN;
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
        return new Operator(name.value(), p.length - 1, 1, Operator.PREFIX) {
            
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
    
    private static class JavaWrap extends Operator {
        
        final Object obj;
     
        JavaWrap(Object obj) {
            
            super(obj.toString(), 0, 1, Operator.NA);
            this.obj = obj;
        }
        
        @Override
        public void eval(Operator[] d, Stack stack) {
            
            stack.push(this);
        }
    }
    
    /**
     * Wraps the given Java object in an LFunction. This allows Java objects to be passed into
     * Lavender functions.
     */
    public LFunction wrap(Object obj) {
        
        return new JavaWrap(obj);
    }
    
    /**
     * Unwraps the given LFunction and returns the wrapped Java object. If this function does not
     * wrap a Java object, or the object is not of the correct type, null is returned.
     */
    public <T> T unwrap(LFunction func, Class<T> cls) {
        
        if(func instanceof JavaWrap) {
            Object obj = ((JavaWrap) func).obj;
            if(cls.isInstance(obj))
                return cls.cast(obj);
        }
        return null;
    }
    
    /**
     * Returns the undefined value. This value is the basic error value.
     */
    public LFunction undefined() {
        
        return Builtin.NAN;
    }
    
    /**
     * Converts the LFunction to a number. If the function does not represent
     * a number, the floating-point NaN value is returned.
     */
    public double asNumber(LFunction f) {
        
        return Constant.value((Operator) f);
    }
    
    /**
     * Converts the given number to an LFunction that can be used by
     * the Lavender runtime.
     */
    public LFunction fromNumber(double d) {
        
        return Constant.of(d);
    }
    
    /**
     * Converts the LFunction to a string. If the function does not represent
     * a string, null is returned.
     */
    public String asString(LFunction f) {
        
        if(f instanceof StringOp)
            return f.toString();
        return null;
    }
    
    /**
     * Returns an LFunction representing the given String.
     */
    public LFunction fromString(String s) {
        
        return StringOp.ofLiteral(s);
    }
    
    /**
     * Converts the given LFunction to a boolean value. The numbers 0.0 and the
     * NaN value are falsey, the rest are truthy.
     */
    public boolean asBool(LFunction f) {
        
        double d = asNumber(f);
        return d == d && d != 0.0;
    }
    
    /**
     * Returns an LFunction representing the given boolean.
     */
    public LFunction fromBool(boolean b) {
        
        return b ? Constant.of(1) : Constant.of(0);
    }
}