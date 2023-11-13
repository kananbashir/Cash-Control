package com.example.cashcontrol.data.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.cashcontrol.data.entity.Profile
import com.example.cashcontrol.data.entity.User

data class UserWithProfiles(
    @Embedded var user: User,
    @Relation (
        parentColumn = "userId",
        entityColumn = "userId"
    )
    var profiles: List<Profile>
)
