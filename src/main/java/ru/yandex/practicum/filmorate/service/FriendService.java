package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendStorage friendStorage;
    private final UserStorage userStorage;

    public void addToFriends(Integer userID, Integer friendID) {
        userStorage.getUserByID(userID).orElseThrow(
                () -> new ServerException(String.format("Пользователь с ID=%d не найден",
                        userID)));
        userStorage.getUserByID(friendID).orElseThrow(
                () -> new ServerException(String.format("Пользователь с ID=%d не найден",
                        friendID)));
        friendStorage.addFriend(userID, friendID);
        userStorage.addEvent(userID,friendID,"FRIEND","ADD");
    }

    public void deleteFromFriends(Integer userID, Integer friendID) {
        friendStorage.deleteFriend(userID, friendID);
            userStorage.addEvent(userID,friendID,"FRIEND","REMOVE");
    }

    public List<User> getCommonFriends(Integer userID, Integer otherID) {
        return friendStorage.getCommonFriends(userID, otherID);
    }

    public List<User> getUserFriends(Integer userID) {
        userStorage.getUserByID(userID).orElseThrow(
                () -> new ServerException(String.format("Пользователь с ID=%d не найден",
                        userID)));
        return friendStorage.getUserFriends(userID);
    }
}
