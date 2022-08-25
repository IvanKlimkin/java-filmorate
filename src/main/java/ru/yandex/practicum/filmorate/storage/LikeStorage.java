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
import java.util.function.Function;

@Slf4j
@Component
public class LikeStorage {
    private final JdbcTemplate jdbcTemplate;

    public LikeStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addLike(Integer filmID, Integer userID, Integer rate) {
        String sql = "merge into LIKES (FILM_ID, USER_LIKED_ID, RATE) values (?, ?, ?)";
        jdbcTemplate.update(sql, filmID, userID, rate);
    }

    public void deleteLike(Integer filmID, Integer userID) {
        String sql = "delete from LIKES WHERE FILM_ID=? and USER_LIKED_ID=?";
        int count = jdbcTemplate.update(sql, filmID, userID);
        if (count == 0) throw new ServerException(String.format("Лайк с ID=%d не найден", userID));
    }

    public List<Film> getLikedUsersID(Integer count) {
        String sql = "select F.*,MPA_NAME from FILMS as F left join LIKES as L on F.FILM_ID=L.FILM_ID " +
                "JOIN MPA M on M.MPA_ID = F.MPA_ID" +
                " group by F.FILM_ID  ORDER BY AVG(RATING), COUNT(L.USER_LIKED_ID) DESC LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> FilmDbStorage.makeFilm(rs), count);
    }

    /**
     * Получить экземпляр jdbcTemplate
     *
     * @return JdbcTemplate - возвращает экземпляр JdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * Обобщённый метод фильтрации или сортировки или того и другого
     *
     * @param function - функция, которая выполняет фильтрацию и/или сортировку
     * @param parameter - в случае фильтрации параметр по которому происходит фильтрация
     *                  - в случае сортировки сортируемая группа объектов
     * @param <T> - тип передаваемого значения
     * @param <R> - тип возвращаемого значения
     * @return -
     */
    public <T, R> R sortingOrFiltering(Function<T, R> function, T parameter) {
        return function.apply(parameter);
    }

    public void updateFilmRatingById(int filmId) {
        String sql = "update FILMS set RATING = " +
                "(select avg(RATE) from LIKES" +
                " where LIKES.FILM_ID = ?)" +
                " where FILM_ID = ?";
        jdbcTemplate.update(sql, filmId, filmId);
        log.debug("Обновлен рейтинг фильма с индексом {}.", filmId);
    }

    public List<Integer> findRecommendedFilmIds(int userId) {
        List<Rate> likedFilmIdsAndRate = getRatesByUserID(userId);
        List<Integer> userIdsWithSameRates = new ArrayList<>();
        for (Rate rate: likedFilmIdsAndRate) {
            String sql = "select USER_ID from USERS U " +
                    "left join LIKES L on U.USER_ID = L.USER_LIKED_ID " +
                    "where (L.FILM_ID = ? and (L.RATE > (? - 2) and L.RATE < (? + 2)))";
            userIdsWithSameRates.add(jdbcTemplate.queryForObject(sql, Integer.class, rate.getFilmId(), rate.getRate(),
                    rate.getRate()));
        }
        if (likedFilmIdsAndRate.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> filmIdsRatedByUsersWithSameTastesButNotLikedByUser = new ArrayList<>();
        for (Integer userIdWithSomeTastes: userIdsWithSameRates) {
            List<Rate> likedFilmIdsAndRateByUserWithSameTastes = getRatesByUserID(userIdWithSomeTastes);
            for (Rate rateUserWithSameTastes: likedFilmIdsAndRateByUserWithSameTastes) {
                for (Rate rateUser: likedFilmIdsAndRate) {
                    if (rateUserWithSameTastes.getFilmId().equals(rateUser.getFilmId())) {
                        likedFilmIdsAndRateByUserWithSameTastes.remove(rateUser);
                    }
                }
            }
            for (Rate rate: likedFilmIdsAndRateByUserWithSameTastes) {
                filmIdsRatedByUsersWithSameTastesButNotLikedByUser.add(rate.getFilmId());
            }
        }
        if (filmIdsRatedByUsersWithSameTastesButNotLikedByUser.isEmpty()) {
            return new ArrayList<>();
        } else return filmIdsRatedByUsersWithSameTastesButNotLikedByUser;
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
}
