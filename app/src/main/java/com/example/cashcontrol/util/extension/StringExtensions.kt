package com.example.cashcontrol.util.extension

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