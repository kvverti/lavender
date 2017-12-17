package kvverti.lavender.runtime;

import java.io.InputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import kvverti.lavender.Lavender;
import kvverti.lavender.Stack;
import kvverti.lavender.operators.Operator;

/**
 * The actual interpreter for Lavender. Instances of
 * this class evaluate expressions written in the Lavender
 * language.
 */
public class Interpreter {
    
    private final Lavender environment;
    private final Stack theStack;
    private final Map<String, String> importedFunctionNames;
    private final Parser parser;
    private String currentDomain;
    
    public Interpreter(Lavender env) {
        
        environment = env;
        theStack = new Stack();
        importedFunctionNames = new HashMap<>();
        parser = new Parser();
        currentDomain = "global";
    }
    
    public Operator evaluate(Operator op) {
        
        op.eval(null, theStack);
        return theStack.popOp();
    }
    
    /**
     * Parses and evaluates all contents of the input stream.
     */
    public void parseInput(InputStream in) {
        
        String tmpDomain = currentDomain;
        try {
            Scanner sc = new Scanner(in);
            while(sc.hasNext()) {
                String s = parseImpl(sc, false);
                if(environment.debug())
                    System.out.println(s);
            }
        } finally {
            currentDomain = tmpDomain;
        }
    }
    
    /**
     * Reads one expression from the input stream
     * and returns a string representation of the result
     * of its evaluation.
     */
    public String repl(InputStream in) {
        
        Scanner sc = new Scanner(in);
        try {
            return parseImpl(sc, true);
        } catch(RuntimeException e) {
            if(environment.debug())
                e.printStackTrace();
            return e.getMessage();
        }
    }
    
    private String parseImpl(Scanner sc, boolean repl) {
        
        int[] nesting = { 0, 0 };
        List<Token> tokens = new ArrayList<>();
        do {
            if(repl)
                System.out.print(currentDomain + "> ");
            getLine(sc, nesting, tokens);
            if(nesting[0] < 0 || nesting[1] < 0)
                return "Unbalanced brackets in input";
        } while((nesting[0] + nesting[1]) > 0);
        if(tokens.isEmpty())
            return "";
        else if(tokens.get(0).value().equals("@")) {
            return runCommand(tokens.subList(1, tokens.size()));
        } else {
            Operator proc = parser.parseExpr(tokens, environment, importedFunctionNames, currentDomain);
            return evaluate(proc).toString();
        }
    }
    
    private void getLine(Scanner in, int[] nesting, List<? super Token> res) {
        
        List<Token> tmp = Tokens.split(in.nextLine());
        for(Token t : tmp) {
            if(t.type() == Token.LITERAL) {
                switch(t.value().charAt(0)) {
                    case '[':
                        nesting[1]++;
                        break;
                    case ']':
                        nesting[1]--;
                        break;
                    case '(':
                        nesting[0]++;
                        break;
                    case ')':
                        nesting[0]--;
                        break;
                }
            }
        }
        res.addAll(tmp);
    }
    
    private String runCommand(List<Token> command) {
        
        if(command.isEmpty())
            return "No command entered";
        String commandName = command.get(0).value();
        switch(commandName) {
            
            case "quit":
                if(command.size() != 1)
                    return "Usage: @quit";
                System.exit(0);
                return "";
            case "delete":
                if(command.size() != 2)
                    return "Usage: @delete <function>";
                String func = command.get(1).value();
                if(environment.unbind(func))
                    return "Deleted function";
                return "Function not found";
            case "namespace":
                if(command.size() == 1)
                    return "Namespace " + currentDomain;
                if(command.size() == 2) {
                    Token t = command.get(1);
                    if(t.type() != Token.IDENT)
                        return "Not a valid namespace";
                    currentDomain = t.value();
                    importedFunctionNames.clear();
                    return "Namespace " + currentDomain;
                }
                return "Usage: @namespace [namespace]";
            case "forward":
                if(command.size() == 1)
                    return "Usage: @forward <func-decl>";
                parser.declareFunction(command.subList(1, command.size()), environment, currentDomain);
                return "";
            case "import":
                if(command.size() != 2)
                    return "Usage: @import <file>";
                {
                    Token t = command.get(1);
                    if(t.type() != Token.STRING)
                        return "Invalid file";
                    String name = t.value();
                    name = name.substring(1, name.length() - 1);
                    environment.loadFile(name);
                    return "Import " + name;
                }
            case "resolve":
                if(command.size() > 1) {
                    Token[] tokens = new Token[command.size() - 1];
                    for(int i = 1; i < command.size(); i++) {
                        tokens[i - 1] = command.get(i);
                        if(tokens[i - 1].type() != Token.STRING)
                            return "Invalid file";
                    }
                    for(Token t : tokens) {
                        String s = t.value();
                        environment.loadFile(s.substring(1, s.length() - 1));
                    }
                }
                environment.link();
                return "Link successful";
            case "using":
                if(command.size() != 2)
                    return "Usage: @using <name>";
                {
                    Token name = command.get(1);
                    switch(name.type()) {
                        
                        case Token.IDENT:
                        case Token.SYMBOL:
                            //using namespace
                            String namespace = name.value();
                            List<String> names = environment.getNamesInNamespace(namespace);
                            for(String s : names) {
                                importedFunctionNames.put(s, namespace + ":" + s);
                            }
                            break;
                        case Token.QUAL_IDENT:
                        case Token.QUAL_SYMBOL:
                            String qual = name.value();
                            String simpleName = qual.split(":", 2)[1];
                            importedFunctionNames.put(simpleName, qual);
                            break;
                        default:
                            return "Invalid name";
                    }
                    return "Using " + name.value();
                }
            case "dump":
                System.out.println("===============================");
                environment.dump();
                System.out.println("===============================");
                return "";
            default:
                return "Command not found: " + commandName;
        }
    }
    
    public void dump() {
        
        System.out.println("Lavender interpreter information:");
        System.out.println("Function aliases:");
        for(Map.Entry<String, String> entry : importedFunctionNames.entrySet()) {
            System.out.print(entry.getKey());
            System.out.print(" -> ");
            System.out.println(entry.getValue());
        }
        System.out.println("Contents of stack:");
        System.out.println(theStack);
    }
}