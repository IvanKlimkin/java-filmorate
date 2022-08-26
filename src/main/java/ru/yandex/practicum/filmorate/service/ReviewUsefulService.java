package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.ReviewUsefulStorage;

@Service
@RequiredArgsConstructor
public class ReviewUsefulService {
    private final ReviewUsefulStorage reviewUsefulStorage;
    private final ReviewStorage reviewStorage;


    public void addLike(int reviewId, int userId) {
        checkReviewIdAndUserID(reviewId, userId);
        reviewUsefulStorage.addLike(reviewId, userId);
        updateUsefulOfReview(reviewId);
    }

    public void addDislike(int reviewId, int userId) {
        checkReviewIdAndUserID(reviewId, userId);
        reviewUsefulStorage.addDislike(reviewId, userId);
        updateUsefulOfReview(reviewId);
    }

    public void removeLikeOrDislike(int reviewId, int userId) {
        checkReviewIdAndUserID(reviewId, userId);
        reviewUsefulStorage.removeLikeOrDislike(reviewId, userId);
        updateUsefulOfReview(reviewId);
    }

    private void checkReviewIdAndUserID(int reviewId, int userId) {
        if (!reviewStorage.getAllReviewIds().contains(reviewId)) {
            throw new ServerException(String.format("Отзыва с индексом %d не найдено.", reviewId));
        }
        if (!reviewStorage.getAllUserIds().contains(userId)) {
            throw new ServerException(String.format("Пользователя с индексом %d не найдено.", userId));
        }
    }

    private void updateUsefulOfReview(int reviewId) {
        reviewStorage.updateReviewUseful(reviewUsefulStorage.getUsefulByReviewId(reviewId), reviewId);
    }

}
