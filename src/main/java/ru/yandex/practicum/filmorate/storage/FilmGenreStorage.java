package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FilmGenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public void setFilmGenre(Film film) {
        String sql = "delete from FILM_GENRE where FILM_ID = ?";
        jdbcTemplate.update(sql, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        for (Genre genre : film.getGenres()) {
            String sqlQuery = "INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) values (?,?)";
            jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());
        }
    }

    public List<Film> loadFilmGenres(List<Film> films) {
        for (Film film : films) {
            int filmId = film.getId();
            String sqlQuery = "SELECT GENRE_ID, GENRE_NAME " +
                    "FROM GENRES " +
                    "WHERE GENRE_ID IN (SELECT GENRE_ID " +
                    "FROM FILM_GENRE " +
                    "WHERE FILM_ID = ?)";
            List<Genre> genres = jdbcTemplate.query(sqlQuery, this::createGenre, filmId);
            genres.sort(Comparator.comparing(Genre::getId));
            film.setGenres(new HashSet<>(genres));
        }
        return films;
    }

    private FilmGenre createFilmGenre(ResultSet rs) throws SQLException {
        return new FilmGenre(
                rs.getInt("FILM_ID"),
                rs.getInt("GENRE_ID"),
                rs.getString("GENRE_NAME"));
    }

    private Genre createGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(
                rs.getInt("genre_id"),
                rs.getString("genre_name"));
    }

}
