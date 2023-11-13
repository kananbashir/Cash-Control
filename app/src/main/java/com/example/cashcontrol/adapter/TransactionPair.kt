package com.example.cashcontrol.adapter

import com.example.cashcontrol.data.entity.DateLimit
import com.example.cashcontrol.data.entity.Transaction

data class TransactionPair(
    val dateLimit: DateLimit,
    val transactionList: List<Transaction>
)