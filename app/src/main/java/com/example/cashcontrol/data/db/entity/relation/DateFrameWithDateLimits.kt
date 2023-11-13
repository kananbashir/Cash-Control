package com.example.cashcontrol.data.db.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.DateLimit

data class DateFrameWithDateLimits(
    @Embedded var dateFrame: DateFrame,
    @Relation (
        parentColumn = "dateFrameId",
        entityColumn = "dateFrameId"
    )
    var dateLimits: List<DateLimit>
)
