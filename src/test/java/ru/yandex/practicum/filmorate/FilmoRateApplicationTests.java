package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmoRateApplicationTests {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    LocalDate TestDate = LocalDate.of(1990, 9, 9);
    User user1 = new User(null, "proba@email.com", "Colombo", TestDate, "Valera");
    User user2 = new User(null, "peace@email.com", "Login", TestDate, "Julia");
 /*   Film film1 = new Film(0,
            "Pirates", "Adventure film", TestDate, 123, new Mpa(1, "Комедия"));
    Film film1Upd = new Film(1,
            "Pirates",
            "Adventure film, to be continued",
            TestDate,
            123,
            new Mpa(1, "Комедия"));*/

    @Test
    void testFindUserById() {
        userDbStorage.createUser(user1);
        userDbStorage.createUser(user2);
        Optional<User> userOptional1 = userDbStorage.getUserByID(1);
        Optional<User> userOptional2 = userDbStorage.getUserByID(2);

        List<User> users = userDbStorage.getAllUsers();
        assertThat(userOptional1)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue(
                                "id", 1)
                )
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue(
                                "email", "proba@email.com")
                );
        assertThat(userOptional2)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue(
                                "id", 2)
                )
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue(
                                "email", "peace@email.com")
                )
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue(
                                "name", "Julia"));
        assertThat(users.size()).isEqualTo(2);

    }
/*
    @Test
    void testFindFilmById() {
        filmDbStorage.createFilm(film1);

        Optional<Film> filmOptional = filmDbStorage.getFilmByID(1);
        filmDbStorage.updateFilm(film1Upd);
        List<Film> list1 = filmDbStorage.getAllFilms();
        Optional<Film> filmOptional2 = filmDbStorage.getFilmByID(1);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue(
                                "id", 1)
                )
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue(
                                "description", "Adventure film")
                );
        assertThat(filmOptional2)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue(
                                "id", 1)
                )
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue(
                                "description", "Adventure film, to be continued")
                );
        assertThat(list1.size()).isEqualTo(1);
    }
*/

}
