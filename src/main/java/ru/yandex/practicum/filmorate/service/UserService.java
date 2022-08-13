package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("DB realisation") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User createUser(User user) {
        User validUser = validate(user);
        validUser = userStorage.createUser(validUser);
        return validUser;
    }

    private User validate(User user) {
        if (StringUtils.containsWhitespace(user.getLogin())) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return user;
    }

    public User updateUser(User user) {
        User validUser = validate(user);
        getUser(user.getId());
        userStorage.updateUser(validUser);
        return validUser;
    }

    public User getUser(Integer id) {
        return userStorage.getUserByID(id).orElseThrow(
                () -> new ServerException(String.format("Пользователь с ID=%d не найден",
                        id)));
    }

    public void deleteUser(Integer Id) {
        userStorage.deleteUser(userStorage.getUserByID(Id).orElseThrow(
                () -> new ServerException(String.format("Пользователь с ID=%d не найден",
                        Id))));
    }

}
