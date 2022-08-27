package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmParameterStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static final LocalDate START_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final FilmParameterStorage filmParameterStorage;
    private final DirectorService directorService;
    private final LikeStorage likeStorage;


    public FilmService(@Qualifier("DB realisation") FilmStorage filmStorage,
                       FilmParameterStorage filmParameterStorage,
                       DirectorService directorService,
                       LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.filmParameterStorage = filmParameterStorage;
        this.directorService = directorService;
        this.likeStorage = likeStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film createFilm(Film film) {
        validate(film);
        film = filmStorage.createFilm(film);
        filmParameterStorage.setFilmParameter(film);
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
        filmParameterStorage.setFilmParameter(film);
        filmStorage.updateFilm(film);
        return film;
    }

    public Film getFilm(Integer id) {
        Film film = filmStorage.getFilmByID(id).orElseThrow(
                () -> new ServerException(String.format("Фильм с ID=%d не найден", id)));
        return Collections.singletonList(film).get(0);
    }

    public void deleteFilm(Integer Id) {
        filmStorage.deleteFilm(filmStorage.getFilmByID(Id).orElseThrow(
                () -> new ServerException(String.format("Фильм с ID=%d не найден", Id))));
    }

    public List<Film> getSortedFilms(Integer directorId, String sort) {
        List<Film> filteredFilms = filmStorage.getSortedFilms(
                directorService.getDirectorById(directorId), sort);
        if (sort.equals("year")) {
            return filteredFilms.stream()
                    .sorted(Comparator.comparing(Film::getReleaseDate))
                    .collect(Collectors.toList());
        } else return filteredFilms;
    }

    public List<Film> getMostPopularFilms(Integer genreId, Integer year, Integer limit) {
        List<Film> films;
        if (genreId == 0 && year == 0) {
            films = likeStorage.getAllSortedFilms(limit);
        } else if (genreId == 0) {
            //фильмы по всем жанрам и по заданному году
            films = filmStorage.getMostPopularFilmsByYear(year, limit);
        } else if (year == 0) {
            //фильмы по заданному жанру и по всем годам
            films = filmStorage.getMostPopularFilmsByGenre(genreId, limit);
        } else {
            //фильмы по заданному жанру и по заданному году
            films = filmStorage.getMostPopularFilmsByGenreAndYear(genreId, year, limit);
        }
        return films;
    }

    public Optional<List<Film>> getSharedFilmsWithFriend(int userId, int friendId) {
        List<Film> films = filmStorage.getSharedFilmsWithFriend(userId, friendId);
        return Optional.of(films);
    }

    public List<Film> searchFilms(String query, String params) {
        return filmStorage.searchFilms(query, params);
    }
}
