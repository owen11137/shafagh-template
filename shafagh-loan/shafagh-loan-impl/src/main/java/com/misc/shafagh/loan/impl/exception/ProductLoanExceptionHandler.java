package com.misc.shafagh.loan.impl.exception;

import com.misc.shafagh.loan.impl.controller.ProductLoanController;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** خطاهای CRUD محصول، همراه توضیح قابل استفاده برای مصرف‌کننده API. */
@RestControllerAdvice(assignableTypes = ProductLoanController.class)
public class ProductLoanExceptionHandler {
    @ExceptionHandler({InvalidProductLoanException.class, ProductLoanNotFoundException.class, DuplicateProductLoanCodeException.class})
    public ProblemDetail businessError(RuntimeException exception) {
        HttpStatus status = exception instanceof ProductLoanNotFoundException ? HttpStatus.NOT_FOUND
                : exception instanceof DuplicateProductLoanCodeException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
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
