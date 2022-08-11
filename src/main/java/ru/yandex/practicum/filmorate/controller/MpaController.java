package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/mpa")
@Validated
public class MpaController {
    private final JdbcTemplate jdbcTemplate;

    public MpaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<Mpa> findAll() {
        String sql = "select MPA.* from MPA";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> this.createMpa(rs)));
    }

    @GetMapping("/{id}")
    public Mpa getMpa(@PathVariable Integer id) {
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
