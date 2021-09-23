package net.lab1024.smartadmin.service.module.support.repeatsubmit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.lab1024.smartadmin.service.common.codeconst.ResponseCodeConst;
import net.lab1024.smartadmin.service.common.domain.ResponseDTO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * [  ]
 *
 * @author 罗伊
 * @date
 */
@Aspect
@Order(1)
public class SmartRepeatSubmitAspect {

    /**
     * 限制缓存最大数量 超过后先放入的会自动移除
     * 默认缓存时间
     */
    private static Cache<Object, Object> cache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(NoRepeatSubmit.MAX_INTERVAL, TimeUnit.MILLISECONDS).build();

    private Function<HttpServletRequest, SmartRepeatSubmitUserDTO> userFunction;

    /**
     * 获取用户信息
     *
     * @param userFunction
     */
    public SmartRepeatSubmitAspect(Function<HttpServletRequest, SmartRepeatSubmitUserDTO> userFunction) {
        this.userFunction = userFunction;
    }

    /**
     * 定义切入点
     *
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(net.lab1024.smartadmin.service.module.support.repeatsubmit.NoRepeatSubmit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        SmartRepeatSubmitUserDTO user = this.userFunction.apply(request);
        if (user == null) {
            return point.proceed();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String servletPath = attributes.getRequest().getServletPath();
        String key = user.getUserId() + "_" + servletPath;
        Object value = cache.getIfPresent(key);
        if (value != null) {
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            NoRepeatSubmit annotation = method.getAnnotation(NoRepeatSubmit.class);
            int interval = Math.min(annotation.value(), NoRepeatSubmit.MAX_INTERVAL);
            if (System.currentTimeMillis() < (long) value + interval) {
                // 提交频繁
                return ResponseDTO.wrap(ResponseCodeConst.REPEAT_SUBMIT);
            }
        }
        cache.put(key, System.currentTimeMillis());
        Object obj = point.proceed();
        cache.put(key, System.currentTimeMillis());
        return obj;
    }

}