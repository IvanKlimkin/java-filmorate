package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new TreeMap<>();

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(List.copyOf(users.values()));
    }

    @Override
    public Optional<User> getUserByID(Integer ID) {
        return Optional.ofNullable(users.get(ID));
    }

    @Override
    public void createUser(User user) {
        users.put(user.getId(), user);
        log.debug("Сохранен пользователь {}", user.getName());
    }

    @Override
    public void updateUser(User user) {
        users.put(user.getId(), user);
        log.debug("Обновлен пользователь {}", user.getName());
    }

    @Override
    public void deleteUser(User user) {
        log.debug("Удален пользователь {}", user.getName());
        users.remove(user.getId());
    }
}
