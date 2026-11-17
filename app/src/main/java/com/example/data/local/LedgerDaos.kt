package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM `Transaction` ORDER BY dateMillis DESC, id DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM `Transaction` WHERE type = :type ORDER BY dateMillis DESC, id DESC")
    fun observeTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM `Transaction` WHERE type = :type")
    fun observeTotalByType(type: String): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM `Transaction` WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY createdAtMillis DESC, id DESC")
    fun observeAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)
}

@Dao
interface PlanItemDao {
    @Query("SELECT * FROM plan_items ORDER BY isChecked ASC, createdAtMillis DESC")
    fun observeAllPlanItems(): Flow<List<PlanItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanItem(item: PlanItemEntity): Long

    @Update
    suspend fun updatePlanItem(item: PlanItemEntity)

    @Delete
    suspend fun deletePlanItem(item: PlanItemEntity)
}

@Dao
interface CharityDao {
    @Query("SELECT * FROM charities ORDER BY dateMillis DESC, id DESC")
    fun observeAllCharities(): Flow<List<CharityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharity(charity: CharityEntity): Long

    @Update
    suspend fun updateCharity(charity: CharityEntity)

    @Delete
    suspend fun deleteCharity(charity: CharityEntity)
}

@Dao
interface CustomCategoryDao {
    @Query("SELECT * FROM custom_categories ORDER BY id ASC")
    fun observeAllCustomCategories(): Flow<List<CustomCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategoryEntity): Long

    @Delete
    suspend fun deleteCategory(category: CustomCategoryEntity)
}
