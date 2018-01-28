
import kvverti.lavender.ljri.*;

/** Test of the Lavender Java Runtime Interface (LJRI) */
public class Test {
    
    //returns a function that adds a number to the given number
    @LavenderFunction("test:addTo")
    public LFunction testMethod(LEnv env, LFunction b) {
        
        LFunction plus = env.getFunction("i_:+");
        return env.capture(plus, b);
    }
}