package com.example.cashcontrol.data.db.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashcontrol.data.db.entity.Profile
import com.example.cashcontrol.data.db.entity.User

data class UserWithProfiles(
    @Embedded var user: User,
    @Relation (
        parentColumn = "userId",
        entityColumn = "userId"
    )
    var profiles: List<Profile>
)
