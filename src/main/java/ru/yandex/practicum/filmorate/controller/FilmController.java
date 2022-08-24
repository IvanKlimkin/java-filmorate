package ru.yandex.practicum.filmorate.controller;

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

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Integer id) {
        filmService.deleteFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable Integer id, @PathVariable Integer userId,
                         @RequestParam (required = false, defaultValue = "6") Integer rate) {
        likeService.addLike(id, userId, rate);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void dislikeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        likeService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(
            @RequestParam(defaultValue = "10") @Positive Integer count,
            @RequestParam(defaultValue = "0") int genreId,
            @RequestParam(defaultValue = "0") int year) {
        return filmService.getMostPopularFilms(genreId, year, count);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getSortedFilms(@PathVariable Integer directorId, @RequestParam String sortBy) {
        return filmService.getSortedFilms(directorId, sortBy);
    }

    @GetMapping("/common")
    public List<Film> getSharedFilmsWithFriend(@RequestParam int userId, @RequestParam int friendId) {
        return filmService.getSharedFilmsWithFriend(userId, friendId).get();
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam(name = "query") String query, @RequestParam(name = "by", required = false) String params) {
        return filmService.searchFilms(query,params);
    }
}
