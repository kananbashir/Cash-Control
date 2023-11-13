package com.example.cashcontrol.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("transaction_table")
open class Transaction {
    @PrimaryKey (autoGenerate = true)
    open var transactionId: Int? = null
    open var transactionAmount: Double = 0.0
    open var transactionCurrency: String = ""
    open var transactionCategory: String = ""
    open var transactionCategories: MutableList<String> = mutableListOf()
    open var transactionSource: String = ""
    open var transactionSources: MutableList<String> = mutableListOf()
    open var transactionDescription: String = ""
    open var date: String = ""
    open var dateLimitId: Int = 0
    open var dateFrameId: Int = 0
    open var transactionType: String = ""
    open var isSelected: Boolean = false
}