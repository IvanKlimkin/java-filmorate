package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> findAll() {
        String sql = "select MPA.* from MPA";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> this.createMpa(rs)));
    }

    public Mpa getMpa(Integer id) {
        String sql = "select MPA.* from MPA where MPA_ID=?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> this.createMpa(rs), id);
        } catch (EmptyResultDataAccessException e) {
            throw new ServerException("Mpa с таким ID отсутствует");
        }
    }

    private Mpa createMpa(ResultSet rs) throws SQLException {
        return new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME"));
    }
}
