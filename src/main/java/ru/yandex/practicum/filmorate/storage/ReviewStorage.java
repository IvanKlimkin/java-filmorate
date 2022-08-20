package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.*;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewStorage {
    private final JdbcTemplate jdbcTemplate;


    public Review createReview(Review review) {
        String sql = "insert into REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL) values (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"REVIEW_ID"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            stmt.setInt(5, review.getUseful());
            return stmt;
        }, keyHolder);
        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        log.debug("Сохранен отзыв от пользователя {} о фильме {}", review.getUserId(), review.getFilmId());
        return review;
    }

    public Review updateReview(Review review) {
        String sql = "update REVIEWS set CONTENT = ?, IS_POSITIVE = ?, USEFUL = ?" +
                " where REVIEW_ID = ?";
        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getUseful(),
                review.getReviewId());
        log.debug("Обновлен отзыв {} у фильма {} от пользователя {}.", review.getReviewId(), review.getFilmId(),
                review.getUserId());
        return review;
    }

    public void deleteReview(int id) {
        String sql = "delete from REVIEWS where REVIEW_ID = ?";
        jdbcTemplate.update(sql, id);
        log.debug("Удален отзыв {}.", id);
    }

    public Review getReview(int id) {
        String sql = "select REVIEW_ID, CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL from REVIEWS " +
                "where REVIEW_ID = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeReview(rs), id);
    }

    public List<Review> getAllReviewsSortedByPopularity() {
        String sql = "select REVIEW_ID, CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL from REVIEWS " +
                "order by USEFUL DESC, REVIEW_ID";
        return new ArrayList<>(jdbcTemplate.query(sql, ((rs, rowNum) -> makeReview(rs))));
    }

    public List<Integer> getAllUserIds() {
        String sql = "select USER_ID from USERS";
        return jdbcTemplate.queryForList(sql, Integer.class);
    }

    public List<Integer> getAllFilmIds() {
        String sql = "select FILM_ID from FILMS";
        return jdbcTemplate.queryForList(sql, Integer.class);
    }

    public List<Integer> getAllReviewIds() {
        String sql = "select REVIEW_ID from REVIEWS";
        return jdbcTemplate.queryForList(sql, Integer.class);
    }

    public void updateReviewUseful(int useful, int reviewId) {
        String sql = "update REVIEWS set USEFUL = ? where REVIEW_ID = ?";
        jdbcTemplate.update(sql, useful, reviewId);
        log.debug("Обновлена полезность отзыва {}.", reviewId);
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        return new Review(rs.getInt("REVIEW_ID"),
                rs.getString("CONTENT"),
                rs.getBoolean("IS_POSITIVE"),
                rs.getInt("USER_ID"),
                rs.getInt("FILM_ID"),
                rs.getInt("USEFUL"));
    }
}
