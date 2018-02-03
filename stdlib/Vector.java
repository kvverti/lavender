
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import kvverti.lavender.ljri.*;

public class Vector {
    
    @LavenderFunction("vector:_fromList")
    public LFunction fromList(LEnv env, LFunction list) {
        //useful functions
        LFunction head = env.getFunction("u_list:head");
        LFunction tail = env.getFunction("u_list:tail");
        LFunction size = env.getFunction("u_global:size");
        //get list size
        LFunction lenF = env.apply(size, list);
        int len = (int) env.asNumber(lenF);
        //create vector data
        LFunction[] data = new LFunction[len];
        for(int i = 0; i < len; i++) {
            //set i'th element to head
            data[i] = env.apply(head, list);
            //set list to tail
            list = env.apply(tail, list);
        }
        return env.wrap(data);
    }
    
    @LavenderFunction("vector:_at")
    public LFunction at(LEnv env, LFunction vec, LFunction idx) {
        
        LFunction[] data = env.unwrap(vec, LFunction[].class);
        if(data == null)
            return env.undefined();
        double i = env.asNumber(idx);
        if(i >= 0 && i < data.length)
            return data[(int) i];
        return env.undefined();
    }
    
    @LavenderFunction("vector:_size")
    public LFunction size(LEnv env, LFunction vec) {
        
        LFunction[] data = env.unwrap(vec, LFunction[].class);
        if(data == null)
            return env.undefined();
        return env.fromNumber(data.length);
    }
    
    @LavenderFunction("vector:_map")
    public LFunction map(LEnv env, LFunction vec, LFunction func) {
        
        LFunction[] data = env.unwrap(vec, LFunction[].class);
        if(data == null)
            return env.undefined();
        LFunction[] res = new LFunction[data.length];
        for(int i = 0; i < data.length; i++)
            res[i] = env.apply(func, data[i]);
        return env.wrap(res);
    }
    
    @LavenderFunction("vector:_filter")
    public LFunction filter(LEnv env, LFunction vec, LFunction func) {
        
        LFunction[] data = env.unwrap(vec, LFunction[].class);
        if(data == null)
            return env.undefined();
        List<LFunction> nlist = new ArrayList<>(data.length);
        for(LFunction f : data) {
            LFunction filterResult = env.apply(func, f);
            if(env.asBool(filterResult))
                nlist.add(f);
        }
        LFunction[] res = nlist.toArray(new LFunction[0]);
        return env.wrap(res);
    }
    
    @LavenderFunction("vector:_concat")
    public LFunction concat(LEnv env, LFunction vec, LFunction vec2) {
        
        LFunction[] data1 = env.unwrap(vec, LFunction[].class);
        LFunction[] data2 = env.unwrap(vec2, LFunction[].class);
        if(data1 == null || data2 == null)
            return env.undefined();
        LFunction[] res = new LFunction[data1.length + data2.length];
        System.arraycopy(data1, 0, res, 0, data1.length);
        System.arraycopy(data2, 0, res, data1.length, data2.length);
        return env.wrap(res);
    }
    
    @LavenderFunction("vector:_zip")
    public LFunction zip(LEnv env, LFunction vec1, LFunction vec2) {
        
        LFunction pair = env.getFunction("u_global:pair");
        LFunction[] data1 = env.unwrap(vec1, LFunction[].class);
        LFunction[] data2 = env.unwrap(vec2, LFunction[].class);
        if(data1 == null || data2 == null)
            return env.undefined();
        int len = Math.min(data1.length, data2.length);
        LFunction[] res = new LFunction[len];
        for(int i = 0; i < len; i++) {
            res[i] = env.apply(pair, data1[i], data2[i]);
        }
        return env.wrap(res);
    }
    
    @LavenderFunction("vector:_take")
    public LFunction take(LEnv env, LFunction vec, LFunction n) {
        
        LFunction[] data = env.unwrap(vec, LFunction[].class);
        if(data == null)
            return env.undefined();
        double len = env.asNumber(n);
        if(len >= 0 && len < data.length) {
            LFunction[] res = Arrays.copyOf(data, (int) len);
            return env.wrap(res);
        }
        return vec;
    }
    
    @LavenderFunction("vector:_in")
    public LFunction in(LEnv env, LFunction vec, LFunction elem) {
        
        LFunction[] data = env.unwrap(vec, LFunction[].class);
        if(data == null)
            return env.undefined();
        LFunction eq = env.getFunction("i_:=");
        for(LFunction f : data) {
            LFunction r = env.apply(eq, f, elem);
            if(env.asBool(r))
                return env.fromBool(true);
        }
        return env.fromBool(false);
    }
    
    @LavenderFunction("vector:_replace")
    public LFunction replace(LEnv env, LFunction vec, LFunction idx, LFunction func) {
     
        LFunction[] data = env.unwrap(vec, LFunction[].class);
        if(data == null)
            return env.undefined();
        double d = env.asNumber(idx);
        if(d >= 0 && d < data.length) {
            LFunction[] res = data.clone();
            res[(int) d] = env.apply(func, data[(int) d]);
            return env.wrap(res);
        }
        return vec;
    }
    
    @LavenderFunction("vector:_eq")
    public LFunction eq(LEnv env, LFunction vec1, LFunction vec2) {
        
        LFunction[] data1 = env.unwrap(vec1, LFunction[].class);
        LFunction[] data2 = env.unwrap(vec2, LFunction[].class);
        if(data1 == null || data2 == null)
            return env.fromBool(false);
        return env.fromBool(Arrays.equals(data1, data2));
    }
}