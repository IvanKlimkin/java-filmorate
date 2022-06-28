package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;


@Builder
@Data
public class Film {
    private Integer id;
    @NotNull @NotBlank(message = "Необходимо задать имя")
    private final String name;
    private final String description;
    private final LocalDate releaseDate;
    @PositiveOrZero(message = "Продолжительность должна быть положительной")
    private final Integer duration;
}
