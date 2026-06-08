package com.taskflow.android.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TaskConverters {
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter fun fromDateTime(v: String?): LocalDateTime? = v?.let { LocalDateTime.parse(it, fmt) }
    @TypeConverter fun toDateTime(v: LocalDateTime?): String?   = v?.format(fmt)

    @TypeConverter fun fromPriority(v: String?): Priority?     = v?.let { Priority.valueOf(it) }
    @TypeConverter fun toPriority(v: Priority?): String?       = v?.name

    @TypeConverter fun fromStatus(v: String?): Status?         = v?.let { Status.valueOf(it) }
    @TypeConverter fun toStatus(v: Status?): String?           = v?.name

    @TypeConverter fun fromRecurrence(v: String?): Recurrence? = v?.let { Recurrence.valueOf(it) }
    @TypeConverter fun toRecurrence(v: Recurrence?): String?   = v?.name
}
