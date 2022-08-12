package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.LikeService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
@Validated
public class FilmController {
    private final FilmService filmService;
    private final LikeService likeService;

    public FilmController(FilmService filmService, LikeService likeService) {
        this.filmService = filmService;
        this.likeService = likeService;
    }

    @GetMapping
    public List<Film> findAll() {
        return (List<Film>) filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Integer id) {
        return filmService.getFilm(id);
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) throws ValidationException {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) throws ValidationException {
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        likeService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void dislikeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        likeService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getMostLikedfilms(@RequestParam(defaultValue = "10") @Positive Integer count) {
        return likeService.getMostLikedFilms(count);
    }

}
