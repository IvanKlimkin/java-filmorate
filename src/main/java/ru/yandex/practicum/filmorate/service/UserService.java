package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;
    private int id = 0;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User createUser(User user) {
        User validUser = validate(user);
        validUser.setId(++id);
        userStorage.createUser(validUser);
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

    public void deleteUser(User user) {
        userStorage.deleteUser(user);
    }

    public void addToFriends(Integer userID, Integer friendID) {
        getUser(friendID).addFriend(userID);
        getUser(userID).addFriend(friendID);
    }

    public void deleteFromFriends(Integer userID, Integer friendID) {
        getUser(userID).deleteFriend(friendID);
        getUser(friendID).deleteFriend(userID);
    }

    public List<User> getCommonFriends(Integer userID, Integer otherID) {
        List<User> friendsName1 = getUserFriends(userID);
        List<User> friendsName2 = getUserFriends(otherID);
        return friendsName1.stream()
                .filter(p -> friendsName2.contains(p) == true)
                .collect(Collectors.toList());

    }

    public List<User> getUserFriends(Integer userID) {
        return getUser(userID).getFriends().stream()
                .map(p -> getUser(p))
                .collect(Collectors.toList());
    }
}
