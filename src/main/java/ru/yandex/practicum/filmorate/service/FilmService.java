package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static final LocalDate START_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final UserService userService;
    private int id = 0;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film createFilm(Film film) {
        validate(film);
        film.setId(++id);
        filmStorage.createFilm(film);
        return film;
    }

    private void validate(Film film) {
        if (film.getDescription().length() > 199) {
            throw new ValidationException("Слишком длинное описание фильма");
        } else if (film.getReleaseDate().isBefore(START_DATE)) {
            throw new ValidationException("Дата релиза раньше 28 декабря 1895 года");
        }
    }

    public Film updateFilm(Film film) {
        getFilm(film.getId());
        validate(film);
        filmStorage.updateFilm(film);
        return film;
    }

    public Film getFilm(Integer id) {
        return filmStorage.getFilmByID(id).orElseThrow(
                () -> new ServerException(String.format("Фильм с ID=%d не найден", id)));
    }

    public void deleteFilm(Film film) {
        filmStorage.deleteFilm(getFilm(film.getId()));
    }

    public void addLike(Integer userID, Integer filmID) {
        if (userService.getUser(userID) != null) {
            getFilm(filmID).addLike(userID);
        }
    }

    public void deleteLike(Integer userID, Integer filmID) {
        if (userService.getUser(userID) != null)
            getFilm(filmID).deleteLike(userID);
    }

    public List<Film> getMostLikedFilms(Integer count) {
        return filmStorage.getAllFilms().stream()
                .sorted((p0, p1) -> p1.getLikedUsersID().size() - p0.getLikedUsersID().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}
