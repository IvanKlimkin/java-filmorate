package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new TreeMap<>();

    @Override
    public List<Film> getAllFilms() {
        List<Film> filmNames = new ArrayList<>();
        for (Film film : films.values()) {
            filmNames.add(film);
        }
        log.debug("Количество фильмов {}", filmNames.size());
        return filmNames;
    }

    @Override
    public Film getFilmByID(Integer ID) {
        return films.values().stream()
                .filter(p -> p.getId().equals(ID))
                .findFirst()
                .orElseThrow(() -> new ServerException(String.format("Фильм с ID=%d не найден",
                        ID)));
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

}
