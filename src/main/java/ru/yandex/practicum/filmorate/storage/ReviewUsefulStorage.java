package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ReviewUsefulStorage {
    private final JdbcTemplate jdbcTemplate;


    public void addLike(int reviewId, int userId) {
        String sql = "merge into REVIEW_USEFUL (REVIEW_ID, USER_ID, USEFUL) values (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, 1);
    }

    public void addDislike(int reviewId, int userId) {
        String sql = "merge into REVIEW_USEFUL (REVIEW_ID, USER_ID, USEFUL) values (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, -1);
    }

    public void removeLikeOrDislike(int reviewId, int userId) {
        String sql = "delete from REVIEW_USEFUL where REVIEW_ID = ? and USER_ID = ?";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    public int getUsefulByReviewId(Integer reviewId) {
        String sql = "select sum (USEFUL) from REVIEW_USEFUL where REVIEW_ID=?";
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(sql, Integer.class, reviewId), 0);
    }


}
