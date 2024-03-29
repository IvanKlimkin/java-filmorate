package ru.yandex.practicum.filmorate.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    private static final DateTimeFormatter formatterWriter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter formatterReader = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
        if (Objects.equals(localDate, null)) {
            jsonWriter.value(String.valueOf(localDate));
            return;
        }
        jsonWriter.value(localDate.format(formatterWriter));
    }

    @Override
    public LocalDate read(JsonReader jsonReader) throws IOException {
        final String text = jsonReader.nextString();
        if (Objects.equals(text, null)) {
            return null;
        }
        return LocalDate.parse(text, formatterReader);
    }
}