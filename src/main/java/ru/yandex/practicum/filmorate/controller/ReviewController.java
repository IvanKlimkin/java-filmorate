package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.service.ReviewUsefulService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewUsefulService reviewUsefulService;

    @PostMapping
    public Review create(@RequestBody @Valid Review review) throws ValidationException {
        return reviewService.createReview(review);
    }

    @PutMapping
    public Review update(@RequestBody @Valid Review review) throws ValidationException {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable int id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping()
    public List<Review> getReviews(@RequestParam(required = false, defaultValue = "0") int filmId,
                                   @RequestParam(required = false, defaultValue = "10") @Positive int count) {
        return reviewService.getPopularReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void addLike(@PathVariable int reviewId, @PathVariable int userId) {
        reviewUsefulService.addLike(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDislike(@PathVariable int reviewId, @PathVariable int userId) {
        reviewUsefulService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void removeLike(@PathVariable int reviewId, @PathVariable int userId) {
        reviewUsefulService.removeLikeOrDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void removeDislike(@PathVariable int reviewId, @PathVariable int userId) {
        reviewUsefulService.removeLikeOrDislike(reviewId, userId);
    }
}
