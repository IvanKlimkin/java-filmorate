package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
@Component
@AllArgsConstructor
public class GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Genre> findAll() {
        String sql = "select GENRES.* from GENRES";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> this.createGenre(rs)));
    }

    @GetMapping("/{id}")
    public Genre getGenre(@PathVariable Integer id) throws ServerException {
        String sql = "select GENRES.* from GENRES where GENRE_ID=?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> this.createGenre(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ServerException("Жанр с таким ID отсутствует");
        }
    }

    private Genre createGenre(ResultSet rs) throws SQLException {
        return new Genre(rs.getInt("GENRE_ID"), rs.getString("GENRE_NAME"));
    }
}
