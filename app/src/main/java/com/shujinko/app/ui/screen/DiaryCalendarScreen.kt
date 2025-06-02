package com.shujinko.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState

@Composable
fun DiaryCalendarScreen(
    onClickMore: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val startDate = YearMonth.now().minusMonths(12).atDay(1)
    val endDate = YearMonth.now().plusMonths(12).atEndOfMonth()

    val calendarState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstDayOfWeek = DayOfWeek.SUNDAY
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("날짜 선택: ${selectedDate}", style = MaterialTheme.typography.titleMedium)

        WeekCalendar(
            state = calendarState,
            dayContent = { day ->
                DayCell(day = day, selectedDate = selectedDate) {
                    selectedDate = it
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📅 ${selectedDate}의 요약", style = MaterialTheme.typography.titleMedium)
                Text("오늘도 수고했어요! 🍀")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onClickMore(selectedDate) }) {
                    Text("더보기")
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: WeekDay,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val isSelected = day.date == selectedDate
    val isSelectable = day.position == WeekDayPosition.RangeDate

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                if (isSelected) Color(0xFF90CAF9) else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .clickable(enabled = isSelectable) { onDateSelected(day.date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (isSelectable) Color.Black else Color.LightGray
        )
    }
}