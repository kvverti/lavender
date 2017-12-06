package kvverti.lavender.operators;

import kvverti.lavender.Stack;
    
public class Logic {
    
    //separator
    public static final Operator COMMA = new Operator(":,", 2, 2, Operator.LEFT_INFIX){};
    
    //mathematical operations
    public static final Operator PI = Constant.of(Math.PI);
    public static final Operator E = Constant.of(Math.E);
    public static final Operator POWER;
    public static final Operator UPLUS;
    public static final Operator UMINUS;
    public static final Operator TIMES;
    public static final Operator DIVIDE;
    public static final Operator REMAINDER;
    public static final Operator PLUS;
    public static final Operator MINUS;
    
    //builtin functions
    public static final Operator ABS;
    public static final Operator SIN;
    public static final Operator COS;
    public static final Operator LOG;
    public static final Operator FLOOR;
    public static final Operator CEIL;
    public static final Operator INT;
    public static final Operator MAX;
    public static final Operator MIN;
    public static final Operator STR;
    public static final Operator NUM;
    
    //comparison operators (1.0 is true, 0.0 is false)
    public static final Operator EQ;
    public static final Operator NE;
    public static final Operator LT;
    public static final Operator LE;
    public static final Operator GT;
    public static final Operator GE;
    
    //boolean operators (1.0 is true, 0.0 is false)
    //nonzero arguments are true, except for NaN, which is false
    public static final Operator AND;
    public static final Operator OR;
    public static final Operator XOR;
    public static final Operator NOT;
    
    /** Zero and NaN are false, all other values are true. */
    static boolean bool(double n) {
        
        return n == n && n != 0.0;
    }
    
