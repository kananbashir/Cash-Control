package com.example.cashcontrol.data

import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Transaction

data class TransactionPair(
    val dateLimit: DateLimit,
    val transactionList: List<Transaction>
)