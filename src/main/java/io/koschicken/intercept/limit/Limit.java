package io.koschicken.intercept.limit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Limit {
    /**
     * 监听函数隔多久才能触发一次
     */
    long value();

    /**
     * 不能触发时的提示消息
     */
    String message() default "CD中...";

    /**
     * 时间类型，默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 如果可以区分账号，是否区分账号
     * 例如不同的人的触发时间不同
     * 默认 true
     */
    boolean code() default true;

    /**
     * 如果可以区分群号，是否区分群号
     * 默认 true
     */
    boolean group() default true;

    /**
     * 如果可以区分bot，是否区分bot
     * 默认 false
     */
    boolean bot() default false;
}
