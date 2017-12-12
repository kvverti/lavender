package kvverti.lavender.runtime;

import java.util.BitSet;
import java.util.List;

import kvverti.lavender.Lavender;
import kvverti.lavender.Stack;
import kvverti.lavender.operators.Operator;

/** Marker class for forward declared functions. Will be replaced with real function. */
public class FunctionDecl extends Operator {
    
    private final int captured;
    private final BitSet byNameParams;
    
    private FunctionDecl(String name, int arity, BitSet byName, int cap, int fix) {
        
        super(name, arity, 1, fix);
        captured = cap;
        byNameParams = byName;
    }
    
    public static Operator add(String name, List<Token> params, int captured, int fix) {
        
        BitSet byName = new BitSet();
        for(int i = 0; i < params.size(); i++) {
            byName.set(i, params.get(i).value().startsWith("=>$"));
        }
        Operator op = new FunctionDecl(name, params.size(), byName, captured, fix);
        //don't override possible definitions
        Lavender lav = Lavender.getRuntime();
        if(fix == Operator.PREFIX)
            lav.addPrefixFunction(name, op);
        else
            lav.addInfixFunction(name, op);
        return op;
    }
    
    @Override
    public Operator resolve() {
        
        Operator res;
        Lavender env = Lavender.getRuntime();
        if(fixing() == Operator.PREFIX)
            res = env.getPrefixFunction(toString());
        else
            res = env.getInfixFunction(toString());
        assert res != null : this;
        if(res == this)
            throw new RuntimeException("Linkage error: " + toString() + " is not defined");
        return res;
    }
    
    @Override
    public void eval(Operator[] params, Stack stack) {
        
        throw new RuntimeException("Linkage error: " + toString() + " is not defined");
    }
    
    @Override
    public int captured() {
        
        return captured;
    }
    
    @Override
    public boolean isByNameParam(int i) {
        
        return byNameParams.get(i);
    }
}