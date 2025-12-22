package com.cinema.utils.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    private static final DateTimeFormatter MYSQL_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            // ✅ GHI THEO CHUẨN MYSQL (KHÔNG Z)
            out.value(value.format(MYSQL_FORMAT));
        }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        String value = in.nextString();

        // ✅ CHẤP NHẬN CẢ ISO + Z
        if (value.endsWith("Z")) {
            return Instant.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        // ✅ MYSQL FORMAT
        return LocalDateTime.parse(value, MYSQL_FORMAT);
    }
}