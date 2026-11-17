package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.LedgerRepository
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.PlansScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.TransactionFormScreen
import com.example.ui.theme.HisobimTheme
import com.example.ui.theme.LocalHisobimColors
import com.example.ui.viewmodel.LedgerViewModel
import kotlinx.coroutines.launch

private enum class HisobimTab(val label: String) {
    HOME("Bosh"),
    CALENDAR("Kalendar"),
    PLANS("Rejalar"),
    REPORTS("Hisobot"),
    MORE("Ko'proq")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HisobimAppRoot()
        }
    }
}

@Composable
fun HisobimAppRoot() {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) {
        val db = AppDatabase.getInstance(context)
        LedgerRepository(
            transactionDao = db.transactionDao(),
            debtDao = db.debtDao(),
            planItemDao = db.planItemDao(),
            charityDao = db.charityDao(),
            customCategoryDao = db.customCategoryDao(),
            preferencesRepository = UserPreferencesRepository(context)
        )
    }
    val viewModel: LedgerViewModel = viewModel(
        factory = remember(repository) { LedgerViewModel.Factory(repository) }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()

    HisobimTheme(themeMode = uiState.prefs.themeMode) {
        if (!isUnlocked) {
            LockScreen(
                savedPassword = uiState.prefs.password,
                onUnlock = viewModel::unlock,
                onCreatePassword = viewModel::createOrUpdatePassword
            )
        } else {
            HisobimMainShell(viewModel = viewModel, uiState = uiState)
        }
    }
}

@Composable
private fun HisobimMainShell(
    viewModel: LedgerViewModel,
    uiState: com.example.ui.viewmodel.HisobimUiState
) {
    val colors = LocalHisobimColors.current
    var currentTab by rememberSaveable { mutableStateOf(HisobimTab.HOME) }
    var activeFormType by remember { mutableStateOf<TransactionType?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (activeFormType != null) {
        BackHandler {
            activeFormType = null
            editingTransaction = null
        }
        TransactionFormScreen(
            uiState = uiState,
            initialType = activeFormType!!,
            existing = editingTransaction,
            onCancel = {
                activeFormType = null
                editingTransaction = null
            },
            onSaveExpense = { id, title, cat, items, directAmt, dateMillis ->
                viewModel.saveExpense(id, title, cat, items, directAmt, dateMillis)
                activeFormType = null
                editingTransaction = null
            },
            onSaveIncome = { id, source, amount, dateMillis ->
                viewModel.saveIncome(id, source, amount, dateMillis)
                activeFormType = null
                editingTransaction = null
            },
            onDelete = { tx ->
                viewModel.deleteTransaction(tx)
                activeFormType = null
                editingTransaction = null
                scope.launch { snackbarHostState.showSnackbar("Amal o'chirildi") }
            }
        )
        return
    }

    if (currentTab != HisobimTab.HOME) {
        BackHandler { currentTab = HisobimTab.HOME }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colors.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    HisobimBottomBar(
                        currentTab = currentTab,
                        onSelectTab = { currentTab = it }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = colors.card
                    ) {
                        HisobimTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {},
                                label = {
                                    Text(
                                        text = tab.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) colors.primaryAccent else colors.textSecondary
                                    )
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Crossfade(targetState = currentTab, label = "hisobim_tab_crossfade") { tab ->
                        when (tab) {
                            HisobimTab.HOME -> HomeScreen(
                                uiState = uiState,
                                onAddExpenseClick = {
                                    editingTransaction = null
                                    activeFormType = TransactionType.EXPENSE
                                },
                                onAddIncomeClick = {
                                    editingTransaction = null
                                    activeFormType = TransactionType.INCOME
                                },
                                onTransactionClick = { tx ->
                                    editingTransaction = tx
                                    activeFormType = tx.transactionType
                                }
                            )

                            HisobimTab.CALENDAR -> CalendarScreen(
                                uiState = uiState,
                                onTransactionClick = { tx ->
                                    editingTransaction = tx
                                    activeFormType = tx.transactionType
                                }
                            )

                            HisobimTab.PLANS -> PlansScreen(
                                uiState = uiState,
                                onToggleChecked = viewModel::togglePlanChecked,
                                onSavePlanItem = { title, qty, price, period ->
                                    viewModel.savePlanItem(
                                        title = title,
                                        quantityLabel = qty,
                                        estimatedPrice = price,
                                        period = period
                                    )
                                },
                                onDeletePlanItem = viewModel::deletePlanItem
                            )

                            HisobimTab.REPORTS -> ReportsScreen(uiState = uiState)

                            HisobimTab.MORE -> MoreHubScreen(
                                uiState = uiState,
                                onSaveDebt = { person, dir, total, due ->
                                    viewModel.saveDebt(
                                        personName = person,
                                        direction = dir,
                                        totalAmount = total,
                                        dueDateLabel = due
                                    )
                                },
                                onRecordDebtPayment = viewModel::recordDebtPayment,
                                onDeleteDebt = viewModel::deleteDebt,
                                onSaveCharity = { title, cat, amt ->
                                    viewModel.saveCharity(title = title, category = cat, amount = amt)
                                },
                                onDeleteCharity = viewModel::deleteCharity,
                                onSetCharityGoal = viewModel::setMonthlyCharityGoal,
                                onSetThemeMode = viewModel::setThemeMode,
                                onUpdatePassword = viewModel::createOrUpdatePassword,
                                onLockApp = viewModel::lock,
                                onAddCustomCategory = viewModel::addCustomCategory,
                                onDeleteCustomCategory = viewModel::deleteCustomCategory,
                                onSetCurrency = viewModel::setCurrency,
                                onPerformBackup = {
                                    viewModel.performLocalBackup()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Mahalliy zaxira nusxa yangilandi")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Minimalist text-only bottom navigation bar matching the Figma PDF (Pages 9-12 & 21-24).
 */
@Composable
private fun HisobimBottomBar(
    currentTab: HisobimTab,
    onSelectTab: (HisobimTab) -> Unit
) {
    val colors = LocalHisobimColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = colors.divider, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HisobimTab.entries.forEach { tab ->
                val selected = currentTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onSelectTab(tab) }
                        .testTag("bottom_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) colors.primaryAccent else colors.textSecondary
                    )
                }
            }
        }
    }
}
