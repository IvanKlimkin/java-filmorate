package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("DB realisation")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "select * from USERS";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> makeUser(rs)));
    }

    static public User makeUser(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("USER_ID");
        String email = rs.getString("EMAIL");
        String login = rs.getString("LOGIN");
        LocalDate birthday = rs.getDate("BIRTHDAY").toLocalDate();
        String name = rs.getString("USER_NAME");
        return new User(id, email, login, birthday, name);
    }

    @Override
    public Optional<User> getUserByID(Integer ID) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from USERS where USER_ID = ?", ID);
        if (userRows.next()) {
            User user = new User(
                    userRows.getInt("USER_ID"),
                    userRows.getString("EMAIL"),
                    userRows.getString("LOGIN"),
                    userRows.getDate("BIRTHDAY").toLocalDate(),
                    userRows.getString("USER_NAME")
            );
            log.info(
                    "Найден пользователь: {} {}",
                    user.getId(), user.getName());

            return Optional.of(user);
        } else {
            log.info("Пользователь с идентификатором {} не найден. ", ID);
            return Optional.empty();
        }
    }

    @Override
    public User createUser(User user) {
        String sql = "insert into USERS (EMAIL,LOGIN,BIRTHDAY,USER_NAME) values (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"USER_ID"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            final LocalDate birthday = user.getBirthday();
            if (birthday == null) {
                stmt.setNull(3, Types.DATE);
            } else {
                stmt.setDate(3, Date.valueOf(birthday));
            }
            stmt.setString(4, user.getName());
            return stmt;
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());
        log.debug("Сохранен пользователь {}", user.getName());
        return user;
    }

    @Override
    public void updateUser(User user) {
        String sql = "update USERS set EMAIL=?,LOGIN=?,BIRTHDAY=?,USER_NAME=? where USER_ID=?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getBirthday(),
                user.getName(),
                user.getId());
        log.debug("Обновлен пользователь {}", user.getName());
    }

    @Override
    public void deleteUser(User user) {
        String sql = "delete from USERS where USER_ID=?";
        jdbcTemplate.update(sql, user.getId());
        log.debug("Удален пользователь {}", user.getName());
    }
}
