package com.example.cashcontrol.data.db.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Transaction

data class DateLimitWithTransactions (
    @Embedded val dateLimit: DateLimit,
    @Relation (
        parentColumn = "dateLimitId",
        entityColumn = "dateLimitId"
    )
    val transactions: List<Transaction>
)