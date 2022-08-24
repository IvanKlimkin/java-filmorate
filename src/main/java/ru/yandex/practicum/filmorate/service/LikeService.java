package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmParameterStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeStorage likeStorage;
    private final FilmParameterStorage FilmParameterStorage;
    private final UserStorage userStorage;

    public void addLike(Integer filmID, Integer userID, Integer rate) {
        likeStorage.addLike(filmID, userID, rate);
        userStorage.addEvent(userID,filmID,"LIKE","ADD");
    }

    public void deleteLike(Integer filmID, Integer userID) {
        likeStorage.deleteLike(filmID, userID);
        userStorage.addEvent(userID,filmID,"LIKE","REMOVE");
    }

    public List<Film> getMostLikedFilms(Integer count) {
        List<Film> films = likeStorage.getLikedUsersID(count);
        FilmParameterStorage.loadFilmParameters(films);
        return films;
    }


}
