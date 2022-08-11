package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
/*
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new TreeMap<>();

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(List.copyOf(films.values()));
    }

    @Override
    public Optional<Film> getFilmByID(Integer ID) {
        return Optional.ofNullable(films.get(ID));
    }

    @Override
    public void createFilm(Film film) {
        films.put(film.getId(), film);
        log.debug("Добавлен фильм {}", film.getName());
    }

    @Override
    public void updateFilm(Film film) {
        films.put(film.getId(), film);
        log.debug("Обновлен фильм {}", film.getName());
    }

    @Override
    public void deleteFilm(Film film) {
        log.debug("Удален фильм {}", film.getName());
        films.remove(film.getId());
    }

}*/
