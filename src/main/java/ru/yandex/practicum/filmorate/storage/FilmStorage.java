package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> getAllFilms();

    Film getFilmByID(Integer ID);

    void createFilm(Film film);

    void updateFilm(Film film);

    void deleteFilm(Film film);

}
