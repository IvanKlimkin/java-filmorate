package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    private final Map<String, Film> films = new TreeMap<>();
    int id = 0;

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
            if (films.containsKey(film.getName())) {
                throw new ValidationException("Такой фильм уже добавлен");
            }
            else if(film.getName().equals(null)||film.getName().equals("")){
                throw new ValidationException("Отсутствет название фильма");
            }
            else if(film.getDescription().length()>199){
                throw new ValidationException("Слишком длинное описание фильма");
            }
            else if(film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))){
                throw new ValidationException("Дата релиза раньше 28 декабря 1895 года");
            }
            film.setId(1);
            films.put(film.getName(), film);
            log.debug("Добавлен фильм {}",film.getName());
            return film;
    }

    @PutMapping
    public Film createPut(@RequestBody @Valid Film film)throws ValidationException {
            if(film.getName().equals(null)||film.getName().equals("")){
                throw new ValidationException("Отсутствет название фильма");
            }
            else if(film.getDescription().length()>199){
                throw new ValidationException("Слишком длинное описание фильма");
            }
            else if(film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))){
                throw new ValidationException("Дата релиза раньше 28 декабря 1895 года");
            }
            films.put(film.getName(), film);
            log.debug("Обновлен фильм {}",film.getName());
            return film;
    }


}
