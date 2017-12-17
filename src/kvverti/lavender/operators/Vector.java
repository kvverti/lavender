package kvverti.lavender.operators;

import java.util.Arrays;
import java.util.List;

import kvverti.lavender.Lavender;
import kvverti.lavender.Stack;

/**
 * An immutable, random-access container usable in Lavender.
 */
public class Vector extends Operator {
    
    public static Operator MK_VECTOR;
    private final Operator[] data;
    private final Lavender env;
    
    private Vector(Operator[] data) {
        
        super(":$VECTOR" + Arrays.toString(data), 1, 1, Operator.PREFIX);
        this.data = data;
        this.env = Lavender.getRuntime();
        if(env == null)
            throw new RuntimeException("Not cool :(");
        if(env.debug()) {
            for(Operator op : data)
                if(op == null)
                    throw new AssertionError("Null op");
        }
    }
    
    @Override
    public void eval(Operator[] params, Stack stack) {
        
        Operator op = stack.popOp();
        if(op.equals(env.getPrefixFunction("global:size")))
            stack.push(data.length);
        else {
            double i = Constant.value(op);
            if(i >= 0 && i < data.length)
                stack.push(data[(int) i]);
            else
                stack.push(Builtin.NAN);
        }
    }
    
    static {
        
        MK_VECTOR = new Operator("vector:fromList", 1, 1, Operator.PREFIX) {
          
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Lavender env = Lavender.getRuntime();
                Operator list = stack.popOp();
                //get size of list
                stack.push(list);
                env.getPrefixFunction("global:size").eval(params, stack);
                int len = (int) stack.pop();
                //fill data with elements of list
                Operator[] data = new Operator[len];
                for(int i = 0; i < len; i++) {
                    //get head of list
                    stack.push(list);
                    env.getPrefixFunction("list:head").eval(params, stack);
                    //set i'th element to the head
                    data[i] = stack.popOp();
                    //reset list to its tail
                    stack.push(list);
                    env.getPrefixFunction("list:tail").eval(params, stack);
                    list = stack.popOp();
                }
                Vector vec = new Vector(data);
                stack.push(vec);
            }
        };
    }
}