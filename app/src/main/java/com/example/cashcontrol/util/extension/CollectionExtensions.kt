package com.example.cashcontrol.util.extension

import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun <E> List<E>.concatenateCategories (): String {
    var concatenatedText = ""
    for (i in this.indices) {
        concatenatedText += if (this.size-1 != i) {
            "- ${this[i]}\n"
        } else {
            "- ${this[i]}"
        }
    }

    return concatenatedText
}

fun List<Transaction>.sortTransactionsByDateDescending (): List<Transaction> {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val sortedTransactions = this.sortedByDescending {
        LocalDate.parse(it.date, formatter)
    }
    return sortedTransactions
}

fun List<DateLimit>.sortDateLimitsByDateDescending (): List<DateLimit> {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val sortedTransactions = this.sortedByDescending {
        LocalDate.parse(it.date, formatter)
    }
    return sortedTransactions
}

fun List<DateFrame>.sortDateFramesByDateDescending (): List<DateFrame> {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val sortedTransactions = this.sortedByDescending {
        LocalDate.parse(it.startPointDate, formatter)
    }
    return sortedTransactions
}