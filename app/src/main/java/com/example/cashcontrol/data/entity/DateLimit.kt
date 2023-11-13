package com.example.cashcontrol.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("date_limit_table")
data class DateLimit(
    var date: String = "",
    var dateFrameId: Int,
    var expenseLimit: Double,
    var limitExceededValue: Double
) {
    @PrimaryKey (autoGenerate = true)
    var dateLimitId: Int? = null
}
