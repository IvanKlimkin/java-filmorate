package ru.yandex.practicum.filmorate;

import lombok.Builder;

import java.time.LocalDate;

@Builder
class FilmForTest {
    public final Integer id;
    public final String name;
    public final String description;
    public final LocalDate releaseDate;
    public final int duration;
}