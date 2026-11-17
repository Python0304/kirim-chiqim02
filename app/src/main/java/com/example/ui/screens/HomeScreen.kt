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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.components.formatAmountPlain
import com.example.ui.components.formatFullGreetingDate
import com.example.ui.components.formatMoneyWithCurrency
import com.example.ui.components.formatSignedAmount
import com.example.ui.components.formatTransactionDateLabel
import com.example.ui.theme.LocalHisobimColors
import com.example.ui.viewmodel.HisobimUiState

@Composable
fun HomeScreen(
    uiState: HisobimUiState,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHisobimColors.current
    val currency = uiState.prefs.currencySymbol

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
            // Greeting Header (Pages 2 & 14)
            item(key = "greeting") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = formatFullGreetingDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Salom!",
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Hero Balance Section (Light = single emerald card; Dark = main card + 2 split cards)
            item(key = "balance_hero") {
                if (!colors.isDark) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_balance_card"),
                        shape = RoundedCornerShape(24.dp),
                        color = colors.primaryButton
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Oy qoldig'i",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.84f)
                                )
                                Text(
                                    text = formatMoneyWithCurrency(uiState.currentMonthBalance, currency),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Kirim",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.80f)
                                    )
                                    Text(
                                        text = formatAmountPlain(uiState.currentMonthIncome),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Chiqim",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.80f)
                                    )
                                    Text(
                                        text = formatAmountPlain(uiState.currentMonthExpense),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Dark mode layout (Page 14): 1 large top card + 2 side-by-side Kirim/Chiqim cards
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_balance_card"),
                            shape = RoundedCornerShape(24.dp),
                            color = colors.card
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Oy qoldig'i",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary
                                )
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = formatAmountPlain(uiState.currentMonthBalance),
                                        style = MaterialTheme.typography.displayLarge,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = currency,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(bottom = 5.dp)
                                    )
                                }
                            }
                        }

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
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Kirim",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = formatAmountPlain(uiState.currentMonthIncome),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = colors.incomeText,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                color = colors.card
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Chiqim",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = formatAmountPlain(uiState.currentMonthExpense),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = colors.expenseColor,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons: "- Chiqim" (left) and "+ Kirim" (right) matching Pages 2 & 14
            item(key = "action_buttons") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddExpenseClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("home_add_expense_button"),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.expenseColor,
                            contentColor = colors.onExpenseColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "- Chiqim",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onAddIncomeClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("home_add_income_button"),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryButton,
                            contentColor = colors.onPrimaryButton
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "+ Kirim",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Recent Transactions Header
            item(key = "recent_header") {
                Text(
                    text = "So'nggi amallar",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (uiState.allTransactions.isEmpty()) {
                item(key = "empty_recent") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = colors.card
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Hali amallar yo'q",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Kirim yoki chiqim qo'shish uchun yuqoridagi tugmalarni bosing",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            } else {
                val recentList = uiState.allTransactions.take(20)
                itemsIndexed(recentList, key = { _, item -> item.id }) { index, tx ->
                    HomeTransactionRow(
                        transaction = tx,
                        onClick = { onTransactionClick(tx) }
                    )
                    if (index < recentList.lastIndex) {
                        HorizontalDivider(
                            color = colors.divider,
                            thickness = 1.dp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HomeTransactionRow(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val colors = LocalHisobimColors.current
    val isIncome = transaction.transactionType == TransactionType.INCOME
    val dateLabel = formatTransactionDateLabel(transaction.dateMillis)
    val subtitle = if (!isIncome && transaction.itemsCount > 0) {
        "$dateLabel · ${transaction.itemsCount} ta mahsulot"
    } else {
        dateLabel
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag("home_tx_${transaction.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (!colors.isDark) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.iconBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = transaction.category,
                    tint = colors.iconBadgeTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colors.card),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                    contentDescription = transaction.category,
                    tint = if (isIncome) colors.incomeText else colors.expenseColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }

        Text(
            text = formatSignedAmount(transaction.amount, isIncome),
            style = MaterialTheme.typography.titleMedium,
            color = if (isIncome) colors.incomeText else colors.expenseColor,
            fontWeight = FontWeight.Bold
        )
    }
}
