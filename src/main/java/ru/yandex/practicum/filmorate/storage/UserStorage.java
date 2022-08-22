package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Component
public interface UserStorage {
    List<User> getAllUsers();

    Optional<User> getUserByID(Integer ID);

    User createUser(User user);

    void updateUser(User user);

    void deleteUser(User user);

    List<Event> getUserFeed(User orElseThrow);

    void addEvent(Integer userId, Integer entityId, String eventType, String operation);
}
