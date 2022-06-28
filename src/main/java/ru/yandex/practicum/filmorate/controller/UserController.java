package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.*;

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
                if (users.containsKey(user.getEmail())) {
                    throw new ValidationException("Такой пользователь уже добавлен");
                }
                else if(user.getEmail().equals(null)||user.getEmail().equals("")){
                    throw new ValidationException("Отсутствет адрес электронной почты");
                }
                else if(StringUtils.containsWhitespace(user.getLogin())) {
                    throw new ValidationException("Логин не может содержать пробелы");
                }
                user.setId(++id);
                users.put(user.getId(), user);
                if(user.getName().equals(null) || user.getName().equals("")){
                    log.debug("Сохранен пользователь {}",user.getLogin());
                }
                else log.debug("Сохранен пользователь {}",user.getName());
                return user;
        }

        @PutMapping
        public User createPut(@RequestBody @Valid User user)throws ValidationException {
                if(user.getEmail().equals(null)||user.getEmail().equals("")){
                    throw new ValidationException("Отсутствет адрес электронной почты");
                }
                else if(StringUtils.containsWhitespace(user.getLogin())) {
                    throw new ValidationException("Логин не может быть пустым и содержать пробелы");
                }
                if(!users.containsKey(user.getId())){
                    throw new ServerException("Пользователя с таким ID не найдено");
                }
                users.put(user.getId(), user);
                if(user.getName().equals(null) || user.getName().equals("")){
                    log.debug("Сохранен пользователь {}",user.getLogin());
                }
                else log.debug("Сохранен пользователь {}",user.getName());
                return user;
        }

    }
