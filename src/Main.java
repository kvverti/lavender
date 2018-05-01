/* anonymous */

import kvverti.lavender.Lavender;

/** The entry point for the entire Lavender runtime */
public class Main {
    
    /** Parses the program arguments and starts a Lavender runtime. */
    public static void main(String[] args) {
        
        Lavender.Args runargs = new Lavender.Args();
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-debug")) {
                runargs.setDebug(true);
            } else if(args[i].equals("-fp")) {
                i++;
                runargs.setFilepath(args[i]);
            } else if(args[i].startsWith("-")) {
                System.err.println("Argument not recognized: " + args[i]);
                System.exit(1);
            } else {
                runargs.setMainFile(args[i]);
            }
        }
        Lavender rt = new Lavender(runargs);
        rt.start();
    }   
}
