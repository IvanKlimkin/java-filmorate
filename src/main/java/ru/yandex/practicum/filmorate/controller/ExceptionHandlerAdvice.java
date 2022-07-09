package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleException(ValidationException e) {
        log.error(e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(ServerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleServerException(ServerException e) {
        log.error(e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Throwable e) {
        log.error(e.getLocalizedMessage());
        return e.getMessage();
    }
}
