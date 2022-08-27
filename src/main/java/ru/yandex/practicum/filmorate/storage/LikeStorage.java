package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@Slf4j
@Component
public class LikeStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmDbStorage filmDbStorage;

    public LikeStorage(JdbcTemplate jdbcTemplate, FilmDbStorage filmDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmDbStorage = filmDbStorage;
        //todo прошу простить за эту инъекцию, чисто для скорости, makeFilm static не получилось, jdbcTemplate не дает
    }

    public void addLike(Integer filmID, Integer userID, Integer rate) {
        int positivity;
        if (rate > 5) {
            positivity = 1;
        } else {
            positivity = -1;
        }
        String sql = "merge into LIKES (FILM_ID, USER_LIKED_ID, RATE, POSITIVITY) values (?, ?, ?, ?)";
        jdbcTemplate.update(sql, filmID, userID, rate, positivity);
        updateFilmRatingAndCountsById(filmID);
    }

    public void deleteLike(Integer filmID, Integer userID) {
        String sql = "delete from LIKES WHERE FILM_ID=? and USER_LIKED_ID=?";
        int count = jdbcTemplate.update(sql, filmID, userID);
        if (count == 0) throw new ServerException(String.format("Лайк с ID=%d не найден", userID));
        updateFilmRatingAndCountsById(filmID);
    }

    public List<Film> getAllSortedFilms(Integer count) {
        String sql = "select F.*, MPA_NAME from FILMS as F " +
                "JOIN MPA M on M.MPA_ID = F.MPA_ID " +
                "ORDER BY (RATING, " +
                "COUNT_POSITIVE, " +
                "COUNT_NEGATIVE) DESC" +
                " LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> filmDbStorage.makeFilm(rs), count);
    }

    public List<Integer> findRecommendedFilmIds(int userId) {
        List<Rate> likedFilmIdsAndRate = getRatesByUserID(userId); // фильмы и оценки, оцененные самим пользователем
        List<Integer> userIdsWithSameRates = new ArrayList<>(); // пользователи с похожими оценками
        for (Rate rate : likedFilmIdsAndRate) {
            String sql = "select L.USER_LIKED_ID from LIKES L " +
                    "where (L.FILM_ID = ? and L.RATE >= (? - 2) and L.RATE <= (? + 2) and L.RATE > 5)";
            userIdsWithSameRates.addAll(jdbcTemplate.queryForList(sql, Integer.class, rate.getFilmId(), rate.getRate(),
                    rate.getRate())); //
            userIdsWithSameRates.remove((Integer) userId); //убрать собственный id

            if (userIdsWithSameRates.isEmpty()) {
                return new ArrayList<>();
            }
        }
        List<Integer> filmIdsRatedByUsersWithSameTastesButNotLikedByUser = new ArrayList<>();
        for (Integer userIdWithSomeTastes : userIdsWithSameRates) {
            List<Rate> likedFilmIdsAndRateByUserWithSameTastes = getRatesByUserID(userIdWithSomeTastes);
            //фильмы и оценки, оцененные одним из пользователей с похожими оценками
            ListIterator<Rate> iterator = likedFilmIdsAndRateByUserWithSameTastes.listIterator();
            while (iterator.hasNext()) {
                for (Rate rateUser : likedFilmIdsAndRate) {
                    if (iterator.next().getFilmId().equals(rateUser.getFilmId())) {
                        iterator.remove();
                        // удаляем фильмы и оценки, которые он сам уже оценил
                    }
                }
            }
            for (Rate rate : likedFilmIdsAndRateByUserWithSameTastes) {
                filmIdsRatedByUsersWithSameTastesButNotLikedByUser.add(rate.getFilmId());
                // складываем оставшиеся id фильмов, т.е. те которые оценил другой пользователь, но не оценил сам
            }
        }
        if (filmIdsRatedByUsersWithSameTastesButNotLikedByUser.isEmpty()) {
            return new ArrayList<>();
        } else return filmIdsRatedByUsersWithSameTastesButNotLikedByUser;
    }

    public List<Film> newFindRecommendedFilmIds(int userId) {

        String sql = "select F.* from FILMS F " +
                "left join LIKES L on F.FILM_ID = L.FILM_ID " +
                "where L.USER_LIKED_ID in " +
                "   (select L.USER_LIKED_ID from LIKES L " +
                "   where L.FILM_ID in " +
                "       ())";

        return new ArrayList<>(jdbcTemplate.query(sql, (rs, rowNum) -> filmDbStorage.makeFilm(rs), userId));
    }

    private List<Rate> getRatesByUserID(int userId) {
        String sql = "select FILM_ID, RATE from LIKES " +
                "where USER_LIKED_ID = ?";
        return new ArrayList<>(jdbcTemplate.query(sql, (rs, rowNum) -> makeRate(rs), userId));
    }

    private Rate makeRate(ResultSet rs) throws SQLException {
        return new Rate(rs.getInt("FILM_ID"),
                rs.getInt("RATE"));
    }

    private void updateFilmRatingAndCountsById(int filmId) {
        String sqlIsEmpty = "select FILM_ID from LIKES " +
                "where FILM_ID = ?";
        if (jdbcTemplate.queryForList(sqlIsEmpty, filmId).isEmpty()) {
            String sqlUpdate = "update FILMS set RATING = 0.0, " +
                    "COUNT_POSITIVE = 0, " +
                    "COUNT_NEGATIVE = 0 " +
                    "where FILM_ID = ?";
            jdbcTemplate.update(sqlUpdate, filmId);
        }
        String sql = "update FILMS set RATING = " +
                "(select avg(RATE) from LIKES " +
                "where LIKES.FILM_ID = ?), " +
                "COUNT_POSITIVE = " +
                "(select sum(POSITIVITY) from LIKES " +
                "where LIKES.FILM_ID = ? " +
                "and LIKES.POSITIVITY = 1), " +
                "COUNT_NEGATIVE = " +
                "(select sum(POSITIVITY) from LIKES " +
                "where LIKES.FILM_ID = ? " +
                "and LIKES.POSITIVITY = -1) " +
                "where FILM_ID = ?";
        jdbcTemplate.update(sql, filmId, filmId, filmId, filmId);

        log.debug("Обновлен рейтинг фильма с индексом {}.", filmId);
    }
}
