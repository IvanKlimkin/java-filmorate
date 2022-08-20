package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "reviewId")
public class Review {
    private Integer reviewId = 0;
    @NotBlank(message = "Отзыву необходимо содержимое.")
    @Size(max = 1000, message = "Слишком длинный отзыв, Краткость - сестра таланта :-) (Не более 1000 символов).")
    private String content;
    @NotNull(message = "Положительность/отрицательность отзыва null.")
    private Boolean isPositive;
    @NotNull(message = "Id пользователя в отзыве null.")
    private Integer userId;
    @NotNull(message = "Id фильма в отзыве null.")
    private Integer filmId;
    private int useful = 0;
}
