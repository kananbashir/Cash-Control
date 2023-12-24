package com.example.cashcontrol.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("date_limit_table")
data class DateLimit(
    var date: String = "",
    var dateFrameId: Int,
    var expenseLimit: Double,
    var limitExceededValue: Double,
    var isInTernaryCycle: Boolean,
    var bonusExpenseValue: Double
) {
    @PrimaryKey (autoGenerate = true)
    var dateLimitId: Int? = null
}
