package com.example.cashcontrol.data.db.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.Profile

data class ProfileWithDateFrames(
    @Embedded var profile: Profile,
    @Relation (
        parentColumn = "profileId",
        entityColumn = "profileId"
    )
    var dateFrames: List<DateFrame>
)
