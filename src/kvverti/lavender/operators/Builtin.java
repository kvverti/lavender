package kvverti.lavender.operators;

import kvverti.lavender.Stack;

public class Builtin {
    
    public static final Operator NAN = Constant.of(Double.NaN);
    //grouping "operators"
    public static final Operator LEFT_PAREN = new Operator("(", 1, 1, Operator.NA){};
    public static final Operator RIGHT_PAREN = new Operator(")", 1, 1, Operator.NA){};
    public static final Operator LEFT_BRACKET = new Operator("[", 1, 1, Operator.NA){};
    public static final Operator RIGHT_BRACKET = new Operator("]", 1, 1, Operator.NA){};
    
    //"function call" operator
    public static final Operator CALL;
    //function variable capture operator
    //captured params are listed after formal params
    public static final Operator CAPTURE;
    
    static {
        
        CALL = new Operator("CALL", -1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] params, Stack stack) {
                
                int numParams = (int) stack.pop();
                assert numParams > 0;
                Operator func = stack.popOp();
                if(func.arity() == 0) {
                    func.eval(params, stack);
                    func = stack.popOp();
                }
                if(numParams - 1 != func.arity()) {
                    //pop the params...and push NAN
                    for(int i = 0; i < numParams - 1; i++)
                        stack.popOp();
                    stack.push(NAN);
                } else {
                    //evaluate arguments to non-by-name parameters
                    Operator[] tmp = new Operator[func.arity()];
                    for(int i = func.arity() - 1; i >= 0; i--)
                        tmp[i] = stack.popOp();
                    for(int i = 0; i < func.arity(); i++) {
                        if(tmp[i].arity() != 0 || func.isByNameParam(i))
                            stack.push(tmp[i]);
                        else //evaluate the by-name argument
                            tmp[i].eval(params, stack);
                    }
                    func.eval(params, stack);
                }
            }
            
            @Override
            public boolean isByNameParam(int i) {
                
                //we don't know if the function will use by-name
                return true;
            }
        };
        CAPTURE = new Operator("CAP", -1, 1, Operator.PREFIX) {
        
            @Override
            public void eval(Operator[] p, Stack stack) {
                
                //pop the number of arguments
                //all but one are the parameters
                int numParams = (int) stack.pop() - 1;
                assert numParams != 0;
                boolean eval;
                if(numParams < 0) {
                    eval = false;
                    numParams = -numParams;
                } else
                    eval = true;
                //read arguments
                Operator[] params = new Operator[numParams];
                for(int i = numParams - 1; i >= 0; i--)
                    params[i] = stack.popOp();
                //last argument is the function to capture
                Operator func = stack.popOp();
                assert numParams <= func.arity();
                Capture res = new Capture(func, params);
                if(res.arity() == 0 && eval)
                    res.eval(p, stack);
                else
                    stack.push(res);
            }
        };
    }
}