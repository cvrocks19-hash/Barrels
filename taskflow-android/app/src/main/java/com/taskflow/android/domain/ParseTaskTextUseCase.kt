package com.taskflow.android.domain

import com.taskflow.android.data.Priority
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class ParsedTask(
    val title: String,
    val dueDate: LocalDateTime?,
    val priority: Priority,
)

class ParseTaskTextUseCase @Inject constructor() {

    fun parse(raw: String): ParsedTask {
        var text = raw.trim()
        val today = LocalDate.now()

        // --- Priority from keywords ---
        val priority = when {
            Regex("""\b(urgent|asap|critical|!)\b""", RegexOption.IGNORE_CASE).containsMatchIn(text) -> Priority.HIGH
            Regex("""\b(low|someday|whenever)\b""",  RegexOption.IGNORE_CASE).containsMatchIn(text) -> Priority.LOW
            else -> Priority.MEDIUM
        }

        // --- Relative date resolution ---
        val nextWeekday: (DayOfWeek) -> LocalDate = { dow ->
            val candidate = today.with(dow)
            if (candidate <= today) candidate.plusWeeks(1) else candidate
        }

        data class DateRule(val regex: Regex, val date: LocalDate)
        val dateRules = listOf(
            DateRule(Regex("""\btoday\b""",     RegexOption.IGNORE_CASE), today),
            DateRule(Regex("""\btomorrow\b""",  RegexOption.IGNORE_CASE), today.plusDays(1)),
            DateRule(Regex("""\bnext week\b""", RegexOption.IGNORE_CASE), today.plusWeeks(1)),
            DateRule(Regex("""\bmonday\b""",    RegexOption.IGNORE_CASE), nextWeekday(DayOfWeek.MONDAY)),
            DateRule(Regex("""\btuesday\b""",   RegexOption.IGNORE_CASE), nextWeekday(DayOfWeek.TUESDAY)),
            DateRule(Regex("""\bwednesday\b""", RegexOption.IGNORE_CASE), nextWeekday(DayOfWeek.WEDNESDAY)),
            DateRule(Regex("""\bthursday\b""",  RegexOption.IGNORE_CASE), nextWeekday(DayOfWeek.THURSDAY)),
            DateRule(Regex("""\bfriday\b""",    RegexOption.IGNORE_CASE), nextWeekday(DayOfWeek.FRIDAY)),
        )

        var resolvedDate: LocalDate? = null
        for (rule in dateRules) {
            if (rule.regex.containsMatchIn(text)) {
                resolvedDate = rule.date
                text = rule.regex.replace(text, "").trim()
                break
            }
        }

        // --- Time: "at 3pm", "at 10:30am", "at 14:00" ---
        val timeRegex = Regex("""\bat (\d{1,2})(?::(\d{2}))?\s*(am|pm)?\b""", RegexOption.IGNORE_CASE)
        val timeMatch = timeRegex.find(text)
        var resolvedTime: LocalTime? = null
        if (timeMatch != null) {
            var hour   = timeMatch.groupValues[1].toInt()
            val minute = timeMatch.groupValues[2].ifEmpty { "0" }.toInt()
            val ampm   = timeMatch.groupValues[3].lowercase()
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            resolvedTime = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            text = timeRegex.replace(text, "").trim()
        }

        val dueDate = when {
            resolvedDate != null -> LocalDateTime.of(resolvedDate, resolvedTime ?: LocalTime.of(9, 0))
            resolvedTime != null -> LocalDateTime.of(today, resolvedTime)
            else                 -> null
        }

        val cleanTitle = text.replace(Regex("""\s{2,}"""), " ").trimEnd(',', '.', ' ')
        return ParsedTask(
            title    = cleanTitle.ifEmpty { raw.trim() },
            dueDate  = dueDate,
            priority = priority,
        )
    }
}
