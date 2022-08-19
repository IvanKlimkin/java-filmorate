package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Component
public class LikeStorage {
    private final JdbcTemplate jdbcTemplate;

    public LikeStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addLike(Integer filmID, Integer userID) {
        String sql = "merge into LIKES (FILM_ID,USER_LIKED_ID) values (?,?)";
        jdbcTemplate.update(sql, filmID, userID);
    }

    public void deleteLike(Integer filmID, Integer userID) {
        String sql = "delete from LIKES WHERE FILM_ID=? and USER_LIKED_ID=?";
        int count = jdbcTemplate.update(sql, filmID, userID);
        if (count == 0) throw new ServerException(String.format("Лайк с ID=%d не найден", userID));
    }

    public List<Film> getLikedUsersID(Integer count) {
        String sql = "select F.*,MPA_NAME from FILMS as F left join LIKES as L on F.FILM_ID=L.FILM_ID " +
                "JOIN MPA M on M.MPA_ID = F.MPA_ID" +
                " group by F.FILM_ID  order by COUNT(L.USER_LIKED_ID) DESC LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> FilmDbStorage.makeFilm(rs), count);
    }
}
