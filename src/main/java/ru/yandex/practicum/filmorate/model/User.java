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
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class User {
    @NotNull(message = "Email не должен быть пустым")
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Email должен быть корректным")
    private final String email;
    @NotNull
    @NotBlank(message = "Логин не должен быть пустым")
    private final String login;
    @PastOrPresent(message = "День рождения должен быть раньше текущей даты")
    private final LocalDate birthday;
    private Integer id;
    private String name;
    private Set<Integer> friends;

    public void addFriend(Integer friendID) {
        if (friends != null) {
            friends.add(friendID);
        } else {
            friends = new HashSet<>();
            friends.add(friendID);
        }
    }

    public void deleteFriend(Integer friendID) {
        if (friends != null) {
            friends.remove(friendID);
        }
    }

    public Set<Integer> getFriends() {
        if (friends != null) {
            return friends;
        } else return Set.of();
    }

}
