package com.example.cashcontrol.adapter

import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Transaction

data class TransactionPair(
    val dateLimit: DateLimit,
    val transactionList: List<Transaction>
)