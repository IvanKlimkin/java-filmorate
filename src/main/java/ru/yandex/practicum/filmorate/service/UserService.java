package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService {
    private UserStorage userStorage;
    private int id = 0;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUser(Integer id) {
        return userStorage.getUserByID(id);
    }

    public User createUser(User user) {
        User validUser = validate(user);
        validUser.setId(++id);
        userStorage.createUser(validUser);
        return validUser;
    }

    private User validate(User user) {
        if (user.getEmail().isEmpty()) {
            throw new ValidationException("Отсутствет адрес электронной почты");
        } else if (StringUtils.containsWhitespace(user.getLogin())) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        return user;
    }

    public User updateUser(User user) {
        User validUser = validate(user);
        if (Objects.equals(userStorage.getUserByID(validUser.getId()), null)) {
            throw new ServerException(String.format("Пользователь с ID=%d не найден",
                    user.getId()));
        }
        userStorage.updateUser(validUser);
        return validUser;
    }

    public void deleteUser(User user) {
        userStorage.deleteUser(user);
    }

    public void addToFriends(Integer userID, Integer friendID) {
        if (userStorage.getUserByID(userID) != null && userStorage.getUserByID(friendID) != null) {
            userStorage.getUserByID(userID).addFriend(friendID);
            userStorage.getUserByID(friendID).addFriend(userID);

        }
    }

    public void deleteFromFriends(Integer userID, Integer friendID) {
        if (userStorage.getUserByID(userID) != null && userStorage.getUserByID(friendID) != null) {
            userStorage.getUserByID(userID).deleteFriend(friendID);
            userStorage.getUserByID(friendID).deleteFriend(userID);
        }
    }

    public List<User> getCommonFriends(Integer userID, Integer otherID) {
        List<User> friendsName1 = getUserFriends(userID);
        List<User> friendsName2 = getUserFriends(otherID);
        return friendsName1.stream()
                .filter(p -> friendsName2.contains(p) == true)
                .collect(Collectors.toList());

    }

    public List<User> getUserFriends(Integer userID) {
        return userStorage.getUserByID(userID).getFriends().stream()
                .map(p -> userStorage.getUserByID(p))
                .collect(Collectors.toList());

    }
}
