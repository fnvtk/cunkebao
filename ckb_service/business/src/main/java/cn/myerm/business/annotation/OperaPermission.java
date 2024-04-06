package cn.myerm.business.annotation;
import java.lang.annotation.*;

@Target({ElementType.METHOD})//
@Retention(RetentionPolicy.RUNTIME) // 注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Documented//说明该注解将被包含在javadoc中
public @interface OperaPermission {
    /**
     * 操作项
     * @return String
     */
    String value() default "";
}
