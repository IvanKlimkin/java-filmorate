package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Qualifier("DB realisation")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "select FILMS.*,MPA_NAME from FILMS join MPA M on M.MPA_ID = FILMS.MPA_ID";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> makeFilm(rs)));
    }

    static public Film makeFilm(ResultSet rs) throws SQLException {
        return new Film(rs.getInt("FILM_ID"),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME"))
        );
    }

    @Override
    public Optional<Film> getFilmByID(Integer ID) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                "select FILMS.*,M.MPA_NAME from FILMS" +
                        " join MPA M on FILMS.MPA_ID = M.MPA_ID where FILM_ID = ?", ID);
        if (filmRows.next()) {
            Film film = new Film(
                    filmRows.getInt("FILM_ID"),
                    filmRows.getString("NAME"),
                    filmRows.getString("DESCRIPTION"),
                    filmRows.getDate("RELEASE_DATE").toLocalDate(),
                    filmRows.getInt("DURATION"),
                    new Mpa(filmRows.getInt("MPA_ID"), filmRows.getString("MPA_NAME"))
            );
            log.info(
                    "Найден фильм: {} {}",
                    film.getId(), film.getName());
            return Optional.of(film);
        } else {
            log.info("Фильм с идентификатором {} не найден. ", ID);
            return Optional.empty();
        }
    }

    @Override
    public Film createFilm(Film film) {
        String sql = "insert into FILMS (NAME,DESCRIPTION,RELEASE_DATE,DURATION,MPA_ID) values (?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"FILM_ID"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            final LocalDate releaseDate = film.getReleaseDate();
            if (releaseDate == null) {
                stmt.setNull(3, Types.DATE);
            } else {
                stmt.setDate(3, Date.valueOf(releaseDate));
            }
            stmt.setInt(4, film.getDuration());
            Integer mpa_id = film.getMpa().getId();
            stmt.setInt(5, mpa_id);
            return stmt;
        }, keyHolder);
        film.setId(keyHolder.getKey().intValue());
        log.debug("Сохранен фильм {}", film.getName());
        return film;
    }

    @Override
    public void updateFilm(Film film) {
        String sql = "update FILMS set NAME = ?,DESCRIPTION = ?,RELEASE_DATE = ?,DURATION = ?,MPA_ID = ? where FILM_ID = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
    }

    @Override
    public void deleteFilm(Film film) {
        String sql = "delete from FILMS where FILM_ID = ?";
        jdbcTemplate.update(sql, film.getId());
    }

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
