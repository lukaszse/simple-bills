package pl.com.seremak.simplebills.transactionmanagement.exceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ConstraintViolationExceptionExceptionHandler  {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(final ConstraintViolationException e) {
        final Map<String, Object> badRequestResponse = prepareBadRequestResponse(e.getMessage());
        return new ResponseEntity<>(badRequestResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    final Map<String, Object> prepareBadRequestResponse(final String message) {
        return Map.of(
                "timestamp", new Date(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "message", message
        );
    }
}
