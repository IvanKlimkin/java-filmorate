package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Component
public interface FilmStorage {
    List<Film> getAllFilms();

    Film getFilmByID(Integer ID);

    void createFilm(Film film);

    void updateFilm(Film film);

    void deleteFilm(Film film);

}
