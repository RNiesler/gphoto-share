package rniesler.gphotoshare.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import rniesler.gphotoshare.exceptions.AuthenticationRequiredException;
import rniesler.gphotoshare.exceptions.ResourceNotFoundException;
import rniesler.gphotoshare.security.impl.GoogleApiException;

@ControllerAdvice
@Slf4j
public class ExceptionHandlingAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex) {
        log.error("Couldn't find a resource " + ex.getMessage());
        return "errors/404";
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleNotAuthenticated() {
        log.error("Tried to use Security method while not authenticated.");
        return "errors/accessDenied";
    }

    @ExceptionHandler(GoogleApiException.class)
    @ResponseStatus(HttpStatus.OK)
    public String handleGoogleApiException(GoogleApiException ex) {
        log.error("Exception while calling Google Photos API. Response status: " + ex.getStatus() + ". Message: " + ex.getMessage());
        return "errors/apiCallError";
    }


    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInternalError(RuntimeException ex) {
        log.error("Exception caught: " + ex.getMessage());
        // If the exception is annotated with @ResponseStatus rethrow it and let
        if (AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class) != null) {
            throw ex;
        }
        return "errors/500";
    }

}
