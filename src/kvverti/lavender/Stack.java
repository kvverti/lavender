package kvverti.lavender;

import java.util.List;
import java.util.ArrayList;

import kvverti.lavender.operators.Operator;
import kvverti.lavender.operators.Constant;

/**
 * A simple Stack structure for functions.
 */
public class Stack {
    
    private final List<Operator> data;
    
    public Stack() {
        
        data = new ArrayList<>();
    }
    
    public void push(Operator elem) {
        
        data.add(elem);
    }
    
    public void push(double elem) { push(Constant.of(elem)); }
    
    public double pop() { return Constant.value(popOp()); }
    
    public Operator popOp() {
        
        return data.remove(data.size() - 1);
    }
    
    public int size() { return data.size(); }
    
    public boolean isEmpty() { return data.isEmpty(); }
    
    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("size=").append(data.size()).append(", data=[ ");
        for(Operator op : data)
            sb.append(op).append(" ");
        sb.append("]");
        return sb.toString();
    }
}