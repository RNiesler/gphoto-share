package rniesler.gphotoshare.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import rniesler.gphotoshare.exceptions.AuthenticationRequiredException;
import rniesler.gphotoshare.exceptions.ResourceNotFoundException;

@ControllerAdvice
public class ExceptionHandlingAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound() {
        return "errors/404";
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleNotAuthenticated() {
        return "errors/accessDenied";
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInternalError() {
        return "errors/500";
    }

}
