package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.components.currentYearMonth
import com.example.ui.components.dayOf
import com.example.ui.components.formatSelectedDayHeader
import com.example.ui.components.formatSignedAmount
import com.example.ui.components.formatTimeOnly
import com.example.ui.components.isSameDay
import com.example.ui.components.isSameYearMonth
import com.example.ui.components.uzbekMonthsCap
import com.example.ui.components.uzbekWeekdayShorts
import com.example.ui.components.yearOf
import com.example.ui.theme.LocalHisobimColors
import com.example.ui.viewmodel.HisobimUiState
import java.util.Calendar

private enum class CalendarScope(val label: String) {
    DAY("Kun"),
    WEEK("Hafta"),
    MONTH("Oy"),
    YEAR("Yil")
}

@Composable
fun CalendarScreen(
    uiState: HisobimUiState,
    onTransactionClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHisobimColors.current
    val todayCal = remember { Calendar.getInstance() }
    var selectedScope by remember { mutableStateOf(CalendarScope.MONTH) }
    var displayYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(todayCal.get(Calendar.DAY_OF_MONTH)) }

    val daysInMonth = remember(displayYear, displayMonth) {
        Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
            .getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // Offset so Monday = 0, ..., Sunday = 6
    val firstDayOffset = remember(displayYear, displayMonth) {
        val dow = Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
            .get(Calendar.DAY_OF_WEEK)
        // Calendar.MONDAY = 2 -> 0, Calendar.SUNDAY = 1 -> 6
        (dow + 5) % 7
    }

    val daysWithActivity = remember(uiState.allTransactions, displayYear, displayMonth) {
        uiState.allTransactions
            .filter { isSameYearMonth(it.dateMillis, displayYear, displayMonth) }
            .map { dayOf(it.dateMillis) }
            .toSet()
    }

    val visibleTransactions = remember(uiState.allTransactions, selectedScope, displayYear, displayMonth, selectedDay) {
        when (selectedScope) {
            CalendarScope.DAY, CalendarScope.MONTH -> uiState.allTransactions.filter {
                isSameDay(it.dateMillis, displayYear, displayMonth, selectedDay)
            }
            CalendarScope.WEEK -> {
                val targetCal = Calendar.getInstance().apply { set(displayYear, displayMonth, selectedDay) }
                val targetWeek = targetCal.get(Calendar.WEEK_OF_YEAR)
                uiState.allTransactions.filter { tx ->
                    val c = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
                    c.get(Calendar.YEAR) == displayYear && c.get(Calendar.WEEK_OF_YEAR) == targetWeek
                }
            }
            CalendarScope.YEAR -> uiState.allTransactions.filter { yearOf(it.dateMillis) == displayYear }
        }
    }

    val visibleNet = visibleTransactions.sumOf {
        if (it.transactionType == TransactionType.INCOME) it.amount else -it.amount
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Kalendar + Noyabr 2026
            item(key = "cal_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kalendar",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uzbekMonthsCap[displayMonth]} $displayYear",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }

            // 4-way Segmented Switch: Kun | Hafta | Oy | Yil
            item(key = "cal_scope_switch") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.segmentTrack)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalendarScope.entries.forEach { scope ->
                        val selected = selectedScope == scope
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) colors.primaryAccent else colors.segmentTrack)
                                .clickable { selectedScope = scope }
                                .testTag("cal_scope_${scope.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = scope.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) colors.onPrimaryAccent else colors.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Month Grid
            item(key = "cal_grid") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Weekday headers: Du Se Ch Pa Ju Sh Ya
                    Row(modifier = Modifier.fillMaxWidth()) {
                        uzbekWeekdayShorts.forEach { wd ->
                            Text(
                                text = wd,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    val totalCells = firstDayOffset + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0..6) {
                                val cellIdx = row * 7 + col
                                val dayNum = cellIdx - firstDayOffset + 1
                                if (dayNum in 1..daysInMonth) {
                                    val isSelected = dayNum == selectedDay
                                    val hasDot = daysWithActivity.contains(dayNum)
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedDay = dayNum }
                                            .padding(vertical = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) colors.primaryAccent
                                                    else colors.background
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNum.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isSelected) colors.onPrimaryAccent else colors.textPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (hasDot) colors.incomeText
                                                    else colors.background
                                                )
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Header + Net Sum
            item(key = "cal_day_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedScope) {
                            CalendarScope.YEAR -> "$displayYear-yil amallari"
                            CalendarScope.WEEK -> "Tanlangan hafta"
                            else -> formatSelectedDayHeader(displayYear, displayMonth, selectedDay)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (visibleTransactions.isNotEmpty()) {
                        Text(
                            text = formatSignedAmount(visibleNet, visibleNet >= 0L),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (visibleNet >= 0L) colors.incomeText else colors.expenseColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (visibleTransactions.isEmpty()) {
                item(key = "cal_empty") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = colors.card
                    ) {
                        Text(
                            text = "Bu sanada amallar qayd etilmagan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(visibleTransactions, key = { _, tx -> tx.id }) { idx, tx ->
                    val isIncome = tx.transactionType == TransactionType.INCOME
                    val subtitle = if (!isIncome && tx.itemsCount > 0) {
                        "${tx.category} · ${tx.itemsCount} ta mahsulot"
                    } else {
                        tx.category
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTransactionClick(tx) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(if (colors.isDark) CircleShape else RoundedCornerShape(14.dp))
                                .background(if (colors.isDark) colors.card else colors.iconBadgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatTimeOnly(tx.dateMillis),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Text(
                            text = formatSignedAmount(tx.amount, isIncome),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isIncome) colors.incomeText else colors.expenseColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (idx < visibleTransactions.lastIndex) {
                        HorizontalDivider(
                            color = colors.divider,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
