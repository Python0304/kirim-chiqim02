package com.example.ui.components

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

object HisobimDefaults {
    val defaultExpenseCategories = listOf(
        "Oziq-ovqat",
        "Kiyim-kechak",
        "Taksi",
        "Kommunal",
        "Boshqa"
    )

    val defaultIncomeCategories = listOf(
        "Oylik maosh",
        "Qo'shimcha daromad",
        "Boshqa"
    )

    val charityCategories = listOf(
        "Hammasi",
        "Sadaqa",
        "Zakot",
        "Vaqf"
    )

    val editableCharityCategories = listOf("Sadaqa", "Zakot", "Vaqf")

    val units = listOf("dona", "kg", "l")

    val currencyOptions = listOf(
        "so'm" to "So'm (UZS)",
        "$" to "Dollar (USD)",
        "₽" to "Rubl (RUB)",
        "€" to "Yevro (EUR)"
    )
}

val uzbekMonthsCap = arrayOf(
    "Yanvar", "Fevral", "Mart", "Aprel", "May", "Iyun",
    "Iyul", "Avgust", "Sentabr", "Oktabr", "Noyabr", "Dekabr"
)

val uzbekMonthsLower = arrayOf(
    "yanvar", "fevral", "mart", "aprel", "may", "iyun",
    "iyul", "avgust", "sentabr", "oktabr", "noyabr", "dekabr"
)

val uzbekMonthInitials = arrayOf("Y", "F", "M", "A", "M", "I", "I", "A", "S", "O", "N", "D")

val uzbekWeekdaysCap = arrayOf(
    "Yakshanba", "Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba"
)

val uzbekWeekdaysLower = arrayOf(
    "yakshanba", "dushanba", "seshanba", "chorshanba", "payshanba", "juma", "shanba"
)

val uzbekWeekdayShorts = arrayOf("Du", "Se", "Ch", "Pa", "Ju", "Sh", "Ya")

fun formatAmountPlain(amount: Long): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ' ' }
    return DecimalFormat("#,###", symbols).format(abs(amount))
}

fun formatMoneyWithCurrency(amount: Long, currency: String): String {
    val sign = if (amount < 0) "-" else ""
    val num = formatAmountPlain(amount)
    return if (currency == "$" || currency == "€") "$sign$currency$num" else "$sign$num $currency"
}

fun formatSignedAmount(amount: Long, isIncome: Boolean): String {
    val prefix = if (isIncome) "+" else "-"
    return "$prefix${formatAmountPlain(amount)}"
}

fun formatFullGreetingDate(millis: Long = System.currentTimeMillis()): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val weekday = uzbekWeekdaysCap[cal.get(Calendar.DAY_OF_WEEK) - 1]
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = uzbekMonthsLower[cal.get(Calendar.MONTH)]
    val year = cal.get(Calendar.YEAR)
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val min = cal.get(Calendar.MINUTE)
    return String.format(Locale.US, "%s, %d-%s %d · %02d:%02d", weekday, day, month, year, hour, min)
}

fun formatShortDateTime(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = uzbekMonthsLower[cal.get(Calendar.MONTH)]
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val min = cal.get(Calendar.MINUTE)
    return String.format(Locale.US, "%d-%s %02d:%02d", day, month, hour, min)
}

fun formatTimeOnly(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return String.format(Locale.US, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
}

fun formatTransactionDateLabel(millis: Long): String {
    val now = Calendar.getInstance()
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val sameYear = now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
    val sameDay = sameYear && now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    return if (sameDay) {
        "Bugun"
    } else {
        "${cal.get(Calendar.DAY_OF_MONTH)}-${uzbekMonthsLower[cal.get(Calendar.MONTH)]}"
    }
}

fun formatCharityBadge(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val monthLetter = uzbekMonthsLower[cal.get(Calendar.MONTH)].first()
    return "$day-$monthLetter"
}

fun formatCharityDateTime(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val month = uzbekMonthsLower[cal.get(Calendar.MONTH)]
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val min = cal.get(Calendar.MINUTE)
    return String.format(Locale.US, "%d-%s, %02d:%02d", day, month, hour, min)
}

fun formatSelectedDayHeader(year: Int, monthZeroBased: Int, day: Int): String {
    val cal = Calendar.getInstance().apply {
        set(year, monthZeroBased, day)
    }
    val weekday = uzbekWeekdaysLower[cal.get(Calendar.DAY_OF_WEEK) - 1]
    return "$day-${uzbekMonthsLower[monthZeroBased]}, $weekday"
}

fun currentYearMonth(): Pair<Int, Int> {
    val cal = Calendar.getInstance()
    return cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
}

fun isSameYearMonth(millis: Long, year: Int, monthZeroBased: Int): Boolean {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == monthZeroBased
}

fun isSameDay(millis: Long, year: Int, monthZeroBased: Int, day: Int): Boolean {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return cal.get(Calendar.YEAR) == year &&
        cal.get(Calendar.MONTH) == monthZeroBased &&
        cal.get(Calendar.DAY_OF_MONTH) == day
}

fun yearOf(millis: Long): Int = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.YEAR)
fun monthOf(millis: Long): Int = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.MONTH)
fun dayOf(millis: Long): Int = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.DAY_OF_MONTH)
