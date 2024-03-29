package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

@Component
public interface FilmStorage {
    List<Film> getAllFilms();

    Optional<Film> getFilmByID(Integer Id);

    Film createFilm(Film film);

    void updateFilm(Film film);

    void deleteFilm(Film film);

    List<Film> getSortedFilms(Director director, String sort);

    List<Film> searchFilms(String lowerQuery, String params);

    List<Film> getMostPopularFilmsByYear(int year, int limit);

    List<Film> getMostPopularFilmsByGenre(int genreId, int limit);

    List<Film> getMostPopularFilmsByGenreAndYear(int genreId, int year, int limit);

}
