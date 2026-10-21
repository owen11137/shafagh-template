package com.misc.shafagh.dpst.impl.exception;
import com.misc.shafagh.dpst.impl.controller.DepositController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
/** پیام‌های قابل استفاده برای خطاهای افتتاح حساب. */
@RestControllerAdvice(assignableTypes=DepositController.class)
public class DepositExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail businessError(ResponseStatusException exception) {
        return ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getReason());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalidRequest(MethodArgumentNotValidException exception) {
        String detail=exception.getBindingResult().getFieldErrors().stream()
            .map(error->error.getField()+": "+error.getDefaultMessage()).sorted()
            .collect(java.util.stream.Collectors.joining(" | "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}
