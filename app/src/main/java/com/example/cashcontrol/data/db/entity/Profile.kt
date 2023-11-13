package com.example.cashcontrol.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("profile_table")
data class Profile(
    var profileName: String,
    var isOnline: Boolean,
    var userId: Int
) {
    @PrimaryKey (autoGenerate = true)
    var profileId: Int? = null
}
