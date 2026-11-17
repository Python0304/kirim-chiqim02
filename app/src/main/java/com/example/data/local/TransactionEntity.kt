package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class PurchaseItem(
    val name: String,
    val quantity: String = "1",
    val unit: String = "dona",
    val price: Long = 0L
) {
    val quantityDisplay: String
        get() = listOf(quantity.trim(), unit.trim()).filter { it.isNotEmpty() }.joinToString(" ")
}

@Entity(tableName = "Transaction")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Long,
    val category: String,
    val title: String,
    val note: String = "",
    val itemsJson: String = "[]",
    val itemsCount: Int = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val transactionType: TransactionType
        get() = if (type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE

    fun parsedItems(): List<PurchaseItem> = PurchaseItemCodec.decode(itemsJson)
}

object PurchaseItemCodec {
    fun encode(items: List<PurchaseItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("name", item.name)
                    put("quantity", item.quantity)
                    put("unit", item.unit)
                    put("price", item.price)
                }
            )
        }
        return arr.toString()
    }

    fun decode(json: String): List<PurchaseItem> {
        if (json.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                PurchaseItem(
                    name = obj.optString("name", ""),
                    quantity = obj.optString("quantity", "1"),
                    unit = obj.optString("unit", "dona"),
                    price = obj.optLong("price", 0L)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
