# Filmorate - сервис по оценке фильмов

**В данной программе реализована возможность:**
- создания/обновления/удаления пользователя
- создания/обновления/удаления фильма
- создания/обновления/удаления режиссера
- получения фильма по ID
- получения списка всех созданных фильмов
- получения пользователя по ID
- получения списка всех созданных пользователей
- добавлению фильму лайков от пользователя
- добавления пользователей друг другу в друзья
- получения списка общих друзей с пользователем
- получения списка общих фильмов на основе оценок пользователя и друзей пользователя
- добавления отзывов к фильму
- поиска фильмов по ключевому слову или по части слова
- показать пользователю ленту событий
- получения рекомендаций по фильму
- получения самых популярных фильмов по годам и по жанрам


В состав модели фильма входит:
1. Название фильма
2. Описание фильма
3. Продолжительность фильма
4. Дату выхода фильма
5. Количество лайков от пользователей
6. Жанр фильма
7. Возрастной ценз фильма

В состав модели пользователя входит:
1. Логин пользователя
2. Имя пользователя
3. Email пользователя
4. Дату рождения пользователя
5. Список друзей пользователя

Все данные хранятся в БД.

**Программа написана на Java с применением фреймворка Spring, база данных H2.**

Примеры кода:
Поиск по фильма по названию и режиссеру
```java
    public List<Film> searchFilms(String lowerQuery, String params) {
        List<Film> filmList = new ArrayList<>();
        String sql;
        if (params.contains("title") && params.contains("director")) {
        sql = "select f.FILM_ID, f.NAME, f.DESCRIPTION, " +
        "f.RELEASE_DATE, f.DURATION, f.MPA_ID, m.MPA_NAME " +
        "from FILMS f " +
        "join MPA m on m.MPA_ID = f.MPA_ID " +
        "left join FILM_DIRECTOR fd on fd.FILM_ID = f.FILM_ID " +
        "left join DIRECTORS d on fd.DIRECTOR_ID = d.DIRECTOR_ID " +
        "left join (select FILM_ID, count(USER_LIKED_ID) evaluate " +
        "from LIKES group by FILM_ID) e on f.FILM_ID = e.FILM_ID " +
        "where (lower(d.DIRECTOR_NAME) like '%' || lower(?) || '%' or lower(f.NAME) like '%' || lower(?) || '%') " +
        "order by e.evaluate desc ";
        filmList = jdbcTemplate.query(sql,((rs, rowNum) -> makeFilm(rs)), lowerQuery, lowerQuery);
        }
        else if (params.equals("director")) {
        sql = "select f.FILM_ID, f.NAME, f.DESCRIPTION, " +
        "f.RELEASE_DATE, f.DURATION, f.MPA_ID, m.MPA_NAME " +
        "from FILMS f " +
        "left join FILM_DIRECTOR fd on fd.FILM_ID = f.FILM_ID " +
        "left join DIRECTORS d on fd.DIRECTOR_ID = d.DIRECTOR_ID " +
        "join MPA m on m.MPA_ID = f.MPA_ID " +
        "left join (select FILM_ID, count(USER_LIKED_ID) evaluate " +
        "from LIKES group by FILM_ID) e on f.FILM_ID = e.FILM_ID " +
        "where lower(d.DIRECTOR_NAME) like '%' || lower(?) || '%' " +
        "order by e.evaluate desc";
        filmList = jdbcTemplate.query(sql,((rs, rowNum) -> makeFilm(rs)), lowerQuery);
        } else if (params.equals("title")) {
        sql = "select f.FILM_ID, f.NAME, f.DESCRIPTION, " +
        "f.RELEASE_DATE, f.DURATION, f.MPA_ID, m.MPA_NAME " +
        "from FILMS f " +
        "join MPA m on m.MPA_ID = f.MPA_ID " +
        "left join FILM_DIRECTOR fd on fd.FILM_ID = f.FILM_ID " +
        "left join (select FILM_ID, count(user_liked_id) evaluate " +
        "from LIKES group by FILM_ID) e on f.FILM_ID = e.FILM_ID " +
        "where lower(f.NAME) like '%' || lower(?) || '%' " +
        "order by e.evaluate desc";
        filmList = jdbcTemplate.query(sql,((rs, rowNum) -> makeFilm(rs)), lowerQuery);
        }
        return filmList;
        }
```
Добавление/обновление/удаление отзывов у фильма
```java
 public Review createReview(Review review) {
        String sql = "insert into REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL) values (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"REVIEW_ID"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            stmt.setInt(5, review.getUseful());
            return stmt;
        }, keyHolder);
        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        log.debug("Сохранен отзыв от пользователя {} о фильме {}", review.getUserId(), review.getFilmId());
        return review;
    }

    public Review updateReview(Review review) {
        String sql = "update REVIEWS set CONTENT = ?, IS_POSITIVE = ?, USEFUL = ?" +
                " where REVIEW_ID = ?";
        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getUseful(),
                review.getReviewId());
        log.debug("Обновлен отзыв {} у фильма {} от пользователя {}.", review.getReviewId(), review.getFilmId(),
                review.getUserId());
        return review;
    }

    public void deleteReview(int id) {
        String sql = "delete from REVIEWS where REVIEW_ID = ?";
        jdbcTemplate.update(sql, id);
        log.debug("Удален отзыв {}.", id);
    }
```
Добавление/обновление/удаление режиссера у фильма
```java
    public List<Director> findAll() {
        String sql = "select DIRECTORS.* from DIRECTORS";
        return jdbcTemplate.query(sql, ((rs, rowNum) -> this.createDirector(rs)));
    }

    public Director addDirector(Director director) {
        String sql = "insert into DIRECTORS (DIRECTOR_NAME) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"DIRECTOR_ID"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        director.setId(keyHolder.getKey().intValue());
        log.debug("Сохранен режиссер {}", director.getName());
        return director;
    }

    public void updateDirector(Director director) {
        String sql = "update DIRECTORS set DIRECTOR_ID = ?,DIRECTOR_NAME = ? where DIRECTOR_ID = ?";
        jdbcTemplate.update(sql,
                director.getId(),
                director.getName(),
                director.getId());
    }

    public void deleteDirector(Integer Id) {
        String sql = "delete from DIRECTORS where DIRECTOR_ID=?";
        int count = jdbcTemplate.update(sql, Id);
        if (count == 0) throw new ServerException(String.format("Режиссер с ID=%d не найден", Id));
    }
```
Запрос самых популярных фильмов по разным параметрам
```java
    public List<Film> getMostPopularFilmsByYear(int year, int limit) {
        String sql = "select * from FILMS F join MPA M on F.MPA_ID = M.MPA_ID " +
                "left join LIKES L ON F.FILM_ID = L.FILM_ID " +
                "where YEAR(F.RELEASE_DATE) = ? " +
                "group by F.FILM_ID, L.USER_LIKED_ID " +
                "order by COUNT(L.USER_LIKED_ID) desc limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), year, limit);
    }

    @Override
    public List<Film> getMostPopularFilmsByGenre(int genreId, int limit) {
        String sql = "select * from FILMS F join MPA M on F.MPA_ID = M.MPA_ID " +
                "left join LIKES L ON F.FILM_ID = L.FILM_ID " +
                "left join FILM_GENRE FG ON F.FILM_ID = FG.FILM_ID " +
                "where FG.GENRE_ID = ? " +
                "group by F.FILM_ID, L.USER_LIKED_ID, FG.GENRE_ID " +
                "order by COUNT(L.USER_LIKED_ID) desc limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), genreId, limit);
    }

    @Override
    public List<Film> getMostPopularFilmsByGenreAndYear(int genreId, int year, int limit) {
        String sql = "select * from FILMS F join MPA M on F.MPA_ID = M.MPA_ID " +
                "left join LIKES L ON F.FILM_ID = L.FILM_ID " +
                "left join FILM_GENRE FG ON F.FILM_ID = FG.FILM_ID " +
                "where FG.GENRE_ID = ? AND YEAR(F.RELEASE_DATE) = ? " +
                "group by F.FILM_ID, L.USER_LIKED_ID " +
                "order by COUNT(L.USER_LIKED_ID) desc limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), genreId, year, limit);
    }
```

