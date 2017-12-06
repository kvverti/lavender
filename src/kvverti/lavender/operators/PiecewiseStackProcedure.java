package kvverti.lavender.operators;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.BitSet;

import kvverti.lavender.Lavender;
import kvverti.lavender.Stack;

/**
 * A piecewise function. Instances of this class contain a set of operations to execute
 * and conditions under which to execute them. Operations to execute may be any sequence of
 * operators, including other stack procedures.
 */
public class PiecewiseStackProcedure extends Operator {
    
    private final int numParams;
    //conditions.length == codes.length
    //conditons for each function
    private final Operator[][] conditions;
    //function to run on condition
    private final Operator[][] codes;
    private final int captured;
    private final BitSet byNameParams;
    private boolean resolved = false;
    
    public PiecewiseStackProcedure(String name,
        int arity,
        int cap,
        int fix,
        List<Queue<Operator>> conds,
        List<Queue<Operator>> ops,
        BitSet byNames) {
        
        super(name, arity, 1, fix);
        assert conds.size() == ops.size() : conds.size() + " " + ops.size();
        numParams = arity;
        int len = ops.size();
        conditions = new Operator[len][];
        codes = new Operator[len][];
        captured = cap;
        byNameParams = byNames;
        for(int i = 0; i < len; i++) {
            
            conditions[i] = new Operator[conds.get(i).size()];
            codes[i] = new Operator[ops.get(i).size()];
            int c = 0;
            for(Operator op : conds.get(i)) {
                conditions[i][c++] = op;
            }
            c = 0;
            for(Operator op : ops.get(i)) {
                codes[i][c++] = op;
            }
        }
    }
    
    @Override
    public void eval(Operator[] d, Stack stack) {
        
        boolean debug = Lavender.getRuntime().debug();
        Operator[] params = new Operator[numParams];
        //read params
        for(int i = params.length - 1; i >= 0; i--)
            params[i] = stack.popOp();
        int funcIdx = -1;
        //determine which function to use
        for(int i = 0; funcIdx < 0 && i < conditions.length; i++) {
            
            for(Operator op : conditions[i])
                op.eval(params, stack);
            if(Logic.bool(stack.pop()))
                funcIdx = i;
        }
        //run function if avail
        if(funcIdx < 0)
            stack.push(Builtin.NAN);
        else {
            if(debug)
                System.out.println(toString() + ">>>");
            for(Operator op : codes[funcIdx]) {
                if(debug)
                    System.out.println("op: " + op);
                op.eval(params, stack);
                if(debug)
                    System.out.println(stack);
            }
            if(debug)
                System.out.println(toString() + "<<<");
        }
    }
    
    @Override
    public Operator resolve() {
        
        if(!resolved) {
            resolved = true;
            try {
                for(Operator[] ops : conditions)
                    for(int i = 0; i < ops.length; i++)
                        ops[i] = ops[i].resolve();
                for(Operator[] ops : codes)
                    for(int i = 0; i < ops.length; i++)
                        ops[i] = ops[i].resolve();
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