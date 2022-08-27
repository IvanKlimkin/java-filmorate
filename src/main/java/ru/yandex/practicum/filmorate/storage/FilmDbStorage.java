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
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Qualifier("DB realisation")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film makeFilm(ResultSet rs) throws SQLException {
        return new Film(rs.getInt("FILM_ID"),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getDate("RELEASE_DATE").toLocalDate(),
                rs.getInt("DURATION"),
                new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")),
                new HashSet<>(){{
                    this.addAll(getGenresByFilmId(rs.getInt("FILM_ID")));
                }},
                new HashSet<>(){{
                    this.addAll(getDirectorsByFilmId(rs.getInt("FILM_ID")));
                }},
                rs.getDouble("RATING"),
                rs.getInt("COUNT_POSITIVE"),
                rs.getInt("COUNT_NEGATIVE")
        );
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "select FILMS.*, MPA_NAME from FILMS " +
                "join MPA M on M.MPA_ID = FILMS.MPA_ID " +
                "ORDER BY RATING DESC, " +
                "COUNT_POSITIVE DESC, " +
                "COUNT_NEGATIVE DESC, " +
                "FILM_ID";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> makeFilm(rs)));
    }

    @Override
    public List<Film> getSortedFilms(Director director, String sort) {
        String sql;
        if (sort.equals("year")) {
            sql = "select FILMS.*, MPA_NAME from FILMS " +
                    "join MPA M on FILMS.MPA_ID = M.MPA_ID " +
                    "join FILM_DIRECTOR FD on FILMS.FILM_ID = FD.FILM_ID " +
                    "where FD.DIRECTOR_ID =? " +
                    "group by FILMS.FILM_ID " +
                    "ORDER BY (avg(RATING), " +
                    "avg(COUNT_POSITIVE), " +
                    "avg(COUNT_NEGATIVE)) DESC";
        } else {
            sql = "select FILMS.*,MPA_NAME from FILMS " +
                    "join MPA M on FILMS.MPA_ID = M.MPA_ID " +
                    "join FILM_DIRECTOR FD on FILMS.FILM_ID = FD.FILM_ID " +
                    "where FD.DIRECTOR_ID=? " +
                    "group by FILMS.FILM_ID " +
                    "ORDER BY (avg(RATING), " +
                    "avg(COUNT_POSITIVE), " +
                    "avg(COUNT_NEGATIVE)) DESC";
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
                    Objects.requireNonNull(filmRows.getDate("RELEASE_DATE")).toLocalDate(),
                    filmRows.getInt("DURATION"),
                    new Mpa(filmRows.getInt("MPA_ID"), filmRows.getString("MPA_NAME")),
                    new HashSet<>(){{
                        this.addAll(getGenresByFilmId(filmRows.getInt("FILM_ID")));
                    }},
                    new HashSet<>(){{
                        this.addAll(getDirectorsByFilmId(filmRows.getInt("FILM_ID")));
                    }},
                    filmRows.getDouble("RATING"),
                    filmRows.getInt("COUNT_POSITIVE"),
                    filmRows.getInt("COUNT_NEGATIVE")
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
        String sql = "insert into FILMS (NAME,DESCRIPTION,RELEASE_DATE,DURATION,MPA_ID, RATING, COUNT_POSITIVE, " +
                "COUNT_NEGATIVE) values (?,?,?,?,?,?,?,?)";
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
            stmt.setDouble(6, 0.0);
            stmt.setInt(7, 0);
            stmt.setInt(8, 0);
            return stmt;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        log.debug("Сохранен фильм {}", film.getName());
        return film;
    }

    @Override
    public void updateFilm(Film film) {
        String sql = "merge into FILMS (FILM_ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID, RATING, " +
                "COUNT_POSITIVE, COUNT_NEGATIVE) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sql1 = "update FILMS set NAME = ?,DESCRIPTION = ?,RELEASE_DATE = ?,DURATION = ?,MPA_ID = ? where " +
                "FILM_ID = ?";
        jdbcTemplate.update(sql,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getRating(),
                film.getCountPositive(),
                film.getCountNegative());
    }

    @Override
    public void deleteFilm(Film film) {
        String sql = "delete from FILMS where FILM_ID = ?";
        jdbcTemplate.update(sql, film.getId());
    }

    @Override
    public List<Film> searchFilms(String lowerQuery, String params) {
        List<Film> filmList = new ArrayList<>();
        String sql;
        if (params.contains("title") && params.contains("director")) {
            sql = "select f.FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID, f.RATING, " +
                    "f.COUNT_POSITIVE, f.COUNT_NEGATIVE, m.MPA_NAME " +
                    "from FILMS f " +
                    "join MPA m on m.MPA_ID = f.MPA_ID " +
                    "left join FILM_DIRECTOR fd on fd.FILM_ID = f.FILM_ID " +
                    "left join DIRECTORS d on fd.DIRECTOR_ID = d.DIRECTOR_ID " +
                    "where (lower(d.DIRECTOR_NAME) like '%' || lower(?) || '%' " +
                    "or lower(f.NAME) like '%' || lower(?) || '%') " +
                    "ORDER BY (RATING, " +
                    "COUNT_POSITIVE, " +
                    "COUNT_NEGATIVE) DESC";
            filmList = jdbcTemplate.query(sql, ((rs, rowNum) -> makeFilm(rs)), lowerQuery, lowerQuery);
        } else if (params.equals("director")) {
            sql = "select f.FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID, f.RATING, " +
                    "f.COUNT_POSITIVE, f.COUNT_NEGATIVE, m.MPA_NAME " +
                    "from FILMS f " +
                    "left join FILM_DIRECTOR fd on fd.FILM_ID = f.FILM_ID " +
                    "left join DIRECTORS d on fd.DIRECTOR_ID = d.DIRECTOR_ID " +
                    "join MPA m on m.MPA_ID = f.MPA_ID " +
                    "where lower(d.DIRECTOR_NAME) like '%' || lower(?) || '%' " +
                    "ORDER BY (RATING, " +
                    "COUNT_POSITIVE, " +
                    "COUNT_NEGATIVE) DESC";
            filmList = jdbcTemplate.query(sql, ((rs, rowNum) -> makeFilm(rs)), lowerQuery);
        } else if (params.equals("title")) {
            sql = "select f.FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID, f.RATING, " +
                    "f.COUNT_POSITIVE, f.COUNT_NEGATIVE, m.MPA_NAME " +
                    "from FILMS f " +
                    "join MPA m on m.MPA_ID = f.MPA_ID " +
                    "left join FILM_DIRECTOR fd on fd.FILM_ID = f.FILM_ID " +
                    "where lower(f.NAME) like '%' || lower(?) || '%' " +
                    "ORDER BY (RATING, " +
                    "COUNT_POSITIVE, " +
                    "COUNT_NEGATIVE) DESC";
            filmList = jdbcTemplate.query(sql, ((rs, rowNum) -> makeFilm(rs)), lowerQuery);
        }
        return filmList;
    }

    @Override
    public List<Film> getMostPopularFilmsByYear(int year, int limit) {
        String sql = "select * from FILMS F " +
                "join MPA M on F.MPA_ID = M.MPA_ID " +
                "where YEAR(F.RELEASE_DATE) = ? " +
                "ORDER BY (RATING, " +
                "COUNT_POSITIVE, " +
                "COUNT_NEGATIVE) DESC " +
                "limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), year, limit);
    }

    @Override
    public List<Film> getMostPopularFilmsByGenre(int genreId, int limit) {
        String sql = "select * from FILMS F " +
                "join MPA M on F.MPA_ID = M.MPA_ID " +
                "left join FILM_GENRE FG ON F.FILM_ID = FG.FILM_ID " +
                "where FG.GENRE_ID = ? " +
                "group by F.FILM_ID, FG.GENRE_ID " +
                "ORDER BY (avg(RATING), " +
                "avg(COUNT_POSITIVE), " +
                "avg(COUNT_NEGATIVE)) DESC " +
                "limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), genreId, limit);
    }

    @Override
    public List<Film> getMostPopularFilmsByGenreAndYear(int genreId, int year, int limit) {
        String sql = "select * from FILMS F " +
                "join MPA M on F.MPA_ID = M.MPA_ID " +
                "left join FILM_GENRE FG ON F.FILM_ID = FG.FILM_ID " +
                "where FG.GENRE_ID = ? AND YEAR(F.RELEASE_DATE) = ? " +
                "group by F.FILM_ID " +
                "ORDER BY (avg(RATING), " +
                "avg(COUNT_POSITIVE), " +
                "avg(COUNT_NEGATIVE)) DESC " +
                "limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), genreId, year, limit);
    }

    @Override
    public List<Film> getSharedFilmsWithFriend(int userId, int friendId) {
        String sql = "select * from FILMS F " +
                "join MPA M on F.MPA_ID = M.MPA_ID " +
                "left join LIKES L on F.FILM_ID = L.FILM_ID " +
                "where (L.USER_LIKED_ID = ? and L.USER_LIKED_ID = ?) " +
                "ORDER BY (RATING, " +
                "COUNT_POSITIVE, " +
                "COUNT_NEGATIVE) DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), userId, friendId);
    }

    private List<Genre> getGenresByFilmId(int id) {
        String sqlSelect = "select G.GENRE_ID, G.GENRE_NAME from FILM_GENRE FG " +
                "left outer join GENRES G on FG.GENRE_ID = G.GENRE_ID " +
                "where FILM_ID = ?";
        return jdbcTemplate.query(sqlSelect, (rs, rowNum) -> Genre.builder()
                .id(rs.getInt("GENRES.GENRE_ID"))
                .name(rs.getString("GENRES.GENRE_NAME"))
                .build(), id);
    }

    private List<Director> getDirectorsByFilmId(int id) {
        String sqlSelect = "select D.DIRECTOR_ID, D.DIRECTOR_NAME from FILM_DIRECTOR FD " +
                "left outer join DIRECTORS D on FD.DIRECTOR_ID = D.DIRECTOR_ID " +
                "where FILM_ID = ?";
        return jdbcTemplate.query(sqlSelect, (rs, rowNum) -> Director.builder()
                .id(rs.getInt("DIRECTORS.DIRECTOR_ID"))
                .name(rs.getString("DIRECTORS.DIRECTOR_NAME"))
                .build(), id);
    }
}
