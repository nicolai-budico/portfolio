package info.wheelly.portfolio.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import info.wheelly.portfolio.exceptions.APIException;

@ControllerAdvice
public class APIErrorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(APIErrorHandler.class);

    @Getter
    @Setter
    @Accessors(chain = true)
    private static class ErrorMessage {
        private Integer httpStatus;
        private String message;
    }


    @ExceptionHandler(APIException.class)
    public ResponseEntity<ErrorMessage> handleAPIError(APIException error) {
        LOG.warn("Caught APIException: " + error.getMessage());
        return new ResponseEntity<>(
                new ErrorMessage()
                        .setMessage(error.getMessage())
                        .setHttpStatus(HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> invalidInput(MethodArgumentNotValidException error) {
        String errorMessage = error.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Invalid request");

        LOG.warn("Caught request validation error: " + errorMessage);
        return new ResponseEntity<>(
                new ErrorMessage()
                        .setMessage(errorMessage)
                        .setHttpStatus(HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }
}