Получение лайков у определенного фильма, а также соритровка фильмов по кол-ву лайков
```java
    private Optional<List<Integer>> getLikesByFilm(int filmId) {
        //Функция получения идентификаторов пользователей по идентификатору фильма (т.е. фильтрация)
        Function<Integer, List<Integer>> filterFuncByFilmId = (filmIdentifier) -> {
            String sqlSelectLikesByFilmId = "SELECT user_liked_id FROM likes WHERE film_id = ?";
            List<Integer> usersId = likeStorage.getJdbcTemplate()
                    .query(sqlSelectLikesByFilmId, (rs, rowNum) -> rs.getInt("user_liked_id"), filmIdentifier);
            return usersId;
        };
        //Получить группу лайков, применив функцию фильтрации
        List<Integer> likes = likeStorage.sortingOrFiltering(filterFuncByFilmId, filmId);
        return Optional.of(likes);
    }
    
    private List<Film> getFilmsOrderedByNumberOfLikes(HashMap<Integer, Integer> filmsIdWithNumberOfLikes) {
        //Функция сортировки
        Function<HashMap<Integer, Integer>, List<Film>> sortingFunction = (filmIdNumberLike) -> {
            return filmIdNumberLike.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .map(filmId -> filmStorage.getFilmByID(filmId).get())
                    .collect(Collectors.toList());
        };
```