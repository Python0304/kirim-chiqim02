package com.example.data.repository

import com.example.data.local.CharityDao
import com.example.data.local.CharityEntity
import com.example.data.local.CustomCategoryDao
import com.example.data.local.CustomCategoryEntity
import com.example.data.local.DebtDao
import com.example.data.local.DebtEntity
import com.example.data.local.PlanItemDao
import com.example.data.local.PlanItemEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow

class LedgerRepository(
    private val transactionDao: TransactionDao,
    private val debtDao: DebtDao,
    private val planItemDao: PlanItemDao,
    private val charityDao: CharityDao,
    private val customCategoryDao: CustomCategoryDao,
    private val preferencesRepository: UserPreferencesRepository
) {
    val transactions: Flow<List<TransactionEntity>> = transactionDao.observeAllTransactions()
    val debts: Flow<List<DebtEntity>> = debtDao.observeAllDebts()
    val planItems: Flow<List<PlanItemEntity>> = planItemDao.observeAllPlanItems()
    val charities: Flow<List<CharityEntity>> = charityDao.observeAllCharities()
    val customCategories: Flow<List<CustomCategoryEntity>> = customCategoryDao.observeAllCustomCategories()
    val preferences: Flow<UserPreferences> = preferencesRepository.preferencesFlow

    suspend fun saveTransaction(tx: TransactionEntity) {
        if (tx.id == 0L) transactionDao.insertTransaction(tx) else transactionDao.updateTransaction(tx)
    }

    suspend fun deleteTransaction(tx: TransactionEntity) = transactionDao.deleteTransaction(tx)

    suspend fun saveDebt(debt: DebtEntity) {
        if (debt.id == 0L) debtDao.insertDebt(debt) else debtDao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) = debtDao.deleteDebt(debt)

    suspend fun savePlanItem(item: PlanItemEntity) {
        if (item.id == 0L) planItemDao.insertPlanItem(item) else planItemDao.updatePlanItem(item)
    }

    suspend fun deletePlanItem(item: PlanItemEntity) = planItemDao.deletePlanItem(item)

    suspend fun saveCharity(charity: CharityEntity) {
        if (charity.id == 0L) charityDao.insertCharity(charity) else charityDao.updateCharity(charity)
    }

    suspend fun deleteCharity(charity: CharityEntity) = charityDao.deleteCharity(charity)

    suspend fun addCustomCategory(category: CustomCategoryEntity) = customCategoryDao.insertCategory(category)

    suspend fun deleteCustomCategory(category: CustomCategoryEntity) = customCategoryDao.deleteCategory(category)

    suspend fun setThemeMode(mode: AppThemeMode) = preferencesRepository.setThemeMode(mode)

    suspend fun setPassword(password: String) = preferencesRepository.setPassword(password)

    suspend fun setCurrency(symbol: String, display: String) = preferencesRepository.setCurrency(symbol, display)

    suspend fun setMonthlyCharityGoal(goal: Long) = preferencesRepository.setMonthlyCharityGoal(goal)

    suspend fun setLastBackupLabel(label: String) = preferencesRepository.setLastBackupLabel(label)
}
