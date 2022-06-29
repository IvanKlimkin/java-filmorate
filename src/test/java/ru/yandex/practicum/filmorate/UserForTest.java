package ru.yandex.practicum.filmorate;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public class UserForTest {
    public final Integer id;
    public final String email;
    public final String login;
    public final String name;
    public final LocalDate birthday;
}
