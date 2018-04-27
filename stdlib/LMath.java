
import kvverti.lavender.ljri.*;

public class LMath {
    
/*     @LavenderFunction("math:pi")
    public LFunction pi(LEnv env) {
        
        return env.fromNumber(Math.PI);
    }
    
    @LavenderFunction("math:e")
    public LFunction e(Lenv env) {
        
        return env.fromNumber(Math.E);
    }
    
    @LavenderFunction("math:abs")
    public LFunction abs(LEnv env, LFunction a) {
        
        double d = env.asNumber(a);
        return env.fromNumber(Math.abs(d));
    } */
    
    @LavenderFunction("math:sin")
    public LFunction sin(LEnv env, LFunction a) {
        
        double d = Math.sin(env.asNumber(a));
        return env.fromNumber(d);
    }
    
    @LavenderFunction("math:cos")
    public LFunction cos(LEnv env, LFunction a) {
        
        double d = Math.cos(env.asNumber(a));
        return env.fromNumber(d);
    }
    
    @LavenderFunction("math:log")
    public LFunction log(LEnv env, LFunction a) {
        
        double d = Math.log(env.asNumber(a));
        return env.fromNumber(d);
    }
    
    @LavenderFunction("math:floor")
    public LFunction floor(LEnv env, LFunction a) {
        
        double d = Math.floor(env.asNumber(a));
        return env.fromNumber(d);
    }
    
    @LavenderFunction("math:ceil")
    public LFunction ceil(LEnv env, LFunction a) {
        
        double d = Math.ceil(env.asNumber(a));
        return env.fromNumber(d);
    }
    
    @LavenderFunction("math:int")
    public LFunction int_(LEnv env, LFunction a) {
        
        double d = (int) env.asNumber(a);
        return env.fromNumber(d);
    }
    
/*     @LavenderFunction("math:max")
    public LFunction max(LEnv env, LFunction a, LFunction b) {
        
        double d = Math.max(env.asNumber(a), env.asNumber(b));
        return env.fromNumber(d);
    }
    
    @LavenderFunction("math:min")
    public LFunction min(LEnv env, LFunction a, LFunction b) {
        
        double d = Math.min(env.asNumber(a), env.asNumber(b));
        return env.fromNumber(d);
    } */
}