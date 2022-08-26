package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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

    public List<Film> getAllSortedFilms(Integer count) {
        String sql = "select F.*, MPA_NAME from FILMS as F left join LIKES as L on F.FILM_ID=L.FILM_ID " +
                "JOIN MPA M on M.MPA_ID = F.MPA_ID" +
                " group by F.FILM_ID ORDER BY (AVG(RATING), COUNT(L.USER_LIKED_ID)) DESC LIMIT ?"; //, COUNT(L.USER_LIKED_ID)
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
     * @param function  - функция, которая выполняет фильтрацию и/или сортировку
     * @param parameter - в случае фильтрации параметр по которому происходит фильтрация
     *                  - в случае сортировки сортируемая группа объектов
     * @param <T>       - тип передаваемого значения
     * @param <R>       - тип возвращаемого значения
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
        List<Rate> likedFilmIdsAndRate = getRatesByUserID(userId); // фильмы и оценки, оцененные самим пользователем
        List<Integer> userIdsWithSameRates = new ArrayList<>(); // пользователи с похожими оценками
        try {
            for (Rate rate : likedFilmIdsAndRate) {
                String sql = "select L.USER_LIKED_ID from LIKES L " +
                        "where (L.FILM_ID = ? and L.RATE >= (? - 2) and L.RATE <= (? + 2))";
                userIdsWithSameRates.addAll(jdbcTemplate.queryForList(sql, Integer.class, rate.getFilmId(), rate.getRate(),
                        rate.getRate())); //
            }
            userIdsWithSameRates.remove((Integer) userId); //убрать собственный id
        } catch (EmptyResultDataAccessException e) { // если иных оценок нет или совпадает с оценкой пользователя
            System.out.println(e.getMessage());
            return new ArrayList<>();
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
