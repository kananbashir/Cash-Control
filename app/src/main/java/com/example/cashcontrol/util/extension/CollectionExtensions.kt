package com.example.cashcontrol.util.extension

import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun <E> List<E>.concatenateCategories (): String {
    var concatenatedText = ""
    for (i in this.indices) {
        if (this.size-1 != i) {
            concatenatedText += "- ${this[i]}\n"
        } else {
            concatenatedText += "- ${this[i]}"
        }
    }

    return concatenatedText
}

fun List<Transaction>.sortTransactionsByDateDescending (): List<Transaction> {
    val formatter = DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN)
    val sortedTransactions = this.sortedByDescending {
        LocalDate.parse(it.date, formatter)
    }
    return sortedTransactions
}

fun List<DateLimit>.sortDateLimitsByDateDescending (): List<DateLimit> {
    val formatter = DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN)
    val sortedTransactions = this.sortedByDescending {
        LocalDate.parse(it.date, formatter)
    }
    return sortedTransactions
}