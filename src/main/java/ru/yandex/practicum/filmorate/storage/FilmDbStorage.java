package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("DB realisation")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static Film makeFilm(ResultSet rs) throws SQLException {
        return new Film(rs.getInt("FILM_ID"),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")),
                new HashSet<>(),
                new HashSet<>()
        );
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "select FILMS.*,MPA_NAME from FILMS join MPA M on M.MPA_ID = FILMS.MPA_ID";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> makeFilm(rs)));
    }

    @Override
    public List<Film> getSortedFilms(Director director, String sort) {
        String sql;
        if(sort.equals("year")){
            sql = "select FILMS.*, MPA_NAME from FILMS join MPA M on FILMS.MPA_ID = M.MPA_ID" +
                    " join FILM_DIRECTOR FD on FILMS.FILM_ID = FD.FILM_ID where FD.DIRECTOR_ID =?";
        }
        else {
            sql = "select FILMS.*,MPA_NAME from FILMS join MPA M on FILMS.MPA_ID = M.MPA_ID" +
                    " left join LIKES L on FILMS.FILM_ID = L.FILM_ID " +
                    "join FILM_DIRECTOR FD on FILMS.FILM_ID = FD.FILM_ID" +
                    " where FD.DIRECTOR_ID=? group by FILMS.FILM_ID order by COUNT(L.USER_LIKED_ID)";
        }
            return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), director.getId());
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
                    new Mpa(filmRows.getInt("MPA_ID"), filmRows.getString("MPA_NAME")),
                    new HashSet<>(),
                    new HashSet<>()
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

}
