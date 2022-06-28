package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
public class User {
    private Integer id;
    @NotNull @NotBlank(message = "Email не должен быть пустым") @Email(message = "Email должен быть корректным")
    private final String email;
    @NotNull @NotBlank(message = "Логин не должен быть пустым")
    private final String login;
    private final String name;
    @PastOrPresent(message = "День рожденья должен быть раньше текущей даты")
    private final LocalDate birthday;
}
