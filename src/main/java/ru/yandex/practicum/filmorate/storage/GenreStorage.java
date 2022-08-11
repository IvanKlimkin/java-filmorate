package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public void setFilmGenre(Film film) {
        String sql = "delete from FILM_GENRE where FILM_ID = ?";
        jdbcTemplate.update(sql, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String sqlTryBatch = "insert into FILM_GENRE (FILM_ID,GENRE_ID) values(?,?)";
        jdbcTemplate.batchUpdate(sqlTryBatch,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, film.getId());
                        ps.setInt(2, film.getGenres().stream()
                                .collect(Collectors.toList())
                                .get(i)
                                .getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return film.getGenres().size();
                    }
                });
    }

    public List<Film> loadFilmGenres(List<Film> films) {
        if (films.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Integer> ids = films.stream().map(Film::getId).collect(Collectors.toList());
        final Map<Integer, Film> filmMap = films.stream().collect(
                Collectors.toMap(film -> film.getId(), film -> film, (a, b) -> b));
        String sql = "select FILM_GENRE.*, GENRE_NAME from FILM_GENRE join GENRES G2 on G2.GENRE_ID = FILM_GENRE.GENRE_ID " +
                " WHERE FILM_ID IN (?)";
        List<FilmGenre> allFilmGenres = jdbcTemplate.query(sql, (rs, rowNum) -> createFilmGenre(rs), ids.toArray());
        for (Film film : filmMap.values()) {
            List<Genre> genres = new ArrayList<>();
            for (FilmGenre filmGenre : allFilmGenres) {
                if (film.getId().equals(filmGenre.getFilmId())) genres.add(
                        new Genre(filmGenre.getGenreId(), filmGenre.getGenreName()));
            }
            film.setGenres(new HashSet<>(genres));
        }
        return filmMap.values().stream().collect(Collectors.toList());
    }

    private FilmGenre createFilmGenre(ResultSet rs) throws SQLException {
        return new FilmGenre(
                rs.getInt("FILM_ID"),
                rs.getInt("GENRE_ID"),
                rs.getString("GENRE_NAME"));
    }

}
