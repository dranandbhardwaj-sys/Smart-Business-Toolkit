package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

private val indianNumberFormat: NumberFormat by lazy {
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
}

private val indianDecimalFormat: NumberFormat by lazy {
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
}

fun formatInr(amount: Double, showDecimals: Boolean = false): String {
    return if (showDecimals && amount % 1.0 != 0.0) {
        indianDecimalFormat.format(amount)
    } else {
        indianNumberFormat.format(amount.roundToLong())
    }
}

fun formatCompactInr(amount: Double): String {
    return when {
        amount >= 10_000_000 -> String.format(Locale("en", "IN"), "₹%.2fCr", amount / 10_000_000)
        amount >= 100_000 -> String.format(Locale("en", "IN"), "₹%.2fL", amount / 100_000)
        amount >= 1_000 -> String.format(Locale("en", "IN"), "₹%.1fK", amount / 1_000)
        else -> formatInr(amount)
    }
}

fun formatDate(millis: Long, pattern: String = "dd MMM yyyy"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}

fun formatDateTime(millis: Long): String {
    return SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(millis))
}

fun tr(en: String, hi: String, lang: String): String {
    return if (lang == "hi") hi else en
}
