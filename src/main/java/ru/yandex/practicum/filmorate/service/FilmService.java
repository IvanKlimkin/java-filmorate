package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmParameterStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static final LocalDate START_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final FilmParameterStorage filmParameterStorage;
    private final DirectorService directorService;

    public FilmService(@Qualifier("DB realisation") FilmStorage filmStorage,
                       FilmParameterStorage filmParameterStorage, DirectorService directorService) {
        this.filmStorage = filmStorage;
        this.filmParameterStorage = filmParameterStorage;
        this.directorService = directorService;
    }

    public Collection<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        return filmParameterStorage.loadFilmParameters(films);
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
        filmStorage.updateFilm(film);
        filmParameterStorage.setFilmParameter(film);
        return film;
    }

    public Film getFilm(Integer id) {
        Film film = filmStorage.getFilmByID(id).orElseThrow(
                () -> new ServerException(String.format("Фильм с ID=%d не найден", id)));
        return filmParameterStorage.loadFilmParameters(Collections.singletonList(film)).get(0);
    }

    public void deleteFilm(Integer Id) {
        filmStorage.deleteFilm(filmStorage.getFilmByID(Id).orElseThrow(
                () -> new ServerException(String.format("Фильм с ID=%d не найден", Id))));
    }

    public List<Film> getSortedFilms(Integer directorId, String sort) {
            List<Film> filteredFilms = filmStorage.getSortedFilms(
                    directorService.getDirectorById(directorId), sort);
            filmParameterStorage.loadFilmParameters(filteredFilms);
        if (sort.equals("year")) {
            return filteredFilms.stream()
                    .sorted(Comparator.comparing(Film::getReleaseDate))
                    .collect(Collectors.toList());
        }
        else return filteredFilms;
    }
    public List<Film> searchFilms(String query, String params) {
        String lowerQuery = query.toLowerCase();
        List<Film> films = filmStorage.searchFilms(lowerQuery,params);
        filmParameterStorage.loadFilmParameters(films);
        return films;
    }
}
