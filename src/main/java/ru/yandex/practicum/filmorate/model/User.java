package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
    private Integer id = 0;
    @NotNull(message = "Email не должен быть пустым")
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Email должен быть корректным")
    private String email;
    @NotNull
    @NotBlank(message = "Логин не должен быть пустым")
    private String login;
    @PastOrPresent(message = "День рождения должен быть раньше текущей даты")
    private LocalDate birthday;
    private String name;
}
