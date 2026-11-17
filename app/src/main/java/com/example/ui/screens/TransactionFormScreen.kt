package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PurchaseItem
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.components.HisobimDefaults
import com.example.ui.components.currentYearMonth
import com.example.ui.components.formatAmountPlain
import com.example.ui.components.formatMoneyWithCurrency
import com.example.ui.components.formatShortDateTime
import com.example.ui.components.uzbekMonthsCap
import com.example.ui.theme.LocalHisobimColors
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.viewmodel.HisobimUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionFormScreen(
    uiState: HisobimUiState,
    initialType: TransactionType,
    existing: TransactionEntity?,
    onCancel: () -> Unit,
    onSaveExpense: (id: Long, title: String, category: String, items: List<PurchaseItem>, directAmount: Long, dateMillis: Long) -> Unit,
    onSaveIncome: (id: Long, source: String, amount: Long, dateMillis: Long) -> Unit,
    onDelete: ((TransactionEntity) -> Unit)? = null
) {
    val colors = LocalHisobimColors.current
    val currency = uiState.prefs.currencySymbol

    var mode by remember { mutableStateOf(existing?.transactionType ?: initialType) }
    var editingId by remember { mutableStateOf(existing?.id ?: 0L) }
    val dateMillis = remember { existing?.dateMillis ?: System.currentTimeMillis() }

    // Expense fields (Pages 3 & 15)
    var placeTitle by remember {
        mutableStateOf(if (existing?.transactionType == TransactionType.EXPENSE) existing.title else "")
    }
    var selectedExpenseCat by remember {
        mutableStateOf(
            if (existing?.transactionType == TransactionType.EXPENSE) existing.category
            else uiState.expenseCategories.firstOrNull() ?: "Oziq-ovqat"
        )
    }
    val purchaseItems = remember {
        mutableStateListOf<PurchaseItem>().apply {
            if (existing?.transactionType == TransactionType.EXPENSE) {
                addAll(existing.parsedItems())
            }
        }
    }
    var directExpenseAmountText by remember {
        mutableStateOf(
            if (existing?.transactionType == TransactionType.EXPENSE && existing.itemsCount == 0) {
                existing.amount.toString()
            } else ""
        )
    }
    var showAddItemDialog by remember { mutableStateOf(false) }

    // Income fields (Pages 4 & 16)
    var selectedIncomeSource by remember {
        mutableStateOf(
            if (existing?.transactionType == TransactionType.INCOME) existing.category
            else uiState.incomeCategories.firstOrNull() ?: "Oylik maosh"
        )
    }
    var incomeAmountText by remember {
        mutableStateOf(if (existing?.transactionType == TransactionType.INCOME) existing.amount.toString() else "")
    }
    var showValidationError by remember { mutableStateOf(false) }

    val computedExpenseTotal = if (purchaseItems.isNotEmpty()) {
        purchaseItems.sumOf { it.price }
    } else {
        directExpenseAmountText.toLongOrNull() ?: 0L
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .padding(horizontal = 20.dp)
        ) {
            // Top bar: Title + Bekor qilish
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (mode == TransactionType.EXPENSE) "Yangi chiqim" else "Yangi kirim",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (existing != null && onDelete != null) {
                        IconButton(
                            onClick = { onDelete(existing) },
                            modifier = Modifier.testTag("form_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "O'chirish",
                                tint = colors.expenseColor
                            )
                        }
                    }
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("form_cancel_button")
                    ) {
                        Text(
                            text = "Bekor qilish",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Segmented Toggle: Chiqim | Kirim
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.segmentTrack)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isExpense = mode == TransactionType.EXPENSE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isExpense) colors.expenseColor else colors.segmentTrack)
                        .clickable {
                            mode = TransactionType.EXPENSE
                            showValidationError = false
                        }
                        .testTag("form_segment_expense"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chiqim",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isExpense) colors.onExpenseColor else colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!isExpense) colors.primaryButton else colors.segmentTrack)
                        .clickable {
                            mode = TransactionType.INCOME
                            showValidationError = false
                        }
                        .testTag("form_segment_income"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Kirim",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (!isExpense) colors.onPrimaryButton else colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable form body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (mode == TransactionType.EXPENSE) {
                    // Place + DateTime row (Pages 3 & 15)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = placeTitle,
                            onValueChange = { placeTitle = it },
                            placeholder = { Text("Korzinka, Taksi…", color = colors.textSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.card,
                                unfocusedContainerColor = colors.card,
                                focusedBorderColor = colors.primaryAccent,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            modifier = Modifier
                                .weight(1.15f)
                                .testTag("expense_title_input")
                        )

                        Surface(
                            modifier = Modifier
                                .weight(0.95f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = colors.card,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = formatShortDateTime(dateMillis),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Category pills
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.expenseCategories.forEach { cat ->
                            val selected = selectedExpenseCat == cat
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { selectedExpenseCat = cat }
                                    .testTag("expense_cat_$cat"),
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

                    // Olingan mahsulotlar section
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Olingan mahsulotlar",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nomi, miqdori va narxi (dona, kg, l)",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    if (purchaseItems.isEmpty()) {
                        // Allow direct quick expense amount if no itemized products added yet
                        OutlinedTextField(
                            value = directExpenseAmountText,
                            onValueChange = {
                                directExpenseAmountText = it.filter { ch -> ch.isDigit() }.take(12)
                                showValidationError = false
                            },
                            label = { Text("Umumiy summa ($currency)") },
                            placeholder = { Text("Masalan: 32000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.card,
                                unfocusedContainerColor = colors.card,
                                focusedBorderColor = colors.primaryAccent,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_direct_amount_input")
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            purchaseItems.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = item.quantityDisplay,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Text(
                                        text = formatAmountPlain(item.price),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { purchaseItems.removeAt(index) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Olib tashlash",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(color = colors.divider)
                            }
                        }
                    }

                    TextButton(
                        onClick = { showAddItemDialog = true },
                        modifier = Modifier.testTag("add_purchase_item_button")
                    ) {
                        Text(
                            text = "+ Mahsulot qo'shish",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.primaryAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // ==================== YANGI KIRIM (Pages 4 & 16) ====================
                    Text(
                        text = "Manba",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.incomeCategories.forEach { source ->
                            val selected = selectedIncomeSource == source
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { selectedIncomeSource = source }
                                    .testTag("income_source_$source"),
                                shape = RoundedCornerShape(22.dp),
                                color = if (selected) colors.primaryAccent else colors.card,
                                border = if (selected) null else BorderStroke(1.dp, colors.border)
                            ) {
                                Text(
                                    text = source,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) colors.onPrimaryAccent else colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    // Centered Big Summa Input
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Summa",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )

                        val displayNum = incomeAmountText.toLongOrNull()?.let { formatAmountPlain(it) } ?: ""
                        BasicTextField(
                            value = incomeAmountText,
                            onValueChange = {
                                incomeAmountText = it.filter { ch -> ch.isDigit() }.take(12)
                                showValidationError = false
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                                color = if (colors.isDark) colors.incomeText else colors.textPrimary,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(colors.primaryAccent),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (incomeAmountText.isEmpty()) {
                                        Text(
                                            text = "0",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 40.sp,
                                            color = colors.textSecondary.copy(alpha = 0.4f)
                                        )
                                    } else {
                                        Text(
                                            text = displayNum,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 40.sp,
                                            color = if (colors.isDark) colors.incomeText else colors.textPrimary
                                        )
                                    }
                                    Box(modifier = Modifier.size(1.dp)) {
                                        innerTextField()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("income_amount_input")
                        )

                        Text(
                            text = "$currency · shu oyga qo'shiladi",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    // Monthly Incomes Card ("Noyabr kirimlari")
                    val (_, monthIdx) = currentYearMonth()
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = colors.card
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${uzbekMonthsCap[monthIdx]} kirimlari",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )

                            if (uiState.currentMonthIncomesList.isEmpty()) {
                                Text(
                                    text = "Bu oyda hali kirimlar kiritilmagan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            } else {
                                uiState.currentMonthIncomesList.forEach { inc ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = inc.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = formatAmountPlain(inc.amount),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = colors.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp)
                                        )
                                        Text(
                                            text = "Tahrir",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = colors.primaryAccent,
                                            modifier = Modifier
                                                .clickable {
                                                    editingId = inc.id
                                                    selectedIncomeSource = inc.category
                                                    incomeAmountText = inc.amount.toString()
                                                }
                                                .padding(vertical = 4.dp)
                                        )
                                    }
                                    HorizontalDivider(color = colors.divider)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Jami kirim",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatAmountPlain(uiState.currentMonthIncome),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.incomeText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (showValidationError) {
                    Text(
                        text = "Iltimos, 0 dan katta summa yoki kamida 1 ta mahsulot kiriting",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.expenseColor
                    )
                }
            }

            // Bottom Jami + Saqlash
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (mode == TransactionType.EXPENSE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jami",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                        Text(
                            text = formatMoneyWithCurrency(computedExpenseTotal, currency),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (colors.isDark) colors.expenseColor else colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        if (mode == TransactionType.EXPENSE) {
                            if (computedExpenseTotal <= 0L) {
                                showValidationError = true
                            } else {
                                onSaveExpense(
                                    editingId,
                                    placeTitle,
                                    selectedExpenseCat,
                                    purchaseItems.toList(),
                                    directExpenseAmountText.toLongOrNull() ?: 0L,
                                    dateMillis
                                )
                            }
                        } else {
                            val amt = incomeAmountText.toLongOrNull() ?: 0L
                            if (amt <= 0L) {
                                showValidationError = true
                            } else {
                                onSaveIncome(editingId, selectedIncomeSource, amt, dateMillis)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("form_save_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryButton,
                        contentColor = colors.onPrimaryButton
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Saqlash",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddPurchaseItemDialog(
            currency = currency,
            onDismiss = { showAddItemDialog = false },
            onAdd = { item ->
                purchaseItems.add(item)
                showAddItemDialog = false
                showValidationError = false
            }
        )
    }
}

@Composable
private fun AddPurchaseItemDialog(
    currency: String,
    onDismiss: () -> Unit,
    onAdd: (PurchaseItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("dona") }
    var priceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mahsulot qo'shish") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nomi") },
                    placeholder = { Text("Masalan: Non, Sut, Go'sht") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_name_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.take(6) },
                        label = { Text("Miqdori") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { ch -> ch.isDigit() }.take(10) },
                        label = { Text("Narxi ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("item_price_input")
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HisobimDefaults.units.forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toLongOrNull() ?: 0L
                    if (name.isNotBlank() && price > 0L) {
                        onAdd(PurchaseItem(name.trim(), quantity.ifBlank { "1" }, unit, price))
                    }
                },
                modifier = Modifier.testTag("confirm_add_item_button")
            ) {
                Text("Qo'shish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}
