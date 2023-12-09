package com.example.cashcontrol.data

import com.example.cashcontrol.util.constant.DateConstant
import com.example.cashcontrol.util.constant.DateLimitConstant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue

class DateLimitCalculator (
    private val totalBudget: Double,
    private val remainingBudget: Double,
    private val previousDayLimitExceedValue: Double,
    startPointDate: String,
    endPointDate: String
) {
    private val parsedStartPointDate: LocalDate = LocalDate.parse(startPointDate, DateTimeFormatter.ofPattern(DateConstant.DATE_LIMIT_DATE_PATTERN, Locale.US))
    private val parsedEndPointDate: LocalDate = LocalDate.parse(endPointDate, DateTimeFormatter.ofPattern(DateConstant.DATE_LIMIT_DATE_PATTERN, Locale.US))

    fun calculate (): Double {
        val periodUntilToday = ChronoUnit.DAYS.between(parsedStartPointDate, LocalDate.now()).absoluteValue.toInt()

        return when (periodUntilToday) {
            0 -> { calculateFirstDayLimit() }
            1 -> { calculateSecondDayLimit() }
            2 -> { calculateThirdDayLimit() }
            else -> { calculateDateLimit() }
        }
    }

    private fun calculateFirstDayLimit (): Double {
        val expenseLimit = ((totalBudget * DateLimitConstant.FIRST_DAY_ALLOWED_PERCENTAGE)/100) - previousDayLimitExceedValue
        return expenseLimit
    }

    private fun calculateSecondDayLimit (): Double {
        val expenseLimit = ((totalBudget * DateLimitConstant.SECOND_DAY_ALLOWED_PERCENTAGE)/100) - previousDayLimitExceedValue
        return expenseLimit
    }

    private fun calculateThirdDayLimit (): Double {
        val expenseLimit = ((totalBudget * DateLimitConstant.THIRD_DAY_ALLOWED_PERCENTAGE)/100) - previousDayLimitExceedValue
        return expenseLimit
    }

    private fun calculateDateLimit (): Double {
        val periodUntilEndPointDate = ChronoUnit.DAYS.between(LocalDate.now(), parsedEndPointDate.plusDays(1)).absoluteValue.toInt()
        val expenseLimit = (remainingBudget / periodUntilEndPointDate) - previousDayLimitExceedValue
        return expenseLimit
    }
}