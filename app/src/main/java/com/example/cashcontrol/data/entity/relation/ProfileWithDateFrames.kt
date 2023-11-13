package com.example.cashcontrol.data.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashcontrol.data.entity.DateFrame
import com.example.cashcontrol.data.entity.Profile

data class ProfileWithDateFrames(
    @Embedded var profile: Profile,
    @Relation (
        parentColumn = "profileId",
        entityColumn = "profileId"
    )
    var dateFrames: List<DateFrame>
)
