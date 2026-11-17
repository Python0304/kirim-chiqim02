package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CharityEntity
import com.example.data.local.CustomCategoryEntity
import com.example.data.local.DebtDirection
import com.example.data.local.DebtEntity
import com.example.data.local.TransactionType
import com.example.ui.components.HisobimDefaults
import com.example.ui.components.currentYearMonth
import com.example.ui.components.formatAmountPlain
import com.example.ui.components.formatCharityBadge
import com.example.ui.components.formatCharityDateTime
import com.example.ui.components.formatMoneyWithCurrency
import com.example.ui.components.uzbekMonthsCap
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LocalHisobimColors
import com.example.ui.viewmodel.HisobimUiState

enum class MoreSection(val label: String) {
    DEBTS("Qarzlar"),
    CHARITY("Hayriya"),
    SETTINGS("Sozlamalar")
}

@Composable
fun MoreHubScreen(
    uiState: HisobimUiState,
    onSaveDebt: (person: String, dir: DebtDirection, total: Long, dueLabel: String) -> Unit,
    onRecordDebtPayment: (DebtEntity, Long) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    onSaveCharity: (title: String, category: String, amount: Long) -> Unit,
    onDeleteCharity: (CharityEntity) -> Unit,
    onSetCharityGoal: (Long) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onLockApp: () -> Unit,
    onAddCustomCategory: (String, TransactionType) -> Unit,
    onDeleteCustomCategory: (CustomCategoryEntity) -> Unit,
    onSetCurrency: (String, String) -> Unit,
    onPerformBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHisobimColors.current
    var section by rememberSaveable { mutableStateOf(MoreSection.DEBTS) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            // Top 3-way pill bar for Qarzlar | Hayriya | Sozlamalar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.segmentTrack)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MoreSection.entries.forEach { sec ->
                    val selected = section == sec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) colors.primaryAccent else colors.segmentTrack)
                            .clickable { section = sec }
                            .testTag("more_section_${sec.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sec.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) colors.onPrimaryAccent else colors.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            when (section) {
                MoreSection.DEBTS -> DebtsSection(
                    uiState = uiState,
                    onSaveDebt = onSaveDebt,
                    onRecordPayment = onRecordDebtPayment,
                    onDeleteDebt = onDeleteDebt,
                    modifier = Modifier.weight(1f)
                )

                MoreSection.CHARITY -> CharitySection(
                    uiState = uiState,
                    onSaveCharity = onSaveCharity,
                    onDeleteCharity = onDeleteCharity,
                    onSetGoal = onSetCharityGoal,
                    modifier = Modifier.weight(1f)
                )

                MoreSection.SETTINGS -> SettingsSection(
                    uiState = uiState,
                    onSetThemeMode = onSetThemeMode,
                    onUpdatePassword = onUpdatePassword,
                    onLockApp = onLockApp,
                    onAddCustomCategory = onAddCustomCategory,
                    onDeleteCustomCategory = onDeleteCustomCategory,
                    onSetCurrency = onSetCurrency,
                    onPerformBackup = onPerformBackup,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ==================== QARZLAR (Pages 8 & 20) ====================
@Composable
private fun DebtsSection(
    uiState: HisobimUiState,
    onSaveDebt: (String, DebtDirection, Long, String) -> Unit,
    onRecordPayment: (DebtEntity, Long) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHisobimColors.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDebtForPayment by remember { mutableStateOf<DebtEntity?>(null) }

    val givenDebts = uiState.debts.filter { it.debtDirection == DebtDirection.GIVEN }
    val takenDebts = uiState.debts.filter { it.debtDirection == DebtDirection.TAKEN }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "debts_title") {
                Text(
                    text = "Qarzlar",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Summary cards: Menga qaytariladi | Men qaytaraman
            item(key = "debts_summary") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = colors.card
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Menga qaytariladi",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = formatAmountPlain(uiState.totalReceivable),
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.incomeText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = colors.card
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Men qaytaraman",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = formatAmountPlain(uiState.totalPayable),
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.expenseColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item(key = "given_header") {
                Text(
                    text = "Men qarz berganlar",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (givenDebts.isEmpty()) {
                item(key = "given_empty") {
                    Text(
                        text = "Hozircha qarz berilgan yozuv yo'q",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            } else {
                itemsIndexed(givenDebts, key = { _, d -> d.id }) { idx, debt ->
                    DebtRow(
                        debt = debt,
                        isGiven = true,
                        onClick = { selectedDebtForPayment = debt }
                    )
                    if (idx < givenDebts.lastIndex) {
                        HorizontalDivider(color = colors.divider, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            item(key = "taken_header") {
                Text(
                    text = "Men qarz olganlar",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (takenDebts.isEmpty()) {
                item(key = "taken_empty") {
                    Text(
                        text = "Hozircha qarz olingan yozuv yo'q",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            } else {
                itemsIndexed(takenDebts, key = { _, d -> d.id }) { idx, debt ->
                    DebtRow(
                        debt = debt,
                        isGiven = false,
                        onClick = { selectedDebtForPayment = debt }
                    )
                    if (idx < takenDebts.lastIndex) {
                        HorizontalDivider(color = colors.divider, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(54.dp)
                .testTag("add_debt_button"),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryButton,
                contentColor = colors.onPrimaryButton
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("+ Qarz qo'shish", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    if (showAddDialog) {
        AddDebtDialog(
            currency = uiState.prefs.currencySymbol,
            onDismiss = { showAddDialog = false },
            onSave = { name, dir, total, due ->
                onSaveDebt(name, dir, total, due)
                showAddDialog = false
            }
        )
    }

    if (selectedDebtForPayment != null) {
        val debt = selectedDebtForPayment!!
        DebtPaymentDialog(
            debt = debt,
            currency = uiState.prefs.currencySymbol,
            onDismiss = { selectedDebtForPayment = null },
            onAddPayment = { amt ->
                onRecordPayment(debt, amt)
                selectedDebtForPayment = null
            },
            onDelete = {
                onDeleteDebt(debt)
                selectedDebtForPayment = null
            }
        )
    }
}

@Composable
private fun DebtRow(
    debt: DebtEntity,
    isGiven: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalHisobimColors.current
    val initial = debt.personName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val dueText = debt.dueDateLabel.ifBlank { "muddat yo'q" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(if (colors.isDark) CircleShape else RoundedCornerShape(14.dp))
                .background(if (colors.isDark) colors.card else colors.iconBadgeBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                color = if (colors.isDark) colors.primaryAccent else colors.iconBadgeTint,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = debt.personName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatAmountPlain(debt.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isGiven) colors.incomeText else colors.expenseColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Qoldi ${formatAmountPlain(debt.remainingAmount)} · $dueText",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            if (debt.paidAmount > 0L) {
                LinearProgressIndicator(
                    progress = { debt.repaymentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = colors.primaryButton,
                    trackColor = colors.divider
                )
            }
        }
    }
}

@Composable
private fun AddDebtDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (String, DebtDirection, Long, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueLabel by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(DebtDirection.GIVEN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yangi qarz") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = direction == DebtDirection.GIVEN,
                        onClick = { direction = DebtDirection.GIVEN },
                        label = { Text("Qarz berdim") }
                    )
                    FilterChip(
                        selected = direction == DebtDirection.TAKEN,
                        onClick = { direction = DebtDirection.TAKEN },
                        label = { Text("Qarz oldim") }
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kishi ismi") },
                    placeholder = { Text("Masalan: Aziz") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() }.take(12) },
                    label = { Text("Summa ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueLabel,
                    onValueChange = { dueLabel = it },
                    label = { Text("Muddat (ixtiyoriy)") },
                    placeholder = { Text("Masalan: 25-noyabrgacha") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (name.isNotBlank() && amt > 0L) {
                        onSave(name, direction, amt, dueLabel)
                    }
                }
            ) { Text("Saqlash") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Bekor qilish") }
        }
    )
}

@Composable
private fun DebtPaymentDialog(
    debt: DebtEntity,
    currency: String,
    onDismiss: () -> Unit,
    onAddPayment: (Long) -> Unit,
    onDelete: () -> Unit
) {
    var payText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${debt.personName} — to'lov") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Qolgan summa: ${formatMoneyWithCurrency(debt.remainingAmount, currency)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = payText,
                    onValueChange = { payText = it.filter { ch -> ch.isDigit() }.take(12) },
                    label = { Text("Qaytarilgan summa ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = payText.toLongOrNull() ?: 0L
                    if (amt > 0L) onAddPayment(amt)
                }
            ) { Text("Qo'shish") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("O'chirish") }
                TextButton(onClick = onDismiss) { Text("Yopish") }
            }
        }
    )
}

// ==================== HAYRIYA (Pages 11 & 23) ====================
@Composable
private fun CharitySection(
    uiState: HisobimUiState,
    onSaveCharity: (String, String, Long) -> Unit,
    onDeleteCharity: (CharityEntity) -> Unit,
    onSetGoal: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHisobimColors.current
    val currency = uiState.prefs.currencySymbol
    val (curYear, curMonth) = remember { currentYearMonth() }

    var selectedFilter by remember { mutableStateOf("Hammasi") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    val filteredCharities = remember(uiState.charities, selectedFilter) {
        if (selectedFilter == "Hammasi") uiState.charities
        else uiState.charities.filter { it.category == selectedFilter }
    }

    val goal = uiState.prefs.monthlyCharityGoal
    val progress = if (goal > 0L) {
        (uiState.currentMonthCharityTotal.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "charity_title") {
                Text(
                    text = "Hayriya",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Monthly Charity Plan Card
            item(key = "charity_plan_card") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGoalDialog = true },
                    shape = RoundedCornerShape(22.dp),
                    color = colors.card
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${uzbekMonthsCap[curMonth]} uchun hayriya rejasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = formatAmountPlain(uiState.currentMonthCharityTotal),
                                style = MaterialTheme.typography.headlineLarge,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "reja: ${formatMoneyWithCurrency(goal, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colors.primaryButton,
                            trackColor = colors.divider
                        )
                    }
                }
            }

            // Filter Pills: Hammasi, Sadaqa, Zakot, Vaqf
            item(key = "charity_filters") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HisobimDefaults.charityCategories) { cat ->
                        val selected = selectedFilter == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { selectedFilter = cat },
                            shape = RoundedCornerShape(22.dp),
                            color = if (selected) colors.primaryAccent else colors.card,
                            border = if (selected) null else BorderStroke(1.dp, colors.border)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) colors.onPrimaryAccent else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            item(key = "charity_list_header") {
                Text(
                    text = "So'nggi hayriyalar",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (filteredCharities.isEmpty()) {
                item(key = "charity_empty") {
                    Text(
                        text = "Hozircha hayriya yozuvlari yo'q",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            } else {
                itemsIndexed(filteredCharities, key = { _, c -> c.id }) { idx, ch ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(if (colors.isDark) CircleShape else RoundedCornerShape(14.dp))
                                .background(if (colors.isDark) colors.card else colors.iconBadgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatCharityBadge(ch.dateMillis),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ch.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${ch.category} · ${formatCharityDateTime(ch.dateMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Text(
                            text = formatAmountPlain(ch.amount),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.incomeText,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { onDeleteCharity(ch) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "O'chirish",
                                tint = colors.textSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (idx < filteredCharities.lastIndex) {
                        HorizontalDivider(color = colors.divider, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }

            item(key = "charity_year_total") {
                HorizontalDivider(color = colors.divider)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$curYear-yil davomida jami",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = formatAmountPlain(uiState.currentYearCharityTotal),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(54.dp)
                .testTag("add_charity_button"),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryButton,
                contentColor = colors.onPrimaryButton
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("+ Hayriya qo'shish", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Sadaqa") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Hayriya qo'shish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HisobimDefaults.editableCharityCategories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Maqsad / Nomi") },
                        placeholder = { Text("Masalan: Masjid, Yetimlar uchun") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() }.take(12) },
                        label = { Text("Summa ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toLongOrNull() ?: 0L
                        if (title.isNotBlank() && amt > 0L) {
                            onSaveCharity(title, category, amt)
                            showAddDialog = false
                        }
                    }
                ) { Text("Saqlash") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Bekor qilish") }
            }
        )
    }

    if (showGoalDialog) {
        var goalText by remember { mutableStateOf(goal.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Oylik hayriya rejasi") },
            text = {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter { ch -> ch.isDigit() }.take(12) },
                    label = { Text("Maqsad summa ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetGoal(goalText.toLongOrNull() ?: 0L)
                        showGoalDialog = false
                    }
                ) { Text("Saqlash") }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Bekor qilish") }
            }
        )
    }
}

// ==================== SOZLAMALAR (Pages 6 & 18) ====================
@Composable
private fun SettingsSection(
    uiState: HisobimUiState,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onLockApp: () -> Unit,
    onAddCustomCategory: (String, TransactionType) -> Unit,
    onDeleteCustomCategory: (CustomCategoryEntity) -> Unit,
    onSetCurrency: (String, String) -> Unit,
    onPerformBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHisobimColors.current
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showCategoriesDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    val totalCategoriesCount = uiState.expenseCategories.size + uiState.incomeCategories.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "settings_title") {
            Text(
                text = "Sozlamalar",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // Ko'rinish Card: Kun | Tun | Avto (Pages 6 & 18)
        item(key = "appearance_card") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = colors.card
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Ko'rinish",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kun yoki tun rejimini tanlang",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.segmentTrack)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            val selected = uiState.prefs.themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) colors.primaryAccent else colors.segmentTrack)
                                    .clickable { onSetThemeMode(mode) }
                                    .testTag("theme_mode_${mode.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) colors.onPrimaryAccent else colors.textSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Settings List Rows matching Pages 6 & 18
        item(key = "settings_rows") {
            Column {
                SettingsRow(
                    title = "Parol va xavfsizlik",
                    subtitle = if (uiState.prefs.password.isEmpty()) "Parol o'rnatilmagan" else "Parolni o'zgartirish",
                    onClick = { showPasswordDialog = true },
                    testTag = "settings_password_row"
                )
                HorizontalDivider(color = colors.divider)
                SettingsRow(
                    title = "Kategoriyalar",
                    subtitle = "$totalCategoriesCount ta",
                    onClick = { showCategoriesDialog = true },
                    testTag = "settings_categories_row"
                )
                HorizontalDivider(color = colors.divider)
                SettingsRow(
                    title = "Valyuta",
                    subtitle = uiState.prefs.currencyDisplay,
                    onClick = { showCurrencyDialog = true },
                    testTag = "settings_currency_row"
                )
                HorizontalDivider(color = colors.divider)
                SettingsRow(
                    title = "Zaxira nusxa",
                    subtitle = uiState.prefs.lastBackupLabel,
                    onClick = onPerformBackup,
                    testTag = "settings_backup_row"
                )
                HorizontalDivider(color = colors.divider)
                SettingsRow(
                    title = "Til",
                    subtitle = "O'zbekcha",
                    onClick = { },
                    testTag = "settings_language_row"
                )
            }
        }
    }

    if (showPasswordDialog) {
        var newPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Parol va xavfsizlik") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("Yangi parol (kamida 4 belgi)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.prefs.password.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                showPasswordDialog = false
                                onLockApp()
                            }
                        ) { Text("Ilovani hozir qulflash") }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPass.length >= 4) {
                            onUpdatePassword(newPass)
                            showPasswordDialog = false
                        }
                    }
                ) { Text("Saqlash") }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text("Bekor qilish") }
            }
        )
    }

    if (showCategoriesDialog) {
        var newCatName by remember { mutableStateOf("") }
        var newCatType by remember { mutableStateOf(TransactionType.EXPENSE) }

        AlertDialog(
            onDismissRequest = { showCategoriesDialog = false },
            title = { Text("Kategoriyalar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = newCatType == TransactionType.EXPENSE,
                            onClick = { newCatType = TransactionType.EXPENSE },
                            label = { Text("Chiqim") }
                        )
                        FilterChip(
                            selected = newCatType == TransactionType.INCOME,
                            onClick = { newCatType = TransactionType.INCOME },
                            label = { Text("Kirim") }
                        )
                    }
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Yangi kategoriya nomi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.customCategories.isNotEmpty()) {
                        uiState.customCategories.forEach { custom ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(custom.name, style = MaterialTheme.typography.bodyMedium)
                                IconButton(
                                    onClick = { onDeleteCustomCategory(custom) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "O'chirish",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            onAddCustomCategory(newCatName, newCatType)
                            newCatName = ""
                        }
                    }
                ) { Text("Qo'shish") }
            },
            dismissButton = {
                TextButton(onClick = { showCategoriesDialog = false }) { Text("Yopish") }
            }
        )
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Valyutani tanlang") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HisobimDefaults.currencyOptions.forEach { (sym, display) ->
                        val selected = uiState.prefs.currencySymbol == sym
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSetCurrency(sym, display)
                                    showCurrencyDialog = false
                                },
                            color = if (selected) colors.primaryAccent.copy(alpha = 0.18f) else colors.background
                        ) {
                            Text(
                                text = display,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Yopish") }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = LocalHisobimColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
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
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary
        )
    }
}
