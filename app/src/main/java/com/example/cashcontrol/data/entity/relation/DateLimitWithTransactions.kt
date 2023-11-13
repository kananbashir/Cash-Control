package com.example.cashcontrol.data.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashcontrol.data.entity.DateLimit
import com.example.cashcontrol.data.entity.Transaction

data class DateLimitWithTransactions (
    @Embedded val dateLimit: DateLimit,
    @Relation (
        parentColumn = "dateLimitId",
        entityColumn = "dateLimitId"
    )
    val transactions: List<Transaction>
)