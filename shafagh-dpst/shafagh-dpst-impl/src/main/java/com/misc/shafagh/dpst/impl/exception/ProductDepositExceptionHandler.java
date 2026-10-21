package com.misc.shafagh.dpst.impl.exception;

import com.misc.shafagh.dpst.impl.controller.ProductDepositController;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** خطاهای CRUD محصول، همراه توضیح قابل استفاده برای مصرف‌کننده API. */
@RestControllerAdvice(assignableTypes = ProductDepositController.class)
public class ProductDepositExceptionHandler {
    @ExceptionHandler({InvalidProductDepositException.class, ProductDepositNotFoundException.class, DuplicateProductDepositCodeException.class, ProductDepositInUseException.class})
    public ProblemDetail businessError(RuntimeException exception) {
        HttpStatus status = exception instanceof ProductDepositNotFoundException ? HttpStatus.NOT_FOUND
                : (exception instanceof DuplicateProductDepositCodeException || exception instanceof ProductDepositInUseException) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ProblemDetail.forStatusAndDetail(status, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalidRequest(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).sorted()
                .collect(java.util.stream.Collectors.joining(" | "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail concurrentUpdate(OptimisticLockingFailureException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "محصول هم‌زمان تغییر کرده است؛ اطلاعات را دوباره دریافت کنید");
    }
}
