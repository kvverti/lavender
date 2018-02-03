package kvverti.lavender.operators;

import kvverti.lavender.Stack;

/** Built in IO functions - the only side effects! */
@Deprecated
public class Io {
    
    public static final Operator FILE;
    
    static {
        FILE = new Operator("io:File", 1, 1, Operator.PREFIX) {
          
            @Override
            public void eval(Operator[] d, Stack stack) {
                
                Operator path = stack.popOp();
                if(path instanceof StringOp) {
                    String p = path.toString();
                    stack.push(new FileObj(p));
                } else
                    stack.push(Builtin.NAN);
            }
        };
    }
}