package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Film {
    private Integer id = 0;
    @NotBlank(message = "Необходимо задать имя")
    private String name;
    @NotBlank(message = "Необходимо задать описание")
    @Size(max = 200, message = "Слишком длинное описание фильма(Не более 200 символов)")
    private String description;
    private LocalDate releaseDate;
    @PositiveOrZero(message = "Продолжительность должна быть положительной")
    private Integer duration;
    @NotNull
    private Mpa mpa;
    private Set<Genre> genres;

    public Film(int filmID, String name, String description, LocalDate releaseDate, int duration, Mpa mpa) {
        this.id = filmID;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
    }
}
