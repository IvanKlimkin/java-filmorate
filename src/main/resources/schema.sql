DROP TABLE IF EXISTS USERS CASCADE;
DROP TABLE IF EXISTS MPA CASCADE;
DROP TABLE IF EXISTS GENRES CASCADE;
DROP TABLE IF EXISTS FILMS CASCADE;
DROP TABLE IF EXISTS FRIENDS CASCADE;
DROP TABLE IF EXISTS LIKES CASCADE;
DROP TABLE IF EXISTS FILM_GENRE CASCADE;
DROP TABLE IF EXISTS FILM_DIRECTOR CASCADE;

create TABLE IF NOT EXISTS USERS
(
    USER_ID   INT PRIMARY KEY AUTO_INCREMENT,
    EMAIL     VARCHAR(100) NOT NULL,
    LOGIN     VARCHAR(100) NOT NULL,
    BIRTHDAY  DATE,
    USER_NAME VARCHAR(100) NOT NULL
);

create TABLE IF NOT EXISTS MPA
(
    MPA_ID   INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    MPA_NAME VARCHAR(200)
);


create TABLE IF NOT EXISTS GENRES
(
    GENRE_ID   INT NOT NULL AUTO_INCREMENT,
    GENRE_NAME VARCHAR(200),
    CONSTRAINT GENRE_PK PRIMARY KEY (GENRE_ID)
);

create TABLE IF NOT EXISTS DIRECTORS
(
    DIRECTOR_ID   INT NOT NULL AUTO_INCREMENT,
    DIRECTOR_NAME VARCHAR(200),
    CONSTRAINT DIRECTOR_PK PRIMARY KEY (DIRECTOR_ID)
);

create TABLE IF NOT EXISTS FILMS
(
    FILM_ID      INT PRIMARY KEY AUTO_INCREMENT,
    NAME         VARCHAR(100) NOT NULL,
    DESCRIPTION  VARCHAR(200) NOT NULL,
    RELEASE_DATE DATE,
    DURATION     INT,
    MPA_ID       INT,
    CONSTRAINT FILMS_MPA_REF
        FOREIGN KEY (MPA_ID)
            REFERENCES MPA (MPA_ID) ON DELETE CASCADE
);

create TABLE IF NOT EXISTS FRIENDS
(
    USER_ID   INT NOT NULL,
    FRIEND_ID INT,
    CONSTRAINT USER_REF
        FOREIGN KEY (USER_ID)
            REFERENCES USERS (USER_ID) ON DELETE CASCADE
);

create TABLE IF NOT EXISTS LIKES
(
    FILM_ID       INT REFERENCES FILMS (FILM_ID) ON DELETE CASCADE,
    USER_LIKED_ID INT REFERENCES USERS (USER_ID),
    PRIMARY KEY (FILM_ID, USER_LIKED_ID)
);

create TABLE IF NOT EXISTS FILM_GENRE
(
    FILM_ID  INT NOT NULL,
    GENRE_ID INT,
    CONSTRAINT FILM_FROM_GENRE_REF
        FOREIGN KEY (FILM_ID)
            REFERENCES FILMS (FILM_ID) ON DELETE CASCADE,
    CONSTRAINT GENRE_FROM_GENRE_REF
        FOREIGN KEY (GENRE_ID)
            REFERENCES GENRES (GENRE_ID) ON DELETE CASCADE,
    CONSTRAINT FILM_GENRE_PK PRIMARY KEY (FILM_ID, GENRE_ID)
);

create TABLE IF NOT EXISTS FILM_DIRECTOR
(
    FILM_ID     INT NOT NULL,
    DIRECTOR_ID INT,
    CONSTRAINT FILM_FROM_DIRECTOR_REF
        FOREIGN KEY (FILM_ID)
            REFERENCES FILMS (FILM_ID) ON DELETE CASCADE,
    CONSTRAINT DIRECTOR_FROM_DIRECTOR_REF
        FOREIGN KEY (DIRECTOR_ID)
            REFERENCES DIRECTORS (DIRECTOR_ID) ON DELETE CASCADE,
    CONSTRAINT FILM_DIRECTOR_PK PRIMARY KEY (FILM_ID, DIRECTOR_ID)
);

create table if not exists REVIEWS
(
    REVIEW_ID   INTEGER auto_increment,
    CONTENT     CHARACTER VARYING(1000) not null,
    IS_POSITIVE BOOLEAN,
    USER_ID     INTEGER,
    FILM_ID     INTEGER,
    USEFUL      INTEGER default 0,
    constraint REVIEW_PK
        primary key (REVIEW_ID),
    constraint FILM_FK
        foreign key (FILM_ID) references FILMS on delete cascade,
    constraint USER_FK
        foreign key (USER_ID) references USERS on delete cascade
);

create table if not exists REVIEW_USEFUL
(
    REVIEW_ID INTEGER not null,
    USER_ID   INTEGER not null,
    USEFUL    INTEGER not null,
    constraint USEFUL_PK
        primary key (REVIEW_ID, USER_ID),
    constraint REVIEW_FK
        foreign key (REVIEW_ID) references REVIEWS
            on delete cascade,
    constraint USER_FK1
        foreign key (USER_ID) references USERS
            on delete cascade
);

