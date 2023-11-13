package com.example.cashcontrol.data.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashcontrol.data.entity.DateFrame
import com.example.cashcontrol.data.entity.Transaction

data class DateFrameWithTransactions (
    @Embedded val dateFrame: DateFrame,
    @Relation (
        parentColumn = "dateFrameId",
        entityColumn = "dateFrameId"
    )
    val transactions: List<Transaction>
)