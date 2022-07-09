package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new TreeMap<>();

    @Override
    public List<User> getAllUsers() {
        List<User> userVal = new ArrayList<>();
        for (User user : users.values()) {
            userVal.add(user);
        }
        log.debug("Количество пользователей {}", userVal.size());
        return userVal;
    }

    @Override
    public User getUserByID(Integer ID) {
        return users.values().stream()
                .filter(p -> p.getId().equals(ID))
                .findFirst()
                .orElseThrow(() -> new ServerException(String.format("Пользователь с ID=%d не найден",
                        ID)));
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
