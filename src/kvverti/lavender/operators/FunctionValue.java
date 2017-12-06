package kvverti.lavender.operators;

import kvverti.lavender.Stack;

/**
 * A general-prupose value wrapper for operators.
 */
public class FunctionValue extends Operator {
    
    private Operator data;
    
    public FunctionValue(Operator data) {
        
        super("val " + data.toString(), 0, 1, Operator.NA);
        this.data = data;
    }
    
    @Override
    public void eval(Operator[] d, Stack stack) {
        
        stack.push(data);
    }
    
    @Override
    public Operator resolve() {
        
        data = data.resolve();
        return this;
    }
}