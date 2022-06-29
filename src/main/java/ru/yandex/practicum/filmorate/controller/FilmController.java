package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new TreeMap<>();
    private int id = 0;

    @GetMapping
    public List<Film> findAll() {
        List <Film>filmNames = new ArrayList<>();
        for(Film film : films.values()){
            filmNames.add(film);
        }
        log.debug("Количество фильмов {}",filmNames.size());
        return filmNames;
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film)throws ValidationException {
        validate(film);
        film.setId(++id);
        films.put(film.getId(), film);
        log.debug("Добавлен фильм {}", film.getName());
            return film;
    }

    private void validate(Film film) {
        if (film.getName().isEmpty()) {
            throw new ValidationException("Отсутствет название фильма");
        } else if (film.getDescription().length() > 199) {
            throw new ValidationException("Слишком длинное описание фильма");
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза раньше 28 декабря 1895 года");
        }
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) throws ValidationException {
        validate(film);
        if (!films.containsKey(film.getId())) {
            throw new ServerException("Фильм с таким ID не найден");
        }
        films.put(film.getId(), film);
        log.debug("Обновлен фильм {}", film.getName());
        return film;
    }
}
