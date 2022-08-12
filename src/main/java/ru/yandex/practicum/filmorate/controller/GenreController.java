package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/genres")
public class GenreController {
    private final JdbcTemplate jdbcTemplate;
    private final GenreService genreService;

    public GenreController(JdbcTemplate jdbcTemplate, GenreService genreService) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreService = genreService;
    }

    @GetMapping
    public List<Genre> findAll() {
        return genreService.findAll();
    }
    @GetMapping("/{id}")
    public Genre getGenre(@PathVariable Integer id) {
        return genreService.getGenre(id);
    }

}
