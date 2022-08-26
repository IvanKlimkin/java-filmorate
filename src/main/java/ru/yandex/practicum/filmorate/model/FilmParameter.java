package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FilmParameter {
    private Integer filmId;
    private Integer parameterId;
    private String parameterName;
    private Integer filmParameterNum;
}
