package com.shujinko.app.ui.screen

import android.text.format.DateUtils.isToday
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shujinko.app.data.Item.DiaryResponse
import com.shujinko.app.ui.components.TopTitle
import java.time.*
import java.time.format.TextStyle
import java.util.*

@Composable
fun DiaryCalendarScreen(
    onClickMore: (LocalDate) -> Unit,
    onWritePastDiary: (LocalDate) -> Unit,
    diaryMap: Map<LocalDate, DiaryResponse>,
    onMonthChange: (Int, Int) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isWeekView by remember { mutableStateOf(true) }
    var lastLoadedMonth by remember { mutableStateOf(Pair(0, 0)) }

    val normalizedDiaryMap = remember(diaryMap) {
        diaryMap.mapKeys { it.key }
    }

    LaunchedEffect(selectedDate) {
        val year = selectedDate.year
        val month = selectedDate.monthValue
        if (lastLoadedMonth != Pair(year, month)) {
            onMonthChange(year, month)
            lastLoadedMonth = Pair(year, month)
        }
    }

    TopTitle(
        title = "CALENDAR",
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SegmentedToggle(
                    isWeekView = isWeekView,
                    onToggle = { isWeekView = it }
                )
            }


                Text(
                    text = "    ${selectedDate.year}년 ${selectedDate.monthValue}월",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )


            // 캘린더
            if (isWeekView) {
                HorizontalWeekCalendar(
                    selectedDate = selectedDate,
                    diaryMap = normalizedDiaryMap,
                    onDateSelected = { selectedDate = it }
                )

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                HorizontalMonthCalendar(
                    selectedDate = selectedDate,
                    diaryMap = normalizedDiaryMap,
                    onDateSelected = { selectedDate = it }
                )
            }

            DiarySummaryCard(
                selectedDate = selectedDate,
                diary = normalizedDiaryMap[selectedDate],
                onClickMore = onClickMore,
                onWritePastDiary = onWritePastDiary
            )
        }
    )
}


@Composable
fun SegmentedToggle(
    isWeekView: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val toggleOptions = listOf("주간", "월간")

    Surface(
        modifier = Modifier
            .height(36.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            toggleOptions.forEachIndexed { index, label ->
                val selected = (isWeekView && index == 0) || (!isWeekView && index == 1)

                val backgroundColor =
                    if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                val textColor =
                    if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onToggle(index == 0) }
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor
                    )
                }
            }
        }
    }
}



