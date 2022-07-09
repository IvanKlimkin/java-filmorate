package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private int id = 0;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilm(Integer id) {
        return filmStorage.getFilmByID(id);
    }

    public Film createFilm(Film film) {
        validate(film);
        film.setId(++id);
        filmStorage.createFilm(film);
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

    public Film updateFilm(Film film) {
        if (Objects.equals(filmStorage.getFilmByID(film.getId()), null)) {
            throw new ServerException(String.format("Фильм с таким ID=%d не найден",
                    film.getId()));
        }
        validate(film);
        filmStorage.updateFilm(film);
        return film;
    }

    public void deleteFilm(Film film) {
        filmStorage.deleteFilm(film);
    }

    public void addLike(Integer userID, Integer filmID) {
        if (filmStorage.getFilmByID(filmID) != null && userStorage.getUserByID(userID) != null)
            filmStorage.getFilmByID(filmID).addLike(userID);
    }

    public void deleteLike(Integer userID, Integer filmID) {
        if (filmStorage.getFilmByID(filmID) != null && userStorage.getUserByID(userID) != null)
            filmStorage.getFilmByID(filmID).deleteLike(userID);
    }

    public List<Film> getMostLikedFilms(Integer count) {
        return filmStorage.getAllFilms().stream()
                .sorted((p0, p1) -> p1.getLikedUsersID().size() - p0.getLikedUsersID().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}
