package kvverti.lavender.operators;

import kvverti.lavender.Stack;

/**
 * An operator that pushes a constant value.
 */
public class Constant extends Operator {
    
    private final double data;
    
    private Constant(double data) {
        
        super(Double.toString(data), 0, 1, Operator.NA);
        this.data = data;
    }
    
    public static Constant of(double data) {
        
        return new Constant(data);
    }
    
    @Override
    public void eval(Operator[] unused, Stack stack) {
        
        stack.push(this);
    }
    
    public static double value(Operator op) {
        
        return op instanceof Constant ? ((Constant) op).data : Double.NaN;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        return obj instanceof Constant && data == ((Constant) obj).data;
    }
    
    @Override
    public int hashCode() {
        
        return Double.hashCode(data);
    }
}