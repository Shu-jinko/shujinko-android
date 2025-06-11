package com.shujinko.app.ui.screen

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
import java.time.*
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryCalendarScreen(
    onClickMore: (LocalDate) -> Unit,
    diaryMap: Map<LocalDate, DiaryResponse>,
    onMonthChange: (Int, Int) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isWeekView by remember { mutableStateOf(true) }
    var lastLoadedMonth by remember { mutableStateOf(Pair(0, 0)) }

    val normalizedDiaryMap = remember(diaryMap) {
        diaryMap.mapKeys { it.key } // 필요하면 .toLocalDate()
    }

    LaunchedEffect(selectedDate) {
        val year = selectedDate.year
        val month = selectedDate.monthValue
        if (lastLoadedMonth != Pair(year, month)) {
            onMonthChange(year, month)
            lastLoadedMonth = Pair(year, month)
        }
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // 좌우 여백
        ) {
            /** 상단 제목 + 토글 (스크롤 안 됨) **/
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CALENDAR", style = MaterialTheme.typography.headlineSmall)
                    SegmentedToggle(
                        isWeekView = isWeekView,
                        onToggle = { isWeekView = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            /** 달력 뷰 **/
            item {
                if (isWeekView) {
                    Text(
                        text = "${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)} ${selectedDate.year}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    HorizontalWeekCalendar(
                        selectedDate = selectedDate,
                        diaryMap = normalizedDiaryMap,
                        onDateSelected = { selectedDate = it }
                    )
                } else {
                    HorizontalMonthCalendar(
                        selectedDate = selectedDate,
                        diaryMap = normalizedDiaryMap,
                        onDateSelected = { selectedDate = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            /** 요약 카드 **/
            item {
                DiarySummaryCard(
                    selectedDate = selectedDate,
                    diary = normalizedDiaryMap[selectedDate],
                    onClickMore = onClickMore
                )

                Spacer(modifier = Modifier.height(32.dp)) // 마지막 바닥 여백
            }
        }
    }
}

@Composable
fun SegmentedToggle(isWeekView: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(4.dp)
    ) {
        listOf("주간", "월간").forEachIndexed { index, label ->
            val selected = (isWeekView && index == 0) || (!isWeekView && index == 1)
            Text(
                text = label,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                    .clickable { onToggle(index == 0) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DiarySummaryCard(
    selectedDate: LocalDate,
    diary: DiaryResponse?,
    onClickMore: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "📅 ${selectedDate}의 요약",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (diary != null) {
                Text("요약: ${diary.summary}", style = MaterialTheme.typography.bodyMedium)
                Text("감정: ${diary.label}", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("이 날짜에는 기록이 없습니다.", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onClickMore(selectedDate) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("더보기")
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

    val screenWidth = (LocalConfiguration.current.screenWidthDp.dp - 32.dp)

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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Row(
        modifier = Modifier
            .width(screenWidth)
            .padding(horizontal = 0.dp),
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
                    .height(80.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
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
    val screenWidth = (LocalConfiguration.current.screenWidthDp.dp - 32.dp)

    // 오늘 기준으로 현재 월 중앙 정렬
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
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
            .padding(16.dp)
    ) {
        Text(
            text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

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
                            DateCell(date, isSelected, isToday, hasDiary) {
                                onDateSelected(date)
                            }
                        }
                    }
                }
            }
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
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    if (hasDiary) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CircleShape
                )
        )
    }
}