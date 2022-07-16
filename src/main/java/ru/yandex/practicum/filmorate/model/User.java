package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
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
    private Integer id = 0;
    private String name;
    private List<Integer> friends = new ArrayList<>();

    public void addFriend(Integer friendID) {
        friends.add(friendID);
    }

    public void deleteFriend(Integer friendID) {
        friends.remove(friendID);
    }

    public Set<Integer> getFriends() {
        Set<Integer> friendsUniq = new HashSet<>(friends);
        return friendsUniq;
    }

}
