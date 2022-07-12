package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
public class Film {
    @NotBlank(message = "Необходимо задать имя")
    private final String name;
    @NotBlank(message = "Необходимо задать описание")
    @Size(max = 200, message = "Слишком длинное описание фильма(Не более 200 символов)")
    private final String description;
    private final LocalDate releaseDate;
    @PositiveOrZero(message = "Продолжительность должна быть положительной")
    private final Integer duration;
    private Integer id = 0;
    private List<Integer> likedUsersID = new ArrayList<>();

    public void addLike(Integer filmID) {
        likedUsersID.add(filmID);
    }

    public void deleteLike(Integer userID) {
        likedUsersID.remove(userID);
    }

    public Set<Integer> getLikedUsersID() {
        Set<Integer> likedUniq = new HashSet<>(likedUsersID);
        return likedUniq;
    }
}
