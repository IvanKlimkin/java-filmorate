package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Director {
    private Integer id;
    @NotBlank(message = "Необходимо задать имя Режиссера")
    private String name;
}
