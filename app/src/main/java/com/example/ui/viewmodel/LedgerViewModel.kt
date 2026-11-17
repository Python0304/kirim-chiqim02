package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CharityEntity
import com.example.data.local.CustomCategoryEntity
import com.example.data.local.DebtDirection
import com.example.data.local.DebtEntity
import com.example.data.local.PlanItemEntity
import com.example.data.local.PlanPeriod
import com.example.data.local.PurchaseItem
import com.example.data.local.PurchaseItemCodec
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.local.UserPreferences
import com.example.data.repository.LedgerRepository
import com.example.ui.components.HisobimDefaults
import com.example.ui.components.currentYearMonth
import com.example.ui.components.formatShortDateTime
import com.example.ui.components.isSameYearMonth
import com.example.ui.components.monthOf
import com.example.ui.components.uzbekMonthsCap
import com.example.ui.components.yearOf
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryReportRow(
    val category: String,
    val totalAmount: Long,
    val percentage: Float,
    val transactions: List<TransactionEntity>
)

data class MonthlyBarPoint(
    val monthIndex: Int, // 0..11
    val income: Long,
    val expense: Long
)

data class HisobimUiState(
    val prefs: UserPreferences = UserPreferences(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val currentMonthIncome: Long = 0L,
    val currentMonthExpense: Long = 0L,
    val currentMonthBalance: Long = 0L,
    val currentMonthIncomesList: List<TransactionEntity> = emptyList(),
    val expenseCategories: List<String> = HisobimDefaults.defaultExpenseCategories,
    val incomeCategories: List<String> = HisobimDefaults.defaultIncomeCategories,
    val customCategories: List<CustomCategoryEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val totalReceivable: Long = 0L, // Menga qaytariladi
    val totalPayable: Long = 0L,    // Men qaytaraman
    val planItems: List<PlanItemEntity> = emptyList(),
    val charities: List<CharityEntity> = emptyList(),
    val currentMonthCharityTotal: Long = 0L,
    val currentYearCharityTotal: Long = 0L
) {
    fun monthlyBreakdown(year: Int, monthZeroBased: Int): List<CategoryReportRow> {
        val monthExpenses = allTransactions.filter {
            it.transactionType == TransactionType.EXPENSE && isSameYearMonth(it.dateMillis, year, monthZeroBased)
        }
        val total = monthExpenses.sumOf { it.amount }
        if (total <= 0L) return emptyList()
        return monthExpenses.groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                CategoryReportRow(
                    category = cat,
                    totalAmount = sum,
                    percentage = sum.toFloat() / total.toFloat(),
                    transactions = list
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    fun monthlyExpenseTotal(year: Int, monthZeroBased: Int): Long {
        return allTransactions
            .filter { it.transactionType == TransactionType.EXPENSE && isSameYearMonth(it.dateMillis, year, monthZeroBased) }
            .sumOf { it.amount }
    }

    fun yearlyBars(year: Int): List<MonthlyBarPoint> {
        val yearTx = allTransactions.filter { yearOf(it.dateMillis) == year }
        return (0..11).map { m ->
            val monthTx = yearTx.filter { monthOf(it.dateMillis) == m }
            MonthlyBarPoint(
                monthIndex = m,
                income = monthTx.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount },
                expense = monthTx.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }
    }

    fun yearlySummary(year: Int): YearlySummary {
        val bars = yearlyBars(year)
        val totalIncome = bars.sumOf { it.income }
        val totalExpense = bars.sumOf { it.expense }
        val totalCharity = charities.filter { yearOf(it.dateMillis) == year }.sumOf { it.amount }
        val maxExpenseBar = bars.maxByOrNull { it.expense }?.takeIf { it.expense > 0L }
        return YearlySummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            savings = totalIncome - totalExpense,
            charity = totalCharity,
            highestExpenseMonthName = maxExpenseBar?.let { uzbekMonthsCap[it.monthIndex] } ?: "—",
            highestExpenseAmount = maxExpenseBar?.expense ?: 0L
        )
    }
}

data class YearlySummary(
    val totalIncome: Long,
    val totalExpense: Long,
    val savings: Long,
    val charity: Long,
    val highestExpenseMonthName: String,
    val highestExpenseAmount: Long
)

class LedgerViewModel(
    private val repository: LedgerRepository
) : ViewModel() {

    private val isUnlockedState = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = isUnlockedState

    private data class CoreRecords(
        val transactions: List<TransactionEntity>,
        val debts: List<DebtEntity>,
        val plans: List<PlanItemEntity>,
        val charities: List<CharityEntity>
    )

    private val coreRecordsFlow = combine(
        repository.transactions,
        repository.debts,
        repository.planItems,
        repository.charities
    ) { txs, debts, plans, charities ->
        CoreRecords(txs, debts, plans, charities)
    }

    val uiState: StateFlow<HisobimUiState> = combine(
        coreRecordsFlow,
        repository.customCategories,
        repository.preferences
    ) { records, customCats, prefs ->
        val (curYear, curMonth) = currentYearMonth()
        val monthTxs = records.transactions.filter { isSameYearMonth(it.dateMillis, curYear, curMonth) }
        val monthIncomes = monthTxs.filter { it.transactionType == TransactionType.INCOME }
        val monthIncomeSum = monthIncomes.sumOf { it.amount }
        val monthExpenseSum = monthTxs
            .filter { it.transactionType == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val customExpense = customCats.filter { it.type == TransactionType.EXPENSE.name }.map { it.name }
        val customIncome = customCats.filter { it.type == TransactionType.INCOME.name }.map { it.name }

        val receivable = records.debts
            .filter { it.debtDirection == DebtDirection.GIVEN }
            .sumOf { it.remainingAmount }
        val payable = records.debts
            .filter { it.debtDirection == DebtDirection.TAKEN }
            .sumOf { it.remainingAmount }

        val monthCharity = records.charities
            .filter { isSameYearMonth(it.dateMillis, curYear, curMonth) }
            .sumOf { it.amount }
        val yearCharity = records.charities
            .filter { yearOf(it.dateMillis) == curYear }
            .sumOf { it.amount }

        HisobimUiState(
            prefs = prefs,
            allTransactions = records.transactions,
            currentMonthIncome = monthIncomeSum,
            currentMonthExpense = monthExpenseSum,
            currentMonthBalance = monthIncomeSum - monthExpenseSum,
            currentMonthIncomesList = monthIncomes,
            expenseCategories = (HisobimDefaults.defaultExpenseCategories + customExpense).distinct(),
            incomeCategories = (HisobimDefaults.defaultIncomeCategories + customIncome).distinct(),
            customCategories = customCats,
            debts = records.debts,
            totalReceivable = receivable,
            totalPayable = payable,
            planItems = records.plans,
            charities = records.charities,
            currentMonthCharityTotal = monthCharity,
            currentYearCharityTotal = yearCharity
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HisobimUiState()
    )

    fun unlock() {
        isUnlockedState.value = true
    }

    fun lock() {
        isUnlockedState.value = false
    }

    fun createOrUpdatePassword(newPassword: String) {
        viewModelScope.launch {
            repository.setPassword(newPassword.trim())
            isUnlockedState.value = true
        }
    }

    fun saveExpense(
        id: Long = 0L,
        title: String,
        category: String,
        items: List<PurchaseItem>,
        directAmount: Long,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        val total = if (items.isNotEmpty()) items.sumOf { it.price } else directAmount
        if (total <= 0L) return
        viewModelScope.launch {
            repository.saveTransaction(
                TransactionEntity(
                    id = id,
                    type = TransactionType.EXPENSE.name,
                    amount = total,
                    category = category.ifBlank { "Boshqa" },
                    title = title.trim().ifBlank { category },
                    itemsJson = PurchaseItemCodec.encode(items),
                    itemsCount = items.size,
                    dateMillis = dateMillis
                )
            )
        }
    }

    fun saveIncome(
        id: Long = 0L,
        source: String,
        amount: Long,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        if (amount <= 0L) return
        viewModelScope.launch {
            repository.saveTransaction(
                TransactionEntity(
                    id = id,
                    type = TransactionType.INCOME.name,
                    amount = amount,
                    category = source.ifBlank { "Oylik maosh" },
                    title = source.ifBlank { "Oylik maosh" },
                    itemsJson = "[]",
                    itemsCount = 0,
                    dateMillis = dateMillis
                )
            )
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch { repository.deleteTransaction(tx) }
    }

    fun saveDebt(
        id: Long = 0L,
        personName: String,
        direction: DebtDirection,
        totalAmount: Long,
        paidAmount: Long = 0L,
        dueDateLabel: String = ""
    ) {
        if (personName.isBlank() || totalAmount <= 0L) return
        viewModelScope.launch {
            repository.saveDebt(
                DebtEntity(
                    id = id,
                    personName = personName.trim(),
                    direction = direction.name,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount.coerceIn(0L, totalAmount),
                    dueDateLabel = dueDateLabel.trim()
                )
            )
        }
    }

    fun recordDebtPayment(debt: DebtEntity, paymentAmount: Long) {
        if (paymentAmount <= 0L) return
        viewModelScope.launch {
            val newPaid = (debt.paidAmount + paymentAmount).coerceAtMost(debt.totalAmount)
            repository.saveDebt(debt.copy(paidAmount = newPaid))
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch { repository.deleteDebt(debt) }
    }

    fun savePlanItem(
        id: Long = 0L,
        title: String,
        quantityLabel: String,
        estimatedPrice: Long,
        period: PlanPeriod
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.savePlanItem(
                PlanItemEntity(
                    id = id,
                    title = title.trim(),
                    quantityLabel = quantityLabel.trim().ifBlank { "1 dona" },
                    estimatedPrice = estimatedPrice.coerceAtLeast(0L),
                    period = period.name
                )
            )
        }
    }

    fun togglePlanChecked(item: PlanItemEntity) {
        viewModelScope.launch {
            repository.savePlanItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun deletePlanItem(item: PlanItemEntity) {
        viewModelScope.launch { repository.deletePlanItem(item) }
    }

    fun saveCharity(
        id: Long = 0L,
        title: String,
        category: String,
        amount: Long,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        if (title.isBlank() || amount <= 0L) return
        viewModelScope.launch {
            repository.saveCharity(
                CharityEntity(
                    id = id,
                    title = title.trim(),
                    category = category.ifBlank { "Sadaqa" },
                    amount = amount,
                    dateMillis = dateMillis
                )
            )
        }
    }

    fun deleteCharity(charity: CharityEntity) {
        viewModelScope.launch { repository.deleteCharity(charity) }
    }

    fun setMonthlyCharityGoal(goal: Long) {
        viewModelScope.launch { repository.setMonthlyCharityGoal(goal) }
    }

    fun addCustomCategory(name: String, type: TransactionType) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCustomCategory(CustomCategoryEntity(name = name.trim(), type = type.name))
        }
    }

    fun deleteCustomCategory(category: CustomCategoryEntity) {
        viewModelScope.launch { repository.deleteCustomCategory(category) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setCurrency(symbol: String, display: String) {
        viewModelScope.launch { repository.setCurrency(symbol, display) }
    }

    fun performLocalBackup() {
        viewModelScope.launch {
            val nowLabel = "Oxirgi: ${formatShortDateTime(System.currentTimeMillis())}"
            repository.setLastBackupLabel(nowLabel)
        }
    }

    class Factory(private val repository: LedgerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LedgerViewModel::class.java)) {
                return LedgerViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
