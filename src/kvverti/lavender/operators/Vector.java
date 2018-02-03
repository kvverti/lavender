package kvverti.lavender.operators;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import kvverti.lavender.Lavender;
import kvverti.lavender.Stack;

/**
 * @deprecated The LJRI implementation of vectors supercedes this class.
 * An immutable, random-access container usable in Lavender.
 */
@Deprecated
public class Vector extends Operator {
    
    public static final Operator MK_VECTOR;
    private final Operator[] data;
    private final Lavender env;
    private final Map<Operator, Operator> methods;
    
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
        methods = new HashMap<>();
        fillInMethodTable();
    }
    
    public static Vector of(Operator[] data) {
        
        return new Vector(data);
    }
    
    @Override
    public void eval(Operator[] params, Stack stack) {
        
        Operator op = stack.popOp();
        Operator method = methods.get(op);
        if(method != null) {
            if(method.arity() == 0)
                method.eval(params, stack);
            else
                stack.push(method);
        } else {
            double i = Constant.value(op);
            if(i >= 0 && i < data.length)
                stack.push(data[(int) i]);
            else
                stack.push(Builtin.NAN);
        }
    }
    
    @Override
    public Operator resolve() {
        
        fillInMethodTable();
        return this;
    }
    
    @Override
    public boolean equals(Object obj) {
       
        if(!(obj instanceof Vector))
            return false;
        Vector vec = (Vector) obj;
        return Arrays.equals(data, vec.data);
    }
    
    @Override
    public int hashCode() {
        
        return Arrays.hashCode(data);
    }
    
    private void fillInMethodTable() {
        
        methods.clear();
        methods.put(env.getInfixFunction("algorithm:map"),
            new Operator("$$VECTOR:map", 1, 1, Operator.PREFIX) {
            
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Operator func = stack.popOp();
                if(func.arity() != 1) {
                    stack.push(Builtin.NAN);
                    return;
                }
                Operator[] newData = new Operator[data.length];
                for(int i = 0; i < data.length; i++) {
                    stack.push(data[i]);
                    func.eval(params, stack);
                    newData[i] = stack.popOp();
                }
                stack.push(new Vector(newData));
            }
        });
        methods.put(env.getInfixFunction("algorithm:flatmap"),
            new Operator("$$VECTOR:flatmap", 1, 1, Operator.PREFIX) {
           
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Operator func = stack.popOp();
                if(func.arity() != 1) {
                    stack.push(Builtin.NAN);
                    return;
                }
                List<Operator> newData = new ArrayList<>(data.length + (data.length / 2));
                for(int i = 0; i < data.length; i++) {
                    stack.push(data[i]);
                    func.eval(params, stack);
                    Vector vec;
                    {
                        Operator op = stack.popOp();
                        if(!(op instanceof Vector)) {
                            stack.push(Builtin.NAN);
                            return;
                        }
                        vec = (Vector) op;
                    }
                    newData.addAll(Arrays.asList(vec.data));
                }
                stack.push(new Vector(newData.toArray(new Operator[0])));
            }
        });
        methods.put(env.getInfixFunction("algorithm:filter"),
            new Operator("$$VECTOR:filter", 1, 1, Operator.PREFIX) {
           
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Operator func = stack.popOp();
                if(func.arity() != 1) {
                    stack.push(Builtin.NAN);
                    return;
                }
                List<Operator> newData = new ArrayList<>(data.length);
                for(int i = 0; i < data.length; i++) {
                    stack.push(data[i]);
                    func.eval(params, stack);
                    if(Logic.bool(stack.pop()))
                        newData.add(data[i]);
                }
                stack.push(new Vector(newData.toArray(new Operator[0])));
            }
        });
        methods.put(env.getInfixFunction("algorithm:reduce"),
            new Operator("$$VECTOR:reduce", 2, 1, Operator.PREFIX) {
           
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Operator func = stack.popOp();
                Operator accum = stack.popOp();
                if(func.arity() != 2) {
                    stack.push(Builtin.NAN);
                    return;
                }
                for(int i = 0; i < data.length; i++) {
                    stack.push(accum);
                    stack.push(data[i]);
                    func.eval(params, stack);
                    accum = stack.popOp();
                }
                stack.push(accum);
            }
        });
        methods.put(env.getInfixFunction("algorithm:zip"),
            new Operator("$$VECTOR:zip", 1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Vector other;
                {
                    Operator op = stack.popOp();
                    if(!(op instanceof Vector)) {
                        stack.push(Builtin.NAN);
                        return;
                    }
                    other = (Vector) op;
                }
                int len = Math.min(data.length, other.data.length);
                Operator[] newData = new Operator[len];
                for(int i = 0; i < len; i++) {
                    stack.push(data[i]);
                    stack.push(other.data[i]);
                    env.getPrefixFunction("global:pair").eval(params, stack);
                    newData[i] = stack.popOp();
                }
                stack.push(new Vector(newData));
            }
        });
        methods.put(env.getInfixFunction("algorithm:take"),
            new Operator("$$VECTOR:take", 1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                int num = Math.min((int) stack.pop(), data.length);
                num = Math.max(num, 0);
                Operator[] newData = Arrays.copyOf(data, num);
                stack.push(new Vector(newData));
            }
        });
        methods.put(env.getInfixFunction("algorithm:replace"),
            new Operator("$$VECTOR:replace", 2, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Operator func = stack.popOp();
                int idx = (int) stack.pop();
                if(func.arity() != 1) {
                    stack.push(Builtin.NAN);
                    return;
                }
                Operator[] newData = data.clone();
                if(idx >= 0 && idx < data.length) {
                    stack.push(data[idx]);
                    func.eval(params, stack);
                    newData[idx] = stack.popOp();
                }
                stack.push(new Vector(newData));
            }
        });
        methods.put(env.getInfixFunction("algorithm:in"),
            new Operator("$$VECTOR:in", 1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Operator elem = stack.popOp();
                for(Operator op : data) {
                    stack.push(elem);
                    stack.push(op);
                    Logic.EQ.eval(params, stack);
                    if(Logic.bool(stack.pop())) {
                        stack.push(1.0);
                        return;
                    }
                }
                stack.push(0.0);
            }
        });
        methods.put(env.getInfixFunction("algorithm:++"),
            new Operator("$$VECTOR:++", 1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Vector other;
                {
                    Operator op = stack.popOp();
                    if(!(op instanceof Vector)) {
                        stack.push(Builtin.NAN);
                        return;
                    }
                    other = (Vector) op;
                }
                Operator[] newData = new Operator[data.length + other.data.length];
                System.arraycopy(data, 0, newData, 0, data.length);
                System.arraycopy(other.data, 0, newData, data.length, other.data.length);
                stack.push(new Vector(newData));
            }
        });
        methods.put(env.getPrefixFunction("global:size"),
            new Operator("$$VECTOR:size", 0, 1, Operator.NA) {
           
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                stack.push(data.length);
            }
        });
        methods.put(Logic.STR,
            new Operator("$$VECTOR:str", 0, 1, Operator.NA) {
        
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Operator res = StringOp.ofLiteral("[ ");
                Operator space = StringOp.ofLiteral(" ");
                stack.push(res);
                for(int i = 0; i < data.length; i++) {
                    stack.push(data[i]);
                    Logic.STR.eval(params, stack);
                    Logic.PLUS.eval(params, stack);
                    stack.push(space);
                    Logic.PLUS.eval(params, stack);
                }
                stack.push(StringOp.ofLiteral("]"));
                Logic.PLUS.eval(params, stack);
            }
        });
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