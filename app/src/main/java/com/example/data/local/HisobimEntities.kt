package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DebtDirection {
    GIVEN, // Men qarz berganlar (Menga qaytariladi)
    TAKEN  // Men qarz olganlar (Men qaytaraman)
}

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val personName: String,
    val direction: String = DebtDirection.GIVEN.name,
    val totalAmount: Long,
    val paidAmount: Long = 0L,
    val dueDateLabel: String = "",
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val debtDirection: DebtDirection
        get() = if (direction == DebtDirection.TAKEN.name) DebtDirection.TAKEN else DebtDirection.GIVEN

    val remainingAmount: Long
        get() = (totalAmount - paidAmount).coerceAtLeast(0L)

    val repaymentProgress: Float
        get() = if (totalAmount > 0L) (paidAmount.toFloat() / totalAmount.toFloat()).coerceIn(0f, 1f) else 0f
}

enum class PlanPeriod(val label: String) {
    DAILY("Kunlik"),
    WEEKLY("Haftalik"),
    MONTHLY("Oylik")
}

@Entity(tableName = "plan_items")
data class PlanItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val quantityLabel: String = "1 dona",
    val estimatedPrice: Long = 0L,
    val period: String = PlanPeriod.WEEKLY.name,
    val isChecked: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val planPeriod: PlanPeriod
        get() = PlanPeriod.entries.firstOrNull { it.name == period } ?: PlanPeriod.WEEKLY
}

@Entity(tableName = "charities")
data class CharityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val category: String = "Sadaqa", // Sadaqa, Zakot, Vaqf
    val amount: Long,
    val dateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_categories")
data class CustomCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: String = TransactionType.EXPENSE.name
)
