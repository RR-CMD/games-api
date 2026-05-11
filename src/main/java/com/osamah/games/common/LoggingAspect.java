package com.osamah.games.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.osamah.games..*Controller.*(..)) || execution(* com.osamah.games..*Service.*(..))")
    public void applicationPackagePointcut() {
    }

    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature()
                .getDeclaringTypeName();
        String methodName = joinPoint.getSignature()
                .getName();

        boolean isSensitive = className.toLowerCase()
                .contains("auth") || className.toLowerCase()
                .contains("security") || methodName.toLowerCase()
                .contains("token");
        Object[] argsToLog = isSensitive ? new Object[]{"[HIDDEN_FOR_SECURITY]"} : joinPoint.getArgs();

        if (log.isDebugEnabled()) {
            log.debug("Enter: {}.{}() with argument[s] = {}", className, methodName, Arrays.toString(argsToLog));
        }

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            Object resultToLog = isSensitive ? "[HIDDEN_FOR_SECURITY]" : result;
            if (log.isDebugEnabled()) {
                log.debug("Exit: {}.{}() with result = {} (Execution time: {} ms)", className, methodName, resultToLog,
                        elapsedTime);
            }
            return result;

        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", Arrays.toString(argsToLog), className, methodName);
            throw e;
        } catch (Exception e) {
            boolean isHandled =
                    e instanceof com.osamah.games.exception.ResourceNotFoundException || e instanceof com.osamah.games.exception.BadRequestException || e instanceof com.osamah.games.exception.DuplicateResourceException || e instanceof org.springframework.web.bind.MethodArgumentNotValidException || e instanceof org.springframework.http.converter.HttpMessageNotReadableException || e instanceof org.springframework.security.core.AuthenticationException || e instanceof org.springframework.security.access.AccessDeniedException || e instanceof io.jsonwebtoken.JwtException;

            if (!isHandled) {
                log.error("CRITICAL Unexpected Exception in {}.{}()", className, methodName, e);
            }
            throw e;
        }
    }
}
