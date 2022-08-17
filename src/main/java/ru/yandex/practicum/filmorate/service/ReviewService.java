package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.ReviewUsefulStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final ReviewUsefulStorage reviewUsefulStorage;

    public Review createReview(Review review) {
        checkReview(review);
        return reviewStorage.createReview(review);
    }

    public Review updateReview(Review review) {
        checkReview(review);
        updateUsefulOfReview(review);
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(int id) {
        checkReviewId(id);
        reviewStorage.deleteReview(id);
    }

    public Review getReviewById(int id) {
        checkReviewId(id);
        return reviewStorage.getReview(id);
    }

    public List<Review> getPopularReviewsByFilmId(int filmId, int count) {
        List<Review> allReviews = reviewStorage.getAllReviewsSortedByPopularity();
        if (filmId != 0) {
            allReviews = allReviews.stream().filter(review -> review.getFilmId() == filmId).collect(Collectors.toList());
        }
        return allReviews.stream().limit(count).collect(Collectors.toList());
    }

    private void checkReview(Review review) {
        if (!reviewStorage.getAllUserIds().contains(review.getUserId())) {
            throw new ServerException(String.format("В отзыве пользователя с индексом %d не найдено.",
                    review.getUserId()));
        }
        if (!reviewStorage.getAllFilmIds().contains(review.getFilmId())) {
            throw new ServerException(String.format("В отзыве фильма с индексом %d не найдено.",
                    review.getFilmId()));
        }
        for (Review existReview : reviewStorage.getAllReviewsSortedByPopularity()) {
            if (review.getContent().equals(existReview.getContent()) // если аналогичный по содержанию отзыв уже есть
                    & !review.getReviewId().equals(existReview.getReviewId())) { // и это не он сам
                throw new ValidationException(String.format("Аналогичный отзыв уже есть, с индексом: %d",
                        existReview.getReviewId()));
            }
        }
    }

    private void checkReviewId(int id) {
        if (!reviewStorage.getAllReviewIds().contains(id)) {
            throw new ServerException(String.format("Отзыва с индексом %d не найдено.", id));
        }
    }

    private void updateUsefulOfReview(Review review) {
        review.setUseful(reviewUsefulStorage.getUsefulByReviewId(review.getReviewId()));
    }
}
