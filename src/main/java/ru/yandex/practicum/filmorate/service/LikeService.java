package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeStorage likeStorage;
    private final FilmGenreStorage filmGenreStorage;

    public void addLike(Integer filmID, Integer userID) {
        likeStorage.addLike(filmID, userID);
    }

    public void deleteLike(Integer filmID, Integer userID) {
        likeStorage.deleteLike(filmID, userID);
    }

    public List<Film> getMostLikedFilms(Integer count) {
        List<Film> films = likeStorage.getLikedUsersID(count);
        filmGenreStorage.loadFilmGenres(films);
        return films;
    }
}
