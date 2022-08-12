package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.FilmGenreStorage;

import java.time.LocalDate;
import java.util.*;

@Service
public class FilmService {
    private static final LocalDate START_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final FilmGenreStorage filmGenreStorage;

    public FilmService(@Qualifier("DB realisation") FilmStorage filmStorage,
                       FilmGenreStorage filmGenreStorage) {
        this.filmStorage = filmStorage;
        this.filmGenreStorage = filmGenreStorage;
    }

    public Collection<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        return filmGenreStorage.loadFilmGenres(films);
    }
//Film genre storage!!!!!!!!!!!!!!!!!!!!!!!!!!
    public Film createFilm(Film film) {
        validate(film);
        film = filmStorage.createFilm(film);
        filmGenreStorage.setFilmGenre(film);
        return film;
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(START_DATE)) {
            throw new ValidationException("Дата релиза раньше 28 декабря 1895 года");
        }
    }

    public Film updateFilm(Film film) {
        getFilm(film.getId());
        validate(film);
        filmStorage.updateFilm(film);
        filmGenreStorage.setFilmGenre(film);
        return film;
    }

    public Film getFilm(Integer id) {
        Film film = filmStorage.getFilmByID(id).orElseThrow(
                () -> new ServerException(String.format("Фильм с ID=%d не найден", id)));
        return filmGenreStorage.loadFilmGenres(Collections.singletonList(film)).get(0);
    }

    public void deleteFilm(Film film) {
        filmStorage.deleteFilm(film);
    }

}
