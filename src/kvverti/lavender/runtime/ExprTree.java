package kvverti.lavender.runtime;

import java.io.PrintStream;
import java.util.*;

import kvverti.lavender.Stack;
import kvverti.lavender.operators.*;
import kvverti.lavender.operators.Builtin;

public class ExprTree {

    private Node root;
    private int params;
    
    public ExprTree(Collection<Operator> operations, int numParams) {
        
        assert !operations.isEmpty() : "operations empty";
        params = numParams;
        Deque<Node> tmpNodes = new ArrayDeque<>();
        for(Operator op : operations) {
            
            Node node = new Node();
            node.value = op;
            if(op.arity() == 0)
                tmpNodes.push(node);
            else if(op.arity() == -1) {
                Deque<Node> deq = new ArrayDeque<>();
                while(tmpNodes.peek().value != Parser.BEGIN_ARGS)
                    deq.push(tmpNodes.pop());
                tmpNodes.pop();
                node.children = new Node[deq.size() + 1];
                for(int i = 0; i < node.children.length - 1; i++)
                    node.children[i] = deq.pop();
                Node len = new Node();
                len.value = Constant.of(node.children.length - 1);
                node.children[node.children.length - 1] = len;
                tmpNodes.push(node);
            } else {
                assert op.arity() > 0 : op.arity() + " " + op;
                node.children = new Node[op.arity()];
                for(int i = op.arity() - 1; i >= 0; i--) {
                    if(tmpNodes.isEmpty())
                        throw new RuntimeException("Invalid stack: " + op);
                    node.children[i] = tmpNodes.pop();
                }
                tmpNodes.push(node);
            }
        }
        if(tmpNodes.size() != 1)
            throw new RuntimeException("Invalid stack");
        root = tmpNodes.pop();
    }
    
    public Deque<Operator> flattenPostOrder() {
        
        Deque<Operator> ops = new ArrayDeque<>();
        fillOps(ops, root);
        return ops;
    }
    
    private void fillOps(Queue<Operator> ops, Node node) {
        
        assert node != null : "Node is null";
        if(node.children != null) {
            for(int i = 0; i < node.children.length; i++) {
                Node n = node.children[i];
                //if param is lazy, wrap in a (unevaluated) zero-arity capturing function
                if(node.value.isByNameParam(i)) {
                    if(n.value.arity() != 0) {
                        Queue<Operator> tmp = new ArrayDeque<>();
                        fillOps(tmp, n);
                        Operator byName = new StackProcedure(
                            node.value + "$byName$", params, params, Operator.PREFIX, tmp, new BitSet());
                        //prevent evaluation when captured
                        byName = new FunctionValue(byName);
                        ops.add(byName);
                        if(params > 0) {
                            for(int j = 0; j < params; j++)
                                ops.add(new Parameter(j, true));
                            ops.add(Constant.of(-params + 1));
                            ops.add(Builtin.CAPTURE);
                        }
                    } else
                        ops.add(n.value.asValue());
                } else
                    fillOps(ops, n);
            }
        }
        ops.add(node.value);
    }
    
    public void print(PrintStream out) {
        
        printImpl(out, root, 0);
    }
    
    private void printImpl(PrintStream out, Node node, int level) {
        
        assert node != null : "node null";
        for(int i = 0; i < level; i++)
            out.print("  ");
        out.println(node.value);
        if(node.children != null) {
            for(Node n : node.children) {
                
                printImpl(out, n, level + 1);
            }
        }
    }
    
    private static class Node {
        
        Operator value;
        Node[] children;
    }
}