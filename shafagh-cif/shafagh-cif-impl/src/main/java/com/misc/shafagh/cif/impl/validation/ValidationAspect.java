package com.misc.shafagh.cif.impl.validation;

import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.*;

@Component
@Aspect
public class ValidationAspect {


//    @Pointcut("within(com.misc.shafagh.cif.impl.service.impl.ProductFeatureServiceImpl)")
//    @Pointcut("execution(public void com.misc.shafagh.cif.impl.service.api.ProductFeatureService.validateNumber(int))")
//    public void validationPointcut(){}

//    @Around("validationPointcut()")
//    public void aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
//        System.out.println("In Around Aspect");
//        int arg = (int) joinPoint.getArgs()[0];
//        if (arg < 0)
//            throw new RuntimeException("Argument should not be negative");
//        else
//            joinPoint.proceed();
//    }
}