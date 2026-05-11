package com.osamah.games.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class LoggingAspectTest {

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        logger.setLevel(Level.DEBUG);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("com.osamah.games.user.UserService");
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1"});
    }


    @Test
    void logAround_ShouldExecuteNormally_WhenNoException() throws Throwable {
        when(joinPoint.proceed()).thenReturn("Success");

        loggingAspect.logAround(joinPoint);

        verify(joinPoint).proceed();
    }

    @Test
    void logAround_ShouldCatchAndRethrow_IllegalArgumentException() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new IllegalArgumentException("Bad Arg"));

        assertThrows(IllegalArgumentException.class, () -> loggingAspect.logAround(joinPoint));
    }

    @Test
    void logAround_ShouldCatchAndRethrow_GenericException() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new RuntimeException("System Failure"));

        assertThrows(RuntimeException.class, () -> loggingAspect.logAround(joinPoint));
    }

}