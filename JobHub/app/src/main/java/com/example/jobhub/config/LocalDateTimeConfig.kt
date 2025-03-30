package com.example.jobhub.config

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeConfig : JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
    private val formatters = listOf(
        DateTimeFormatter.ISO_DATE_TIME,                  // "2025-06-30T12:34:56"
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), // "2025-06-30 12:34:56"
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss") // "2025-06-30T12:34:56"
    )

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime {
        val dateString = json.asString
        for (formatter in formatters) {
            try {
                return LocalDateTime.parse(dateString, formatter)
            } catch (_: Exception) { }
        }
        throw JsonParseException("Unparseable date: $dateString") // In ra lỗi để debug
    }

    override fun serialize(src: LocalDateTime, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.format(formatters[0])) // Chọn 1 format chuẩn
    }
}

