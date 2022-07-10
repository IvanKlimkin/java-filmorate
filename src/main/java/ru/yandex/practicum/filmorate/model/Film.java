package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Film {
    @NotBlank(message = "Необходимо задать имя")
    private final String name;
    @NotBlank(message = "Необходимо задать описание")
    private final String description;
    private final LocalDate releaseDate;
    @PositiveOrZero(message = "Продолжительность должна быть положительной")
    private final Integer duration;
    private Integer id;
    private Set<Integer> likedUsersID;

    public void addLike(Integer filmID) {
        if (likedUsersID != null) {
            likedUsersID.add(filmID);
        } else {
            likedUsersID = new HashSet<>();
            likedUsersID.add(filmID);
        }
    }

    public void deleteLike(Integer userID) {
        if (likedUsersID != null) {
            likedUsersID.remove(userID);
        }
    }

    public Set<Integer> getLikedUsersID() {
        if (likedUsersID != null) {
            return likedUsersID;
        } else return Set.of();
    }
}
