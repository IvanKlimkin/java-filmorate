package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmParameter;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilmParameterStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void setFilmParameter(Film film) {
        String sql = "delete from FILM_GENRE where FILM_ID = ?";
        jdbcTemplate.update(sql, film.getId());
        sql = "delete from FILM_DIRECTOR where FILM_ID = ?";
        jdbcTemplate.update(sql, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            log.info("Пустой лист жанров фильма с ID=%d", film.getId());
        } else {
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
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            log.info("Пустой лист режиссеров фильма с ID=%d", film.getId());
        } else {
            String sqlTryBatch = "insert into FILM_DIRECTOR (FILM_ID,DIRECTOR_ID) values(?,?)";
            jdbcTemplate.batchUpdate(sqlTryBatch,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setInt(1, film.getId());
                            ps.setInt(2, film.getDirectors().stream()
                                    .collect(Collectors.toList())
                                    .get(i)
                                    .getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return film.getDirectors().size();
                        }
                    });
        }
    }

    public List<Film> loadFilmParameters(List<Film> films) {
        if (films.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Integer> ids = films.stream().map(Film::getId).collect(Collectors.toList());
        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        final Map<Integer, Film> filmMap = films.stream().collect(
                Collectors.toMap(film -> film.getId(), film -> film, (a, b) -> b));
        String sql = "select FILM_GENRE.*, GENRE_NAME from FILM_GENRE" +
                " join GENRES G on FILM_GENRE.GENRE_ID = G.GENRE_ID WHERE FILM_ID IN (:ids)";
        List<FilmParameter> allFilmsParameter = namedParameterJdbcTemplate.query(
                sql, parameters, (rs, rowNum) -> createFilmParameter(rs, 0));
        sql = "select FILM_DIRECTOR.*, DIRECTOR_NAME from FILM_DIRECTOR" +
                " join DIRECTORS D on FILM_DIRECTOR.DIRECTOR_ID = D.DIRECTOR_ID  WHERE FILM_ID IN (:ids)";
        allFilmsParameter.addAll(
                namedParameterJdbcTemplate.query(
                        sql, parameters, (rs, rowNum) -> createFilmParameter(rs, 1)));
        for (Film film : filmMap.values()) {
            List<Genre> genres = new ArrayList<>();
            List<Director> directors = new ArrayList<>();
            for (FilmParameter filmParameter : allFilmsParameter) {
                if (film.getId().equals(filmParameter.getFilmId())){
                    if (filmParameter.getFilmParameterNum() == 0) {
                        genres.add(
                                new Genre(filmParameter.getParameterId(), filmParameter.getParameterName()));
                    } else if (filmParameter.getFilmParameterNum() == 1) {
                        directors.add(
                                new Director(filmParameter.getParameterId(), filmParameter.getParameterName()));
                    }
            }}
            film.setGenres(new HashSet<>(genres));
            film.setDirectors(new HashSet<>(directors));
        }
        return filmMap.values().stream().collect(Collectors.toList());
    }

    private FilmParameter createFilmParameter(ResultSet rs, Integer parameterNum) throws SQLException {
        if (parameterNum == 0) {
            return new FilmParameter(
                    rs.getInt("FILM_ID"),
                    rs.getInt("GENRE_ID"),
                    rs.getString("GENRE_NAME"),
                    parameterNum);
        } else {
            return new FilmParameter(
                    rs.getInt("FILM_ID"),
                    rs.getInt("DIRECTOR_ID"),
                    rs.getString("DIRECTOR_NAME"),
                    parameterNum);
        }
    }
}
