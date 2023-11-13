package com.example.cashcontrol.data.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashcontrol.data.entity.DateFrame
import com.example.cashcontrol.data.entity.DateLimit

data class DateFrameWithDateLimits(
    @Embedded var dateFrame: DateFrame,
    @Relation (
        parentColumn = "dateFrameId",
        entityColumn = "dateFrameId"
    )
    var dateLimits: List<DateLimit>
)
