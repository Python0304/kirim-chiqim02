package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.currentYearMonth
import com.example.ui.components.formatAmountPlain
import com.example.ui.components.formatTransactionDateLabel
import com.example.ui.components.uzbekMonthInitials
import com.example.ui.components.uzbekMonthsCap
import com.example.ui.theme.LocalHisobimColors
import com.example.ui.viewmodel.CategoryReportRow
import com.example.ui.viewmodel.HisobimUiState
import com.example.ui.viewmodel.MonthlyBarPoint
import kotlin.math.roundToInt

private enum class ReportTabMode(val label: String) {
    MONTH("Oy"),
    YEAR("Yil")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    uiState: HisobimUiState,
    modifier: Modifier = Modifier
) {
    val colors = LocalHisobimColors.current
    val (curYear, curMonth) = remember { currentYearMonth() }

    var mode by remember { mutableStateOf(ReportTabMode.MONTH) }
    var selectedMonth by remember { mutableIntStateOf(curMonth) }
    var selectedYear by remember { mutableIntStateOf(curYear) }
    var inspectingCategory by remember { mutableStateOf<CategoryReportRow?>(null) }

    // Recent 4 months for pills (e.g., Noyabr, Oktabr, Sentabr, Avgust)
    val recentMonths = remember(curMonth) {
        (0..5).map { offset -> ((curMonth - offset) + 12) % 12 }
    }
    val yearOptions = remember(curYear) { listOf(curYear, curYear - 1) }

    val breakdown = remember(uiState.allTransactions, selectedYear, selectedMonth) {
        uiState.monthlyBreakdown(selectedYear, selectedMonth)
    }
    val monthTotalExpense = remember(uiState.allTransactions, selectedYear, selectedMonth) {
        uiState.monthlyExpenseTotal(selectedYear, selectedMonth)
    }
    val yearlyBars = remember(uiState.allTransactions, selectedYear) {
        uiState.yearlyBars(selectedYear)
    }
    val yearlySummary = remember(uiState.allTransactions, uiState.charities, selectedYear) {
        uiState.yearlySummary(selectedYear)
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
            item(key = "report_title") {
                Text(
                    text = if (mode == ReportTabMode.MONTH) "Oy hisoboti" else "Yillik hisobot",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Oy | Yil Segmented Switch (Pages 12 & 24)
            item(key = "report_mode_switch") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.segmentTrack)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ReportTabMode.entries.forEach { tab ->
                        val selected = mode == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) colors.primaryAccent else colors.segmentTrack)
                                .clickable { mode = tab }
                                .testTag("report_mode_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) colors.onPrimaryAccent else colors.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (mode == ReportTabMode.MONTH) {
                // Month selector pills (Noyabr, Oktabr, Sentabr...)
                item(key = "month_pills") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(recentMonths) { mIdx ->
                            val selected = selectedMonth == mIdx
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { selectedMonth = mIdx },
                                shape = RoundedCornerShape(22.dp),
                                color = if (selected) colors.primaryAccent else colors.card,
                                border = if (selected) null else BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = uzbekMonthsCap[mIdx],
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) colors.onPrimaryAccent else colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                // Donut Chart (Pages 5 & 17)
                item(key = "donut_chart") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(190.dp)) {
                            val strokeW = 28.dp.toPx()
                            val diameter = size.minDimension - strokeW
                            val topLeft = Offset(strokeW / 2f, strokeW / 2f)
                            val arcSize = Size(diameter, diameter)

                            if (breakdown.isEmpty()) {
                                drawArc(
                                    color = colors.divider,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeW)
                                )
                            } else {
                                var start = -90f
                                breakdown.forEachIndexed { idx, row ->
                                    val sweep = (row.percentage * 360f).coerceAtLeast(3f)
                                    val color = colors.chartPalette[idx % colors.chartPalette.size]
                                    drawArc(
                                        color = color,
                                        startAngle = start,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeW, cap = StrokeCap.Butt)
                                    )
                                    start += sweep
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Chiqim",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = formatAmountPlain(monthTotalExpense),
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item(key = "category_hint") {
                    Text(
                        text = "Kategoriyani bossangiz, olingan narsalar ko'rinadi",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (breakdown.isEmpty()) {
                    item(key = "empty_month_report") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = colors.card
                        ) {
                            Text(
                                text = "${uzbekMonthsCap[selectedMonth]} oyida chiqimlar qayd etilmagan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            )
                        }
                    }
                } else {
                    itemsIndexed(breakdown, key = { _, row -> row.category }) { idx, row ->
                        val dotColor = colors.chartPalette[idx % colors.chartPalette.size]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { inspectingCategory = row }
                                .padding(vertical = 8.dp)
                                .testTag("report_cat_${row.category}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(if (colors.isDark) CircleShape else RoundedCornerShape(4.dp))
                                    .background(dotColor)
                            )
                            Text(
                                text = row.category,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatAmountPlain(row.totalAmount),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(row.percentage * 100f).roundToInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                modifier = Modifier.widthIn(min = 38.dp),
                                textAlign = TextAlign.End
                            )
                        }
                        if (idx < breakdown.lastIndex) {
                            HorizontalDivider(color = colors.divider)
                        }
                    }
                }
            } else {
                // ==================== YILLIK HISOBOT (Pages 12 & 24) ====================
                item(key = "year_pills") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        yearOptions.forEach { yr ->
                            val selected = selectedYear == yr
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { selectedYear = yr },
                                shape = RoundedCornerShape(22.dp),
                                color = if (selected) colors.primaryAccent else colors.card,
                                border = if (selected) null else BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = yr.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) colors.onPrimaryAccent else colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                // Legend + 12-Month Dual Bar Chart
                item(key = "yearly_bar_chart") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.incomeText)
                                )
                                Text("Kirim", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.expenseColor)
                                )
                                Text("Chiqim", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                        }

                        YearlyBarChartCanvas(
                            bars = yearlyBars,
                            incomeColor = colors.incomeText,
                            expenseColor = colors.expenseColor,
                            emptyBarColor = colors.divider
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            uzbekMonthInitials.forEach { letter ->
                                Text(
                                    text = letter,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Yearly summary table matching Pages 12 & 24
                item(key = "yearly_summary_rows") {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        YearlySummaryRow("Jami kirim", formatAmountPlain(yearlySummary.totalIncome), colors.incomeText)
                        HorizontalDivider(color = colors.divider)
                        YearlySummaryRow("Jami chiqim", formatAmountPlain(yearlySummary.totalExpense), colors.expenseColor)
                        HorizontalDivider(color = colors.divider)
                        YearlySummaryRow("Jamg'arma", formatAmountPlain(yearlySummary.savings), colors.textPrimary)
                        HorizontalDivider(color = colors.divider)
                        YearlySummaryRow("Hayriya", formatAmountPlain(yearlySummary.charity), colors.textPrimary)
                        HorizontalDivider(color = colors.divider)
                        YearlySummaryRow(
                            label = "Eng ko'p xarajat oyi: ${yearlySummary.highestExpenseMonthName}",
                            value = formatAmountPlain(yearlySummary.highestExpenseAmount),
                            valueColor = colors.textPrimary
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Category drill-down bottom sheet ("Kategoriyani bossangiz, olingan narsalar ko'rinadi")
    if (inspectingCategory != null) {
        val catRow = inspectingCategory!!
        ModalBottomSheet(
            onDismissRequest = { inspectingCategory = null },
            containerColor = colors.card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = catRow.category,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatAmountPlain(catRow.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.expenseColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                catRow.transactions.forEach { tx ->
                    val items = tx.parsedItems()
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = colors.background
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${tx.title} · ${formatTransactionDateLabel(tx.dateMillis)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formatAmountPlain(tx.amount),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.expenseColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items.forEach { pi ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${pi.name} (${pi.quantityDisplay})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = formatAmountPlain(pi.price),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun YearlyBarChartCanvas(
    bars: List<MonthlyBarPoint>,
    incomeColor: androidx.compose.ui.graphics.Color,
    expenseColor: androidx.compose.ui.graphics.Color,
    emptyBarColor: androidx.compose.ui.graphics.Color
) {
    val maxVal = remember(bars) {
        bars.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1L) ?: 1L
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val groupWidth = size.width / 12f
        val barWidth = (groupWidth * 0.30f).coerceAtLeast(4.dp.toPx())
        val gap = (groupWidth * 0.08f).coerceAtLeast(2.dp.toPx())
        val radius = CornerRadius(barWidth / 2f, barWidth / 2f)

        bars.forEachIndexed { idx, point ->
            val groupLeft = idx * groupWidth + (groupWidth - (barWidth * 2f + gap)) / 2f

            val incHeight = if (point.income > 0L) {
                (point.income.toFloat() / maxVal.toFloat() * size.height).coerceAtLeast(8.dp.toPx())
            } else 4.dp.toPx()

            val expHeight = if (point.expense > 0L) {
                (point.expense.toFloat() / maxVal.toFloat() * size.height).coerceAtLeast(8.dp.toPx())
            } else 4.dp.toPx()

            drawRoundRect(
                color = if (point.income > 0L) incomeColor else emptyBarColor,
                topLeft = Offset(groupLeft, size.height - incHeight),
                size = Size(barWidth, incHeight),
                cornerRadius = radius
            )

            drawRoundRect(
                color = if (point.expense > 0L) expenseColor else emptyBarColor,
                topLeft = Offset(groupLeft + barWidth + gap, size.height - expHeight),
                size = Size(barWidth, expHeight),
                cornerRadius = radius
            )
        }
    }
}

@Composable
private fun YearlySummaryRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    val colors = LocalHisobimColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}
