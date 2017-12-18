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
                System.out.println("Running in debug mode");
            } else if(args[i].equals("-fp")) {
                i++;
                runargs.setFilepath(args[i]);
            } else {
                System.err.println("Argument not recognized: " + args[i]);
                System.exit(1);
            }
        }
        Lavender rt = new Lavender(runargs);
        System.out.println("Lavender runtime v. 1.1 by Chris Nero");
        System.out.println("Open source at https://github.com/kvverti/lavender");
        System.out.println("Enter function definitions or expressions");
        rt.start();
    }   
}
