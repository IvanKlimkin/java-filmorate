package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Component
public interface UserStorage {
    List<User> getAllUsers();

    User getUserByID(Integer ID);

    void createUser(User user);

    void updateUser(User user);

    void deleteUser(User user);
}
