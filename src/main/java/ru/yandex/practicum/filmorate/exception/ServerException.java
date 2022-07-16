package ru.yandex.practicum.filmorate.exception;

import java.util.NoSuchElementException;

public class ServerException extends NoSuchElementException {
    public ServerException(String message) {
        super(message);
    }
}
