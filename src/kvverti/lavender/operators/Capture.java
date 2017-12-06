package kvverti.lavender.operators;

import java.util.Arrays;

import kvverti.lavender.Stack;

/**
 * Represents a function that has captured parameters.
 */
public class Capture extends Operator {
    
    private final Operator wrapped;
    private final Operator[] capturedParams;
    
    public Capture(Operator f, Operator[] cap) {
        
        super(f.toString() + Arrays.toString(cap), f.arity() - cap.length, f.returns(), f.fixing());
        wrapped = f;
        capturedParams = cap;
    }
    
    @Override
    public void eval(Operator[] p, Stack stack) {
        
        for(Operator op : capturedParams)
            stack.push(op);
        wrapped.eval(p, stack);
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if(obj instanceof Capture) {
            
            Capture cap = (Capture) obj;
            return wrapped.equals(cap.wrapped) && Arrays.equals(capturedParams, cap.capturedParams);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        
        return wrapped.hashCode() + Arrays.hashCode(capturedParams);
    }
    
    @Override
    public boolean isByNameParam(int i) {
        
        return wrapped.isByNameParam(i);
    }
}