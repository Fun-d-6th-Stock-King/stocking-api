package com.stocking.infra.config;

import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.base.Joiner;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
public class LogAspect {

//    @Around("(within(@org.springframework.stereotype.Controller *) || "
    @Around("("
            + "within(@org.springframework.web.bind.annotation.RestController *))"
            + "&& execution(public * *(..))")
    public Object logging(ProceedingJoinPoint pjp) throws Throwable {
        
        RequestAttributes requestAttribute = RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = ((ServletRequestAttributes) requestAttribute).getRequest();

        log.info("\n==================== REQUEST : [{}] {} from {}({}) / {}({}) = {} =======================", 
            request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), request.getHeader("User-Agent"),
            pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), getRequestParams(requestAttribute)
        );
        
        final StopWatch stopWatch = new StopWatch();
        
        stopWatch.start();
        Object result = pjp.proceed();
        stopWatch.stop();

        log.info("\n==================== RESPONSE : {}({}) ({}ms) =======================", 
                pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), stopWatch.getTotalTimeMillis());

        return result;

    }

    /**
     * request 파라미터를 문자열로 반환
     * @param requestAttribute
     * @return
     */
    private String getRequestParams(RequestAttributes requestAttribute) {

        String params = "";

        if(requestAttribute != null){
            HttpServletRequest request = ((ServletRequestAttributes) requestAttribute).getRequest();

            Map<String, String[]> paramMap = request.getParameterMap();

            if(!paramMap.isEmpty()) {
                params = paramMap.entrySet().stream()
                    .map(entry -> String.format("%s -> (%s)",
                            entry.getKey(), Joiner.on(",").join(entry.getValue())))
                    .collect(Collectors.joining(", "));
                params = " [" + params + "]";
            }
        }
        return params;
    }
}
