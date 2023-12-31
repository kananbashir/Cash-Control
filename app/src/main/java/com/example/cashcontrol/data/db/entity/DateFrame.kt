package com.example.cashcontrol.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("dateFrame_table")
data class DateFrame(
    var startPointDate: String, // pattern dd MMMM yyyy
    var endPointDate: String, // pattern dd MMMM yyyy
    var initialBudget: Double,
    var mainCurrency: String,
    var savedMoney: Double,
    var isUnfinished: Boolean,
    var isOnline: Boolean,
    var profileId: Int

) {
    @PrimaryKey (autoGenerate = true)
    var dateFrameId: Int? = null
    var totalIncomeOfAll: Double = 0.0
    var totalExpenseOfAll: Double = 0.0

    fun getTotalBudget (): Double {
        return initialBudget + totalIncomeOfAll - savedMoney
    }

    fun getRemainingBudget (): Double {
        return getTotalBudget() - totalExpenseOfAll
    }
}
