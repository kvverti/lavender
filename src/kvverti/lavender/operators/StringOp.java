package kvverti.lavender.operators;

import kvverti.lavender.Stack;

public class StringOp extends Operator {
    
    private class Comp extends Operator {
        
        private final int wh;
        
        Comp(int which) {
            super(":string$comp", 1, 1, Operator.PREFIX);
            wh = which;
        }
        
        @Override
        public void eval(Operator[] p, Stack stack) {
            
            Operator b = stack.popOp();
            if(b instanceof StringOp) {
                switch(wh) {
                case 0:
                    stack.push(value.compareTo(((StringOp) b).value) < 0 ? 1.0 : 0.0);
                    break;
                case 1:
                    stack.push(value.compareTo(((StringOp) b).value) > 0 ? 1.0 : 0.0);
                    break;
                case 2:
                    stack.push(value.compareTo(((StringOp) b).value) <= 0 ? 1.0 : 0.0);
                    break;
                case 3:
                    stack.push(value.compareTo(((StringOp) b).value) >= 0 ? 1.0 : 0.0);
                    break;
                default:
                    throw new AssertionError(wh);
                }
            } else
                stack.push(0.0);
        }
    }
    
    private final String value;
    private final Operator lt = new Comp(0);
    private final Operator gt = new Comp(1);
    private final Operator le = new Comp(2);
    private final Operator ge = new Comp(3);
    
    private StringOp(String value) {
        
        super(value, 1, 1, Operator.PREFIX);
        this.value = value;
    }
    
    public static StringOp of(String str) {
        
        StringBuilder sb = new StringBuilder();
        int idx = 1;
        while(idx < str.length() - 1) {
            
            char c = str.charAt(idx);
            if(c != '\\')
                sb.append(c);
            else {
                c = str.charAt(++idx);
                switch(c) {
                    
                    case '\\':
                        sb.append('\\');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '\"':
                        sb.append('\"');
                        break;
                    case '\'':
                        sb.append('\'');
                        break;
                    default:
                        throw new AssertionError(c);
                }
            }
            idx++;
        }
        return new StringOp(sb.toString());
    }
    
    public static StringOp ofLiteral(String str) {
        
        return new StringOp(str);
    }
    
    @Override
    public void eval(Operator[] params, Stack stack) {
        
        Operator op = stack.popOp();
        if(op == Logic.STR) {
            stack.push(this);
        } else if(op == Logic.NUM) {
            double d;
            try { d = Double.parseDouble(value); }
            catch(NumberFormatException e) { d = Double.NaN; }
            stack.push(d);
        } else if(op.toString().equals("global:size")) {
            stack.push(value.length());
        } else if(op == Logic.LT) {
            stack.push(lt);
        } else if(op == Logic.LE) {
            stack.push(le);
        } else if(op == Logic.GT) {
            stack.push(gt);
        } else if(op == Logic.GE) {
            stack.push(ge);
        } else {
            double idx = Constant.value(op);
            if(idx >= 0 && idx < value.length())
                stack.push(new StringOp(value.substring((int) idx, (int) idx + 1)));
            else
                stack.push(Builtin.NAN);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        
        return obj instanceof StringOp && ((StringOp) obj).value.equals(value);
    }
    
    @Override
    public int hashCode() {
        
        return value.hashCode();
    }
}