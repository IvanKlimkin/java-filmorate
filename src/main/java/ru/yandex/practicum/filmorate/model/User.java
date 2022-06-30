package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class User {
    private Integer id;
    @NotNull(message = "Email не должен быть пустым")
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Email должен быть корректным")
    private final String email;
    @NotNull
    @NotBlank(message = "Логин не должен быть пустым")
    private final String login;
    private String name;
    @PastOrPresent(message = "День рожденья должен быть раньше текущей даты")
    private final LocalDate birthday;
}
