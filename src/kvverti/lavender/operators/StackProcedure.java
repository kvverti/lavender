package kvverti.lavender.operators;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.BitSet;

import kvverti.lavender.Lavender;
import kvverti.lavender.Stack;

/**
 * A StackProcedure represents a list of operations on a stack structure.
 * It may also be used as a function operator.
 */
public class StackProcedure extends Operator {
    
    private final Operator[] params;
    private final Operator[] code;
    private final int captured;
    private final BitSet byNameParams;
    private boolean resolved = false;
    
    public StackProcedure(String name, int arity, int cap, int fix, Collection<Operator> ops, BitSet byNames) {
        
        super(name, arity, 1, fix);
        params = new Operator[arity];
        code = new Operator[ops.size()];
        captured = cap;
        byNameParams = byNames;
        ops.toArray(code);
    }
    
    public void eval(Operator[] unused, Stack stack) {
        
        boolean debug = Lavender.getRuntime().debug();
        //read params
        for(int i = params.length - 1; i >= 0; i--)
            params[i] = stack.popOp();
        //execute code
        if(debug)
            System.out.println(toString() + ">>>");
        for(Operator op : code) {
            if(debug)
                System.out.println("op: " + op);
            op.eval(params, stack);
            if(debug)
                System.out.println(stack);
        }
        if(debug)
            System.out.println(toString() + "<<<");
    }
    
    @Override
    public Operator resolve() {
        
        if(!resolved) {
            resolved = true;
            try {
                for(int i = 0; i < code.length; i++)
                    code[i] = code[i].resolve();
            } catch(RuntimeException e) {
                resolved = false;
                throw e;
            }
        }
        return this;
    }
    
    @Override
    public int captured() {
        
        return captured;
    }
    
    @Override
    public boolean isByNameParam(int i) {
        
        assert i >= 0 && i < arity() : i;
        return byNameParams.get(i);
    }
}