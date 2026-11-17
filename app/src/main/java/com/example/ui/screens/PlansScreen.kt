package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlanItemEntity
import com.example.data.local.PlanPeriod
import com.example.ui.components.formatAmountPlain
import com.example.ui.components.formatMoneyWithCurrency
import com.example.ui.theme.LocalHisobimColors
import com.example.ui.viewmodel.HisobimUiState

@Composable
fun PlansScreen(
    uiState: HisobimUiState,
    onToggleChecked: (PlanItemEntity) -> Unit,
    onSavePlanItem: (title: String, quantityLabel: String, estimatedPrice: Long, period: PlanPeriod) -> Unit,
    onDeletePlanItem: (PlanItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHisobimColors.current
    val currency = uiState.prefs.currencySymbol
    var selectedPeriod by remember { mutableStateOf(PlanPeriod.WEEKLY) }
    var showAddDialog by remember { mutableStateOf(false) }

    val periodItems = remember(uiState.planItems, selectedPeriod) {
        uiState.planItems.filter { it.planPeriod == selectedPeriod }
    }
    val totalPlanned = periodItems.sumOf { it.estimatedPrice }
    val checkedCount = periodItems.count { it.isChecked }
    val progress = if (periodItems.isNotEmpty()) checkedCount.toFloat() / periodItems.size.toFloat() else 0f

    val periodSubtitle = when (selectedPeriod) {
        PlanPeriod.DAILY -> "Bugungi kun uchun reja"
        PlanPeriod.WEEKLY -> "Shu hafta uchun reja"
        PlanPeriod.MONTHLY -> "Shu oy uchun reja"
    }

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
                .padding(horizontal = 20.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "plans_title") {
                    Text(
                        text = "Rejalar",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3-way Segmented Switch: Kunlik | Haftalik | Oylik
                item(key = "plans_period_switch") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.segmentTrack)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PlanPeriod.entries.forEach { period ->
                            val selected = selectedPeriod == period
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) colors.primaryAccent else colors.segmentTrack)
                                    .clickable { selectedPeriod = period }
                                    .testTag("plan_period_${period.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = period.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) colors.onPrimaryAccent else colors.textSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Summary Card (Pages 10 & 22)
                item(key = "plans_summary_card") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = colors.card
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = periodSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = formatMoneyWithCurrency(totalPlanned, currency),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$checkedCount / ${periodItems.size} ta olindi",
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

                item(key = "plans_list_header") {
                    Text(
                        text = "Olinadigan narsalar",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (periodItems.isEmpty()) {
                    item(key = "plans_empty") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = colors.card
                        ) {
                            Text(
                                text = "Bu davr uchun hali reja qo'shilmagan. Pastdagi tugma orqali olinadigan narsalarni kiriting.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                } else {
                    itemsIndexed(periodItems, key = { _, item -> item.id }) { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleChecked(item) }
                                .padding(vertical = 6.dp)
                                .testTag("plan_item_${item.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Custom rounded checkbox matching Figma
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (item.isChecked) colors.primaryButton else colors.background)
                                    .border(
                                        width = if (item.isChecked) 0.dp else 1.5.dp,
                                        color = if (item.isChecked) colors.primaryButton else colors.border,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.isChecked) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Olindi",
                                        tint = colors.onPrimaryButton,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (item.isChecked) colors.textSecondary else colors.textPrimary,
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val detail = buildString {
                                    if (item.quantityLabel.isNotBlank()) append(item.quantityLabel)
                                    if (item.estimatedPrice > 0L) {
                                        if (isNotEmpty()) append(" · ")
                                        append(formatAmountPlain(item.estimatedPrice))
                                    }
                                }
                                if (detail.isNotBlank()) {
                                    Text(
                                        text = detail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary,
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeletePlanItem(item) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "O'chirish",
                                    tint = colors.textSecondary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (idx < periodItems.lastIndex) {
                            HorizontalDivider(
                                color = colors.divider,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }

            // Bottom "+ Reja qo'shish" pill button
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(54.dp)
                    .testTag("add_plan_button"),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryButton,
                    contentColor = colors.onPrimaryButton
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "+ Reja qo'shish",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showAddDialog) {
        AddPlanItemDialog(
            currency = currency,
            period = selectedPeriod,
            onDismiss = { showAddDialog = false },
            onSave = { title, qty, price ->
                onSavePlanItem(title, qty, price, selectedPeriod)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddPlanItemDialog(
    currency: String,
    period: PlanPeriod,
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${period.label} reja qo'shish") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nomi") },
                    placeholder = { Text("Masalan: Non, Go'sht, Sabzavot") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_title_input")
                )
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Miqdori") },
                    placeholder = { Text("Masalan: 2 dona, 1 kg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() }.take(10) },
                    label = { Text("Taxminiy narxi ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_price_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, qty.ifBlank { "1 dona" }, priceText.toLongOrNull() ?: 0L)
                    }
                },
                modifier = Modifier.testTag("confirm_add_plan_button")
            ) {
                Text("Saqlash")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}
