package com.example.cashcontrol.util.extension

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun String.getCurrencyCode(): String {
    val currencyCodeAndSymbol = this.split(" - ")
    if (currencyCodeAndSymbol.isNotEmpty()) {
        return currencyCodeAndSymbol[0]
    }
    return ""
}

fun String.getCurrencySymbol(): String {
    val currencyCodeAndSymbol = this.split(" - ")
    if (currencyCodeAndSymbol.isNotEmpty()) {
        return currencyCodeAndSymbol[1]
    }
    return ""
}
fun String.convertDateFromIso8601To(pattern: String): String {
    val parsedDate = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
    return parsedDate.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
}

fun String.convertDateToIso8601From(pattern: String): String {
    val parsedDate = LocalDate.parse(this, DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    return parsedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
}