@Composable
fun DiarySummaryCard(
    selectedDate: LocalDate,
    diary: DiaryResponse?,
    onClickMore: (LocalDate) -> Unit,
    onWritePastDiary: (LocalDate) -> Unit
) {
    val dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
    val dateLabel = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 ($dayOfWeek)"
    val isPastOrToday = !selectedDate.isAfter(LocalDate.now())
    val isToday = selectedDate == LocalDate.now()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "${dateLabel}의 요약",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                diary != null -> {
                    if (!diary.summary.isNullOrEmpty()) {
                        Text(
                            text = diary.summary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "감정: ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "😊 ${diary.label}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                isPastOrToday  -> {
                    Text(
                        text = if (isToday)
                            "오늘은 일기가 아직 없네요. 지금 작성해볼까요?"
                        else
                            "이 날은 일기가 없네요. 지금 작성해볼까요?",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    Text(
                        text = "곧 있을 하루를 보내고 일기를 남겨보세요.",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when {
                    diary != null -> {
                        OutlinedButton(onClick = { onClickMore(selectedDate) }) {
                            Text("더보기")
                        }
                    }

                    isPastOrToday  -> {
                        OutlinedButton(onClick = { onWritePastDiary(selectedDate) }) {
                            Text(if (isToday) "오늘 일기 작성하기" else "지난 일기 작성하기")
                        }
                    }

                    else -> {
                        // 오늘 또는 미래 → 버튼 없음
                    }
                }
            }
        }
    }
}



@Composable
fun HorizontalWeekCalendar(
    selectedDate: LocalDate,
    diaryMap: Map<LocalDate, DiaryResponse>,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val firstDayOfWeek = DayOfWeek.SUNDAY
    val initialWeekIndex = 1000

    val currentDayOfWeek = today.dayOfWeek
    val currentWeekStart = today.minusDays(((currentDayOfWeek.value % 7 - firstDayOfWeek.value % 7 + 7) % 7).toLong())

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialWeekIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val screenWidth = (LocalConfiguration.current.screenWidthDp.dp - 45.dp)

    // ✅ 스크롤 위치 기반 selectedDate 업데이트
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val weekStart = currentWeekStart.plusWeeks((listState.firstVisibleItemIndex - initialWeekIndex).toLong())
        val middleDate = weekStart.plusDays(3) // 수요일
        if (middleDate != selectedDate) {
            onDateSelected(middleDate)
        }
    }

    LazyRow(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        items(2000) { index ->
            val weekStart = currentWeekStart.plusWeeks((index - initialWeekIndex).toLong())
            val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }

            Box(modifier = Modifier.width(screenWidth)) {
                WeekContent(
                    weekDates = weekDates,
                    selectedDate = selectedDate,
                    today = today,
                    diaryMap = diaryMap,
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}


@Composable
fun WeekContent(
    weekDates: List<LocalDate>,
    selectedDate: LocalDate,
    today: LocalDate,
    diaryMap: Map<LocalDate, DiaryResponse>,
    onDateSelected: (LocalDate) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Row(
        modifier = Modifier
            .width(screenWidth)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDates.forEach { date ->
            val isSelected = date == selectedDate
            val isToday = date == today
            val hasDiary = diaryMap.containsKey(date)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                // 요일 텍스트
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 날짜 셀
                DateCell(
                    date = date,
                    isSelected = isSelected,
                    isToday = isToday,
                    hasDiary = hasDiary,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}



@Composable
fun HorizontalMonthCalendar(
    selectedDate: LocalDate,
    diaryMap: Map<LocalDate, DiaryResponse>,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val initialIndex = 1000
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val screenWidth = (LocalConfiguration.current.screenWidthDp.dp - 45.dp)

    // ✅ 스크롤 위치 기반으로 selectedDate 자동 갱신
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val centerMonth = YearMonth.now().plusMonths((listState.firstVisibleItemIndex - initialIndex).toLong())
        val middleOfMonth = centerMonth.atDay(15) // 15일 기준으로 선택
        if (middleOfMonth != selectedDate) {
            onDateSelected(middleOfMonth)
        }
    }

    // 🎯 초기 월로 스크롤 정렬
    LaunchedEffect(Unit) {
        listState.scrollToItem(initialIndex)
    }

    LazyRow(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        items(2000) { index ->
            val currentMonth = YearMonth.now().plusMonths((index - initialIndex).toLong())
            Box(modifier = Modifier.width(screenWidth)) {
                MonthContent(
                    yearMonth = currentMonth,
                    selectedDate = selectedDate,
                    today = today,
                    diaryMap = diaryMap,
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}


@Composable
fun MonthContent(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    diaryMap: Map<LocalDate, DiaryResponse>,
    onDateSelected: (LocalDate) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val firstDay = yearMonth.atDay(1)
    val lastDay = yearMonth.atEndOfMonth()
    val startDayOfWeek = DayOfWeek.SUNDAY
    val offset = (firstDay.dayOfWeek.value % 7 - startDayOfWeek.value % 7 + 7) % 7

    val allDays = buildList<LocalDate?> {
        repeat(offset) { add(null) }
        generateSequence(firstDay) { it.plusDays(1) }
            .takeWhile { !it.isAfter(lastDay) }
            .forEach { add(it) }
        while (size < 42) add(null)
    }

    val weeks = allDays.chunked(7)

    Column(
        modifier = Modifier
            .width(screenWidth)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 요일 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 날짜 셀
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val isSelected = date == selectedDate
                            val isToday = date == today
                            val hasDiary = diaryMap.containsKey(date)

                            DateCell(
                                date = date,
                                isSelected = isSelected,
                                isToday = isToday,
                                hasDiary = hasDiary,
                                onClick = { onDateSelected(date) }
                            )
                        } else {
                            // 빈 날짜 셀 처리 (안 깨지게 유지)
                            Spacer(modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


@Composable
fun DateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasDiary: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val backgroundColor = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        }

        val borderStroke = if (isToday && !isSelected)
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        else null

        val textColor = if (isSelected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurface

        Surface(
            modifier = Modifier
                .size(40.dp)
                .clickable { onClick() },
            shape = CircleShape,
            color = backgroundColor,
            border = borderStroke
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textColor
                    )
                )
            }
        }

        if (hasDiary) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .aspectRatio(1f)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )
        } else {
            Spacer(modifier = Modifier.height(10.dp)) // 빈 공간 대체
        }
    }
}