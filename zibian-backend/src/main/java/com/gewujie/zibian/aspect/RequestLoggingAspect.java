package com.gewujie.zibian.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
public class RequestLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingAspect.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("execution(* com.gewujie.zibian.controller..*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        String methodName = signature.getName();
        String className = signature.getDeclaringType().getSimpleName();

        Map<String, Object> argsMap = new HashMap<>();
        if (paramNames != null && args != null) {
            for (int i = 0; i < Math.min(paramNames.length, args.length); i++) {
                argsMap.put(paramNames[i], args[i]);
            }
        } else if (args != null) {
            // Fallback if parameter names are not available
            for (int i = 0; i < args.length; i++) {
                argsMap.put("arg" + i, args[i]);
            }
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("ReqID: {} | Method: {}.{} | Args: {} | Exception: {} | Cost Time: {}ms",
                    requestId, className, methodName, toJson(argsMap), t.getMessage(), duration);
            throw t;
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("ReqID: {} | Method: {}.{} | Args: {} | Return: {} | Cost Time: {}ms",
                requestId, className, methodName, toJson(argsMap), toJson(result), duration);

        return result;
    }

    private String toJson(Object obj) {
        if (obj == null)
            return "null";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
