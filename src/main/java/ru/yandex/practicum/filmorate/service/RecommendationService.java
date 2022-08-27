package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmParameterStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final LikeStorage likeStorage;
    private final FilmStorage filmStorage;

    public List<Film> getRecommendations(int userId) {
        List<Integer> recommendedFilmIds = likeStorage.findRecommendedFilmIds(userId); // получаем не сортированные id фильмов
        if (recommendedFilmIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Film> recommendedFilms = new ArrayList<>();
        for (Integer filmId : recommendedFilmIds) { // получаем по ним не сортированные фильмы
            recommendedFilms.add(filmStorage.getFilmByID(filmId).get());
        }
        // сортируем
        Comparator<Film> sortByRate = (o1, o2) -> {
            if (o1.getRating() > o2.getRating()) {
                return 1;
            } else if (o1.getRating() < o2.getRating()) {
                return -1;
            } else {
                return o2.getId() - o1.getId();
            }
        };
        return recommendedFilms.stream().sorted(sortByRate).collect(Collectors.toList()); // сортируем
    }
}
