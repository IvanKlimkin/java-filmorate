package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FilmDirector {
    private Integer filmId;
    private Integer directorId;
    private String directorName;
}
