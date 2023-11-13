package com.example.cashcontrol.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("user_table")
data class User(
    var username: String,
    var password: String,
    var isOnline: Boolean,
    var cachedExpenseCategories: MutableSet<String>,
    var cachedIncomeCategories: MutableSet<String>
) {
    @PrimaryKey (autoGenerate = true)
    var userId: Int? = null
}
