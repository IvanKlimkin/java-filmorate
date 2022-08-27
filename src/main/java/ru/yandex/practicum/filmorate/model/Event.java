package ru.yandex.practicum.filmorate.model;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = "eventId")
public class Event {
    private Integer eventId;
    private Integer userId;
    private Integer entityId;
    private String eventType;
    private String operation;
    private Long timestamp;
}
