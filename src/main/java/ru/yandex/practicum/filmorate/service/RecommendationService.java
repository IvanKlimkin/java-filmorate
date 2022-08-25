package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final LikeStorage likeStorage;
    private final FilmStorage filmStorage;

    public List<Film> getRecommendations(int userId) {
        List<Integer> recommendedFilmIds = likeStorage.findRecommendedFilmIds(userId);
        if (recommendedFilmIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Film> recommendedFilms = new ArrayList<>();
        for (Integer filmId: recommendedFilmIds) {
            recommendedFilms.add(filmStorage.getFilmByID(filmId).get());
        }
        return recommendedFilms;
    }
}
