package ru.yandex.practicum.filmorate;

import lombok.Builder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Builder
public class UserForTest {
    public final int id;
    public final String email;
    public final String login;
    public final String name;
    public final LocalDate birthday;
}
