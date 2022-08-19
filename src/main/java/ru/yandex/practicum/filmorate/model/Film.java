package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
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
    private Set<Director> directors;

}
