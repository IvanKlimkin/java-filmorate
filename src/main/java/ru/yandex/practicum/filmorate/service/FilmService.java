package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmParameterStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static final LocalDate START_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final FilmParameterStorage filmParameterStorage;
    private final DirectorService directorService;
    private final LikeStorage likeStorage;


    public FilmService(@Qualifier("DB realisation") FilmStorage filmStorage,
                       FilmParameterStorage filmParameterStorage,
                       DirectorService directorService,
                       LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.filmParameterStorage = filmParameterStorage;
        this.directorService = directorService;
        this.likeStorage = likeStorage;
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
        } else return filteredFilms;
    }

    public List<Film> getMostPopularFilms(int genreId, int year, int limit) {
        List<Film> films;
        if (genreId == 0 && year == 0) {
            films = likeStorage.getLikedUsersID(limit);
        } else if (genreId == 0 && year != 0) {
            //фильмы по всем жанрам и по заданному году
            films = filmStorage.getMostPopularFilmsByYear(year, limit);
        } else if (genreId != 0 && year == 0) {
            //фильмы по заданному жанру и по всем годам
            films = filmStorage.getMostPopularFilmsByGenre(genreId, limit);
        } else {
            //фильмы по заданному жанру и по заданному году
            films = filmStorage.getMostPopularFilmsByGenreAndYear(genreId, year, limit);
        }
        return filmParameterStorage.loadFilmParameters(films);
    }


    /**
     * Получить общие фильмы с другом
     *
     * @param userId   - идентификатор пользователя
     * @param friendId - идентификатор друга
     * @return Optional<List < Film>> - список фильмов упорядоченный по количеству лайков (от большего к меньшему)
     */
    public Optional<List<Film>> getSharedFilmsWithFriend(int userId, int friendId) {
        //Получить фильмы, которые нравятся пользователю и другу пользователя
        List<Integer> filmsThatUserLikes = getFilmsThatUserLikes(userId).get();
        List<Integer> filmsThatFriendLikes = getFilmsThatUserLikes(friendId).get();
        //Получить общие идентификаторы фильмов
        HashMap<Integer, Integer> filmsIdWithNumberOfLikes = getSharedFilms(filmsThatUserLikes, filmsThatFriendLikes);
        //Получить отсортированные по количеству лайков фильмы
        List<Film> sharedFilmOrderedByNumberLikes = getFilmsOrderedByNumberOfLikes(filmsIdWithNumberOfLikes);

        return Optional.of(sharedFilmOrderedByNumberLikes);
    }

    /**
     * Получить фильмы понравившиеся пользователю
     *
     * @param userId - идентификатор пользователя
     * @return Optional<List < Integer>> - возвращает список идентификаторов фильмов понравившихся пользователю
     */
    private Optional<List<Integer>> getFilmsThatUserLikes(int userId) {
        //Функция получения идентификаторов фильмов по идентификатору пользователя (т.е. фильтровация)
        Function<Integer, List<Integer>> filterFilmByUserId = (userID) -> {
            String sqlSelectLikesByUserId = "SELECT film_id FROM likes WHERE user_liked_id = ?";
            List<Integer> filmsId = likeStorage.getJdbcTemplate()
                    .query(sqlSelectLikesByUserId, (rs, rowNum) -> rs.getInt("film_id"), userID);
            return filmsId;
        };
        //Применить фильтр и получить результат
        List<Integer> filmsId = likeStorage.sortingOrFiltering(filterFilmByUserId, userId);
        return Optional.of(filmsId);
    }

    /**
     * Получить группу фильмов которые нравятся и пользователю и другу
     *
     * @param filmsThatUserLikes   - идентификаторы фильмов, которые нравятся пользователю
     * @param filmsThatFriendLikes - идентификаторы фильмов, которые нравятся другу
     * @return HashMap<Integer, Integer> - сопоставление идентификаторов фильмов с количеством лайков у них
     */
    private HashMap<Integer, Integer> getSharedFilms(List<Integer> filmsThatUserLikes, List<Integer> filmsThatFriendLikes) {
        //Получить общие фильмы
        Set<Integer> sharedFilms = new HashSet<>();
        sharedFilms.addAll(filmsThatUserLikes);
        sharedFilms.retainAll(filmsThatFriendLikes);
        //Сопоставить идентификаторы фильмов количеству лайков
        HashMap<Integer, Integer> filmsIdWithNumberOfLikes = new HashMap<>();
        sharedFilms.stream()
                .forEach(filmId -> filmsIdWithNumberOfLikes.put(filmId, getLikesByFilm(filmId).get().size()));
        return filmsIdWithNumberOfLikes;
    }

    /**
     * Получить лайки поставленные фильму
     *
     * @param filmId - идентфикатор фильма
     * @return Optional<List < Integer>> - возвращает список лайков поставленных фильму
     */
    private Optional<List<Integer>> getLikesByFilm(int filmId) {
        //Функция получения идентификаторов пользователей по идентификатору фильма (т.е. фильтрация)
        Function<Integer, List<Integer>> filterFuncByFilmId = (filmIdentifier) -> {
            String sqlSelectLikesByFilmId = "SELECT user_liked_id FROM likes WHERE film_id = ?";
            List<Integer> usersId = likeStorage.getJdbcTemplate()
                    .query(sqlSelectLikesByFilmId, (rs, rowNum) -> rs.getInt("user_liked_id"), filmIdentifier);
            return usersId;
        };
        //Получить группу лайков, применив функцию фильтрации
        List<Integer> likes = likeStorage.sortingOrFiltering(filterFuncByFilmId, filmId);
        return Optional.of(likes);
    }

    /**
     * Получить фильмы упорядоченные по количеству лайков пользователей
     *
     * @param filmsIdWithNumberOfLikes
     * @return
     */
    private List<Film> getFilmsOrderedByNumberOfLikes(HashMap<Integer, Integer> filmsIdWithNumberOfLikes) {
        //Функция сортировки
        Function<HashMap<Integer, Integer>, List<Film>> sortingFunction = (filmIdNumberLike) -> {
            return filmIdNumberLike.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .map(filmId -> filmStorage.getFilmByID(filmId).get())
                    .collect(Collectors.toList());
        };

        //Получить отсортированные по убыванию количества лайков фильмы, применив фнукцию сортировки к отображению
        return likeStorage.sortingOrFiltering(sortingFunction, filmsIdWithNumberOfLikes);
    }

    /**
     * Получить рекомендацию фильмов
     *
     * @param userId - идентификатор пользователя для которого запрашивается рекомендация
     * @return - список рекомендуемых фильмов
     */
    public Optional<List<Film>> getRecommendations(int userId) {
        //Получить сопоставление идентификаторов пользователей с количеством общих, с главным пользователем, фильмов
        Map<Integer, Integer> usersIdSharedFilms = getUsersIdAndNumberSharedFilms(userId);
        //Если главный пользователь не имеет лайкнутых фильмов, рекомендация не выдаётся, т.к. он может смотреть любой фильм
        if (usersIdSharedFilms.size() < 1) {
            return Optional.of(Collections.emptyList());
        }
        //Получить запись пользователя с максимальным числом общих фильмов
        Optional<Map.Entry<Integer, Integer>> userIdWithMaxSharedFilms = usersIdSharedFilms.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue));

        //Получить идентификаторы рекомендуемых фильмов
        List<Integer> recommendedFilmsId = getRecommendedFilmsId(userId, userIdWithMaxSharedFilms.get().getKey());
        //Получить объекты фильмов
        List<Film> recommendedFilmObjects = recommendedFilmsId.stream()
                .map(filmId -> {
                    Film film = getFilm(filmId);
                    filmParameterStorage.setFilmParameter(film);
                    return film;
                })
                .collect(Collectors.toList());
        return Optional.of(recommendedFilmObjects);
    }

    /**
     * Получить сопоставление пользовательского идентификатора и количество общих фильмов с эталонным ползователем
     *
     * @param userId - идентификатор эталонного пользователя
     * @return - сопоставление ид - количество общих фильмов
     */
    private Map<Integer, Integer> getUsersIdAndNumberSharedFilms(int userId) {
        HashMap<Integer, List<Integer>> usersIdToFilmsId = new HashMap<>();
        //Получить сопоставление идентификаторов всех пользователей и понравившихся им фильмов
        getUsersId().stream()
                .forEach(userIdentifier -> usersIdToFilmsId.put(userIdentifier, getFilmsThatUserLikes(userIdentifier).get()));
        //Удалить идентификатор основного пользователя чтобы он не обрабатывался
        usersIdToFilmsId.remove(userId);
        //Получить сопоставление идентификаторов пользователей и количества общих фильмов
        Map<Integer, Integer> usersIdSharedFilms = new HashMap<>();
        usersIdToFilmsId.entrySet().stream()
                .forEach(entry -> {
                    int quantitySharedFilms = getSharedFilms(getFilmsThatUserLikes(userId).get(), entry.getValue()).size();
                    usersIdSharedFilms.put(entry.getKey(), quantitySharedFilms);
                });
        return usersIdSharedFilms;
    }

    /**
     * Получить идентификаторы всех пользователей
     *
     * @return - группа идентификаторов пользователей
     */
    private List<Integer> getUsersId() {
        String sqlSelectUsersId = "SELECT user_liked_id FROM likes";
        //Функция фильтрации пользователей без параметров. Возвращает всех пользователей.
        Function<Integer, List<Integer>> getUsersIdFunc = (emptyValue) -> likeStorage.getJdbcTemplate().query(sqlSelectUsersId, (rs, rowNum) -> rs.getInt("user_liked_id"));
        List<Integer> usersId = likeStorage.sortingOrFiltering(getUsersIdFunc, null);
        return usersId;
    }

    /**
     * Получить идентификаторы рекомендуемых фильмов
     *
     * @param userId           - идентификатор основного пользователя
     * @param anotherFilmLover - идентификатор второго пользователя
     * @return - группа идентификаторов фильмо которые рекомендуются
     */
    List<Integer> getRecommendedFilmsId(Integer userId, Integer anotherFilmLover) {
        //Получить идентификаторы фильмов для пользователя и его коллеги
        List<Integer> userFilmsId = getFilmsThatUserLikes(userId).get();
        List<Integer> anotherFilmLoverFilmsId = getFilmsThatUserLikes(anotherFilmLover).get();

        //Вернуть разницу между фильмами. Т.е. рекомендуемые фильмы.
        if (userFilmsId.size() > anotherFilmLoverFilmsId.size()) {
            userFilmsId.removeAll(anotherFilmLoverFilmsId);
            return userFilmsId;
        } else {
            anotherFilmLoverFilmsId.removeAll(userFilmsId);
            return anotherFilmLoverFilmsId;
        }
    }

}
