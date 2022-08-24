package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Director> findAll() {
        String sql = "select D.* from DIRECTORS D";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> this.createDirector(rs)));
    }

    public Director addDirector(Director director) {
        String sql = "insert into DIRECTORS (DIRECTOR_NAME) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"DIRECTOR_ID"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        director.setId(keyHolder.getKey().intValue());
        log.debug("Сохранен режиссер {}", director.getName());
        return director;
    }

    public void updateDirector(Director director) {
        String sql = "update DIRECTORS set DIRECTOR_ID = ?,DIRECTOR_NAME = ? where DIRECTOR_ID = ?";
        jdbcTemplate.update(sql,
                director.getId(),
                director.getName(),
                director.getId());
    }

    public void deleteDirector(Integer Id) {
        String sql = "delete from DIRECTORS where DIRECTOR_ID=?";
        int count = jdbcTemplate.update(sql, Id);
        if (count == 0) throw new ServerException(String.format("Режиссер с ID=%d не найден", Id));
    }

    public Director getDirectorById(Integer id) throws ServerException {
        String sql = "select D.* from DIRECTORS D where DIRECTOR_ID=?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> this.createDirector(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ServerException("Режиссер с таким ID отсутствует");
        }
    }

    private Director createDirector(ResultSet rs) throws SQLException {
        return new Director(rs.getInt("DIRECTOR_ID"), rs.getString("DIRECTOR_NAME"));
    }
}