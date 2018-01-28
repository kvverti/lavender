package kvverti.lavender.ljri;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LavenderFunction {
    
    /**
     * The name of the function within the Lavender runtime.
     * A value of the empty string signifies an anonymous function.
     */
    String value() default "";
}