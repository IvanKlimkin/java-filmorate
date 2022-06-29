package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
    @Slf4j
    @RequestMapping("/users")
    public class UserController {

        private Map<Integer,User> users = new TreeMap<>();
        private int id = 0;

        @GetMapping
        public List<User> findAll() {
            List <User>userVal = new ArrayList<>();
            for(User user : users.values()){
                userVal.add(user);
            }
            log.debug("Количество пользователей {}",userVal.size());
            return userVal;
        }

        @PostMapping
        public User create(@RequestBody @Valid User user)throws ValidationException {
            User validUser = validate(user);
            validUser.setId(++id);
            users.put(validUser.getId(), validUser);
            log.debug("Сохранен пользователь {}", validUser.getName());
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

    @PutMapping
    public User update(@RequestBody @Valid User user) throws ValidationException {
        User validUser = validate(user);
        if (!users.containsKey(validUser.getId())) {
            throw new ServerException("Пользователя с таким ID не найдено");
        }
        users.put(validUser.getId(), validUser);
        log.debug("Сохранен пользователь {}", user.getName());
        return user;
    }

}
