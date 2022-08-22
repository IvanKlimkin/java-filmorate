package ru.yandex.practicum.filmorate.model;

import lombok.*;



@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = "eventId")
public class Event {
Integer eventId;
Integer userId;
Integer entityId;
String eventType;
String operation;
Long timestamp;
}
