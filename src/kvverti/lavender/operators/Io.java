package kvverti.lavender.operators;

import kvverti.lavender.Stack;

/** Built in IO functions - the only side effects! */
public class Io {
    
    /* public static final Operator PRINT;
    public static final Operator GETC;
    public static final Operator GETS;
    public static final Operator CLOSE;
    public static final Operator OPEN;
    
    static {
        
        PRINT = new Operator("io:print", 2, 1, Operator.PREFIX) {
      
            @Override
            public void eval(Operator[] p, Stack stack) {
             
                Logic.STR.eval(p, stack);
                Operator value = stack.popOp();
                assert value instanceof StringOp : value;
                Operator outputFile = stack.popOp();
                if(outputFile instanceof FileObj) {
                    ((FileObj) outputFile).write(value.toString());
                }
                stack.push(outputFile);
            }
        };
        GETC = new Operator("io:getc", 1, 1, Operator.PREFIX) {
      
            @Override
            public void eval(Operator[] p, Stack stack) {
                
                Operator inputFile = stack.popOp();
                if(inputFile instanceof FileObj) {
                    int c = ((FileObj) inputFile).read();
                    if(c < 0)
                        stack.push(Builtin.NAN);
                    else
                        stack.push(StringOp.ofLiteral(Character.toString((char)c)));
                } else
                    stack.push(Builtin.NAN);
            }
        };
        GETS = new Operator("io:gets", 2, 1, Operator.PREFIX) {
      
            @Override
            public void eval(Operator[] p, Stack stack) {
                
                double len = stack.pop();
                Operator file = stack.popOp();
                if(!Double.isNaN(len) && file instanceof FileObj) {
                    String str = ((FileObj) file).read((int)len);
                    stack.push(StringOp.ofLiteral(str));
                } else
                    stack.push(Builtin.NAN);
            }
        };
        CLOSE = new Operator("io:close", 1, 1, Operator.PREFIX) {
      
            @Override
            public void eval(Operator[] p, Stack stack) {
                
                Operator file = stack.popOp();
                if(file instanceof FileObj)
                    ((FileObj) file).close();
                stack.push(0);
            }
        };
        OPEN = new Operator("io:open", 2, 1, Operator.PREFIX) {
      
            @Override
            public void eval(Operator[] p, Stack stack) {
                
                String args;
                {   //get file arguments
                    Operator op = stack.popOp();
                    if(!(op instanceof StringOp)) {
                        stack.push(Builtin.NAN);
                        return;
                    }
                    args = op.toString();
                }
                String name;
                {   //get file name
                    Operator op = stack.popOp();
                    if(!(op instanceof StringOp)) {
                        stack.push(Builtin.NAN);
                        return;
                    }
                    name = op.toString();
                }
                FileObj file = new FileObj(name, args);
                stack.push(file);
            }
        };
    } */
}