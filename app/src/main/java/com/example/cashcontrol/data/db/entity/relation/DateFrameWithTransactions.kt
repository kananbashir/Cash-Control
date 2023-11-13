package com.example.cashcontrol.data.db.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.Transaction

data class DateFrameWithTransactions (
    @Embedded val dateFrame: DateFrame,
    @Relation (
        parentColumn = "dateFrameId",
        entityColumn = "dateFrameId"
    )
    val transactions: List<Transaction>
)