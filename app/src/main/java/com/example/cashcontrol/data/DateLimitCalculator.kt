package com.example.cashcontrol.data

import android.util.Log
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.ui.fragment.onboarding.OnBoardingFinishFragmentDirections
import com.example.cashcontrol.util.constant.DateConstant
import com.example.cashcontrol.util.constant.DateLimitConstant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

class DateLimitCalculator (
    private val totalBudget: Double,
    private val remainingBudget: Double,
    private val previousDayLimitExceedValue: Double,
    private val startPointDate: String,
    private val endPointDate: String
) {
    private val parsedStartPointDate: LocalDate = LocalDate.parse(startPointDate, DateTimeFormatter.ofPattern(DateConstant.DATE_LIMIT_DATE_PATTERN))
    private val parsedEndPointDate: LocalDate = LocalDate.parse(endPointDate, DateTimeFormatter.ofPattern(DateConstant.DATE_LIMIT_DATE_PATTERN))

    fun calculate (): Double {
        val periodUntilToday = ChronoUnit.DAYS.between(parsedStartPointDate, LocalDate.now()).absoluteValue.toInt()
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculate) -> periodUntilToday: $periodUntilToday")

        return when (periodUntilToday) {
            0 -> { calculateFirstDayLimit() }
            1 -> { calculateSecondDayLimit() }
            2 -> { calculateThirdDayLimit() }
            else -> { calculateDateLimit() }
        }
    }

    private fun calculateFirstDayLimit (): Double {
        val expenseLimit = ((totalBudget * DateLimitConstant.FIRST_DAY_ALLOWED_PERCENTAGE)/100) - previousDayLimitExceedValue
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateFirstDayLimit) -> totalBudget: $totalBudget")
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateFirstDayLimit) -> previousDayLimitExceedValue: $previousDayLimitExceedValue")
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateFirstDayLimit) -> EXPENSE LIMIT: $expenseLimit")
        return expenseLimit
    }

    private fun calculateSecondDayLimit (): Double {
        val expenseLimit = ((totalBudget * DateLimitConstant.SECOND_DAY_ALLOWED_PERCENTAGE)/100) - previousDayLimitExceedValue
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateSecondDayLimit) -> totalBudget: $totalBudget")
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateSecondDayLimit) -> previousDayLimitExceedValue: $previousDayLimitExceedValue")
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateSecondDayLimit) -> EXPENSE LIMIT: $expenseLimit")
        return expenseLimit
    }

    private fun calculateThirdDayLimit (): Double {
        val expenseLimit = ((totalBudget * DateLimitConstant.THIRD_DAY_ALLOWED_PERCENTAGE)/100) - previousDayLimitExceedValue
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateThirdDayLimit) -> totalBudget: $totalBudget")
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateThirdDayLimit) -> previousDayLimitExceedValue: $previousDayLimitExceedValue")
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateThirdDayLimit) -> EXPENSE LIMIT: $expenseLimit")
        return expenseLimit
    }

    private fun calculateDateLimit (): Double {
        val periodUntilEndPointDate = ChronoUnit.DAYS.between(LocalDate.now(), parsedEndPointDate.plusDays(1)).absoluteValue.toInt()
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateDateLimit) -> periodUntilEndPointDate: $periodUntilEndPointDate")
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateDateLimit) -> remainingBudget: $remainingBudget")
        val expenseLimit = (remainingBudget / periodUntilEndPointDate) - previousDayLimitExceedValue
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateDateLimit) -> totalBudget: $totalBudget")
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateDateLimit) -> previousDayLimitExceedValue: $previousDayLimitExceedValue")
        Log.i("UYUYUYCALCULATE","DateLimitCalculator(calculateDateLimit) -> EXPENSE LIMIT: $expenseLimit")
        return expenseLimit
    }
}