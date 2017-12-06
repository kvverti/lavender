package kvverti.lavender.operators;

import kvverti.lavender.Stack;

/**
 * An operator that pushes a parameter.
 */
public class Parameter extends Operator {
    
    private final int idx;
    private final boolean forward;
    
    public Parameter(int idx) {
        
        this(idx, false);
    }
    
    public Parameter(int idx, boolean fwd) {
        
        super("param " + idx, 0, 1, Operator.NA);
        this.idx = idx;
        this.forward = fwd;
    }
    
    @Override
    public void eval(Operator[] params, Stack stack) {
        
        Operator op = params[idx];
        if(op.arity() == 0 && !forward)
            op.eval(params, stack);
        else
            stack.push(op);
    }
    
    @Override
    public Operator asValue() {
        
        return forward ? this : new Parameter(idx, true);
    }
}