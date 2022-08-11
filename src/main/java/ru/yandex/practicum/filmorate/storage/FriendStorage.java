package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Slf4j
@Component
public class FriendStorage {

    private final JdbcTemplate jdbcTemplate;

    public FriendStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend(Integer userID, Integer friendID) {
        String sql = "merge into FRIENDS(USER_ID, FRIEND_ID) key(USER_ID,FRIEND_ID) VALUES (?,?)";
        jdbcTemplate.update(sql, userID, friendID);
    }

    public void deleteFriend(Integer userID, Integer friendID) {
        String sql = "delete from FRIENDS where USER_ID=? and FRIEND_ID=?";
        jdbcTemplate.update(sql, userID, friendID);
    }

    public List<User> getCommonFriends(Integer user1ID, Integer user2ID) {
        String sql = "select u.* from USERS u " +
                "join FRIENDS f1 on(u.USER_ID = f1.FRIEND_ID and f1.USER_ID = ?)" +
                "join FRIENDS f2 on (u.USER_ID = f2.FRIEND_ID and f2.USER_ID =?)";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> UserDbStorage.makeUser(rs)), user1ID, user2ID);
    }

    public List<User> getUserFriends(Integer userID) {
        String sql = "select U.USER_ID,U.EMAIL,U.LOGIN,U.BIRTHDAY,U.USER_NAME FROM USERS as U " +
                "join FRIENDS as F on (U.USER_ID=F.FRIEND_ID and F.USER_ID=?)";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> UserDbStorage.makeUser(rs)), userID);
    }
}
