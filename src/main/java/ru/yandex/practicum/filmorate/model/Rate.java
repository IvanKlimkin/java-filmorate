package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class Rate {
    private Integer filmId;
    private Integer rate;
}
