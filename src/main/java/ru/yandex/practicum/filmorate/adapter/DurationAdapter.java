package ru.yandex.practicum.filmorate.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.hibernate.validator.internal.util.logging.formatter.DurationFormatter;


import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    //private static DurationFormatter formatterWriter = DurationFormatter.ofPattern("yyyy-MM-dd");
    //private static DurationFormatter formatterReader = DurationFormatter;// .ofPattern("yyyy-MM-dd");

    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        if (duration == null) {
            jsonWriter.value(String.valueOf(duration));
            return;
        }
        jsonWriter.value(duration.toMinutes());
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        final String text = jsonReader.nextString();
        if (text.equals("null")) {
            return null;
        }
        return Duration.parse(text);
    }

}
