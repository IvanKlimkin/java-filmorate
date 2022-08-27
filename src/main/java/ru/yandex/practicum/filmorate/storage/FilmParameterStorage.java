package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilmParameterStorage {
    private final JdbcTemplate jdbcTemplate;

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
}