    static {
        
        POWER = new Operator(":**", 2, 1, Operator.RIGHT_INFIX) {

            @Override
            public void eval(Operator[] d, Stack stack) {
                
                double b = stack.pop();
                double a = stack.pop();
                stack.push(Math.pow(a, b));
            }
        };
        UPLUS = new Operator(":+", 1, 1, Operator.PREFIX) {

            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(stack.pop());
            }
        };
        UMINUS = new Operator(":-", 1, 1, Operator.PREFIX) { 
    
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(-stack.pop());
            }
        };
        TIMES = new Operator(":*", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(stack.pop() * stack.pop()); //bc mult is commutative
            }
        };
        DIVIDE = new Operator(":/", 2, 1, Operator.LEFT_INFIX) {
            
            @Override
            public void eval(Operator[] d, Stack stack) {
                //bc division is not commutative
                double b = stack.pop();
                double a = stack.pop();
                stack.push(a / b);
            }
        };
        REMAINDER = new Operator(":%", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                //bc remainder is not commutative
                double b = stack.pop();
                double a = stack.pop();
                stack.push(a % b);
            }
        };
        PLUS = new Operator(":+", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                Operator b = stack.popOp();
                Operator a = stack.popOp();
                if(a instanceof StringOp && b instanceof StringOp) {
                    stack.push(StringOp.ofLiteral(a.toString() + b.toString()));
                } else
                    stack.push(Constant.value(a) + Constant.value(b));
            }
        };
        MINUS = new Operator(":-", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                //bc subtraction isn't commutative either...
                stack.push(-stack.pop() + stack.pop());
            }
        };
        ABS = new Operator(":abs", 1, 1, Operator.PREFIX) {
      
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(Math.abs(stack.pop()));
            }
        };
        SIN = new Operator(":sin", 1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(Math.sin(stack.pop()));
            }
        };
        COS = new Operator(":cos", 1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(Math.cos(stack.pop()));
            }
        };
        LOG = new Operator(":log", 1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(Math.log(stack.pop()));
            }
        };
        FLOOR = new Operator(":floor", 1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(Math.floor(stack.pop()));
            }
        };
        CEIL = new Operator(":ceil", 1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(Math.ceil(stack.pop()));
            }
        };
        INT = new Operator(":int", 1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push((int) stack.pop());
            }
        };
        MAX = new Operator(":max", 2, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(Math.max(stack.pop(), stack.pop()));
            }
        };
        MIN = new Operator(":min", 2, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(Math.min(stack.pop(), stack.pop()));
            }
        };
        STR = new Operator(":str", 1, 1, Operator.PREFIX) {
      
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Operator op = stack.popOp();
                if(op.arity() == 1) {
                    stack.push(this);
                    op.eval(params, stack);
                    Operator res = stack.popOp();
                    stack.push(res);
                } else if(op instanceof Constant) {
                    stack.push(StringOp.ofLiteral(op.toString()));
                } else
                    stack.push(Builtin.NAN);
            }
        };
        NUM = new Operator(":num", 1, 1, Operator.PREFIX) {
      
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                Operator op = stack.popOp();
                if(op.arity() == 1) {
                    stack.push(this);
                    op.eval(params, stack);
                } else
                    stack.push(Constant.value(op));
            }
        };
        EQ = new Operator(":=", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                Operator b = stack.popOp();
                Operator a = stack.popOp();
                if(a.arity() == 1) {
                    stack.push(this);
                    a.eval(d, stack);
                    Operator tmp = stack.popOp();
                    if(tmp.arity() == 1) {
                        stack.push(b);
                        tmp.eval(d, stack);
                        Operator res = stack.popOp();
                        if(!(res instanceof Constant) || Double.isNaN(Constant.value(res)))
                            //whoops, didn't implement equals...or implemented it imporoperly
                            stack.push(a.equals(b) ? 1.0 : 0.0);
                        else
                            stack.push(res);
                    } else
                        stack.push(a.equals(b) ? 1.0 : 0.0);
                } else
                    stack.push(a.equals(b) ? 1.0 : 0.0);
            }
        };
        NE = new Operator(":!=", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                //a != b implies !(a = b)
                EQ.eval(d, stack);
                stack.push(bool(stack.pop()) ? 0.0 : 1.0);
            }
        };
        LT = new Operator(":<", 2, 1, Operator.LEFT_INFIX) {
      
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                Operator b = stack.popOp();
                Operator a = stack.popOp();
                if(a.arity() == 1) {
                    stack.push(this);
                    a.eval(d, stack);
                    Operator tmp = stack.popOp();
                    if(tmp.arity() == 1) {
                        stack.push(b);
                        tmp.eval(d, stack);
                    } else
                        stack.push(0.0);
                } else
                    stack.push(Constant.value(a) < Constant.value(b) ? 1.0 : 0.0);
            }
        };
        LE = new Operator(":<=", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                //a <= b implies !(b < a)
                Operator b = stack.popOp();
                Operator a = stack.popOp();
                stack.push(b);
                stack.push(a);
                LT.eval(d, stack);
                stack.push(bool(stack.pop()) ? 0.0 : 1.0);
            }
        };
        GT = new Operator(":>", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                //a > b implies b < a
                Operator b = stack.popOp();
                Operator a = stack.popOp();
                stack.push(b);
                stack.push(a);
                LT.eval(d, stack);
                stack.push(bool(stack.pop()) ? 1.0 : 0.0);
            }
        };
        GE = new Operator(":>=", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                //a >= b implies !(a < b)
                LT.eval(d, stack);
                stack.push(bool(stack.pop()) ? 0.0 : 1.0);
            }
        };
        AND = new Operator(":&", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                //note: no short circuit
                stack.push(bool(stack.pop()) & bool(stack.pop()) ? 1.0 : 0.0);
            }
        };
        OR = new Operator(":|", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                //note: no short circuit
                stack.push(bool(stack.pop()) | bool(stack.pop()) ? 1.0 : 0.0);
            }
        };
        XOR = new Operator(":^", 2, 1, Operator.LEFT_INFIX) {
        
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(bool(stack.pop()) ^ bool(stack.pop()) ? 1.0 : 0.0);
            }
        };
        NOT = new Operator(":!", 1, 1, Operator.PREFIX) {
      
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                stack.push(bool(stack.pop()) ? 0.0 : 1.0);
            }
        };
    }
}