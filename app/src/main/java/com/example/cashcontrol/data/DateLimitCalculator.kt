package com.example.cashcontrol.data

import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithTransactions
import com.example.cashcontrol.util.constant.DateLimitConstant
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import com.example.cashcontrol.util.extension.isInTernaryCycle
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

class DateLimitCalculator {
    private var totalBudget: Double? = null
    private var remainingBudget: Double? = null
    private var previousDayLimitExceedValue: Double? = null
    private var dateFrame: DateFrame? = null
    private var bonusExpenseValue: Double? = null
    private var totalExpenseOfAll: Double? = null
    private var parsedStartPointDate: LocalDate? = null
    private var parsedEndPointDate: LocalDate? = null
    private var issueDate: LocalDate? = null

    fun setPreviousDayLimitExceedValue(previousDayLimitExceedValue: Double): DateLimitCalculator {
        this.previousDayLimitExceedValue = previousDayLimitExceedValue
        return this
    }

    fun setIssueDate(date: LocalDate): DateLimitCalculator {
        issueDate = date
        return this
    }

    fun setDateFrame(dateFrame: DateFrame): DateLimitCalculator {
        this.dateFrame = dateFrame
        this.totalBudget = dateFrame.getTotalBudget()
        this.remainingBudget = dateFrame.getRemainingBudget()
        parsedStartPointDate =
            LocalDate.parse(dateFrame.startPointDate, DateTimeFormatter.ISO_LOCAL_DATE)
        parsedEndPointDate =
            LocalDate.parse(dateFrame.endPointDate, DateTimeFormatter.ISO_LOCAL_DATE)
        this.totalExpenseOfAll = dateFrame.totalExpenseOfAll
        return this
    }

    fun setBonusExpenseValue (bonusExpenseValue: Double): DateLimitCalculator {
        this.bonusExpenseValue = bonusExpenseValue
        return this
    }

    fun calculate(): Double {
        if (dateFrame == null) {
            throw NullPointerException("No date frame object provided. Please set a date frame object prior to calculation")
        } else {
            val periodFromStartPointDate = ChronoUnit.DAYS.between(issueDate, parsedStartPointDate).absoluteValue.toInt()
            return when (periodFromStartPointDate) {
                0 -> {
                    calculateFirstDayLimit()
                }

                1 -> {
                    calculateSecondDayLimit()
                }

                2 -> {
                    calculateThirdDayLimit()
                }

                else -> {
                    calculateStandardDateLimit()
                }
            }
        }
    }

    private fun calculateFirstDayLimit(): Double {
        val expenseLimit = remainingBudget!!.toBigDecimal()
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
            .multiply(DateLimitConstant.FIRST_DAY_ALLOWED_PERCENTAGE.toBigDecimal())
            .minus(previousDayLimitExceedValue!!.toBigDecimal())
            .toDouble()
        return if (expenseLimit > 0) {
            expenseLimit
        } else {
            0.0
        }
    }

    private fun calculateSecondDayLimit(): Double {
        val expenseLimit = remainingBudget!!.toBigDecimal()
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
            .multiply(DateLimitConstant.SECOND_DAY_ALLOWED_PERCENTAGE.toBigDecimal())
            .minus(previousDayLimitExceedValue!!.toBigDecimal())
            .toDouble()
        return if (expenseLimit > 0) {
            expenseLimit
        } else {
            0.0
        }
    }

    private fun calculateThirdDayLimit(): Double {
        val expenseLimit = remainingBudget!!.toBigDecimal()
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
            .multiply(DateLimitConstant.THIRD_DAY_ALLOWED_PERCENTAGE.toBigDecimal())
            .minus(previousDayLimitExceedValue!!.toBigDecimal())
            .toDouble()
        return if (expenseLimit > 0) {
            expenseLimit
        } else {
            0.0
        }
    }

    private fun calculateStandardDateLimit(): Double {
        if (issueDate == null) {
            throw NullPointerException("Please provide an issue date to calculate a standard daily limit")
        } else if (bonusExpenseValue == null) {
            throw NullPointerException("Please provide a bonus expense value to calculate a standard daily limit")
        } else {
            val periodUntilEndPointDate = ChronoUnit.DAYS.between(issueDate, parsedEndPointDate).plus(1).absoluteValue.toInt()
            val expenseLimit = remainingBudget!!.toBigDecimal().divide(periodUntilEndPointDate.toBigDecimal(), 2, RoundingMode.HALF_EVEN)
                .minus(previousDayLimitExceedValue!!.toBigDecimal()).plus(bonusExpenseValue!!.toBigDecimal()).toDouble()
            return if (expenseLimit > 0) {
                expenseLimit
            } else {
                0.0
            }
        }
    }

    fun createSubsequentDateLimits(
        allDateLimits: List<DateLimit>,
        allTransactions: List<Transaction>,
    ): List<DateLimit> {
        if (dateFrame != null) {
            val lastDateLimit = allDateLimits.last()
            val parsedLastDate = LocalDate.parse(
                lastDateLimit.date,
                DateTimeFormatter.ISO_LOCAL_DATE
            )

            val daysPassedFromLastDate = ChronoUnit.DAYS.between(
                parsedLastDate,
                LocalDate.now()
            ).absoluteValue

            val dateLimitList: MutableList<DateLimit> = mutableListOf()

            for (i in 1 until daysPassedFromLastDate+1) {
                val updatedDate = parsedLastDate.plusDays(i)

                val previousDayLimitExceedValue =
                    if (i.toInt() == 1) Util.getPreviousDayLimitExceededValueFor(lastDateLimit, allDateLimits) else 0.0

                val isInTernaryCycle = updatedDate.isInTernaryCycle(dateFrame!!.startPointDate)
                val bonusExpenseValue =
                    if (isInTernaryCycle) Util.calculateBonusExpenseValueOf(updatedDate, allDateLimits, allTransactions) else 0.0

                val expenseLimit = setPreviousDayLimitExceedValue(previousDayLimitExceedValue)
                    .setIssueDate(updatedDate)
                    .setBonusExpenseValue(bonusExpenseValue)
                    .calculate()

                val dateLimit = DateLimit(
                    updatedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    dateFrame!!.dateFrameId!!,
                    expenseLimit,
                    limitExceededValue = 0.0,
                    isInTernaryCycle = isInTernaryCycle,
                    bonusExpenseValue = bonusExpenseValue
                )


                dateLimitList.add(dateLimit)
            }

            return dateLimitList
        } else {
            throw NullPointerException("No date frame object provided. Please set a date frame object to create the date limits")
        }
    }

    fun createAllDateLimits(): List<DateLimit> {
        if (dateFrame != null) {
            val parsedStartPointDate = LocalDate.parse(
                dateFrame!!.startPointDate,
                DateTimeFormatter.ISO_LOCAL_DATE
            )
            val daysPassedFromStartPoint = ChronoUnit.DAYS.between(
                parsedStartPointDate,
                LocalDate.now().plusDays(1)
            ).absoluteValue

            val dateLimitList: MutableList<DateLimit> = mutableListOf()

            for (i in 0 until daysPassedFromStartPoint) {
                val updatedDate = parsedStartPointDate.plusDays(i)
                val isInTernaryCycle = updatedDate.isInTernaryCycle(dateFrame!!.startPointDate)
                val bonusExpenseValue = if (isInTernaryCycle) {
                    Util.calculateBonusExpenseValueOf(updatedDate, dateLimitList, listOf())
                } else {
                    0.0
                }

                val expenseLimit = setPreviousDayLimitExceedValue(0.0)
                    .setIssueDate(updatedDate)
                    .setBonusExpenseValue(bonusExpenseValue)
                    .calculate()

                val dateLimit = DateLimit(
                    updatedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    dateFrame!!.dateFrameId!!,
                    expenseLimit,
                    limitExceededValue = 0.0,
                    isInTernaryCycle = isInTernaryCycle,
                    bonusExpenseValue = bonusExpenseValue
                )

                dateLimitList.add(dateLimit)
            }

            return dateLimitList

        } else {
            throw NullPointerException("No date frame object provided. Please set a date frame object to create the date limits")
        }
    }

    fun reCalculateAllExpenseLimits(
        allDateLimits: List<DateLimit>,
        dateFrameWithTransactions: DateFrameWithTransactions
    ): List<DateLimit> {
            if (dateFrame != null) {
                val updatedDateLimits = mutableListOf<DateLimit>()
                val allExpenses = Util.extractExpensesFromTransactionList(dateFrameWithTransactions.transactions)
                for (dateLimit in allDateLimits) {
                    val previousDayLimitExceedValue = Util.getPreviousDayLimitExceededValueFor(dateLimit, allDateLimits)
                    val bonusExpenseValue = if (dateLimit.isInTernaryCycle) {
                        Util.calculateBonusExpenseValueOf(
                            LocalDate.parse(dateLimit.date, DateTimeFormatter.ISO_LOCAL_DATE),
                            allDateLimits,
                            allExpenses
                        )
                    } else {
                        0.0
                    }

                    val expenseLimit = setPreviousDayLimitExceedValue(previousDayLimitExceedValue)
                        .setIssueDate(LocalDate.parse(dateLimit.date, DateTimeFormatter.ISO_LOCAL_DATE))
                        .setBonusExpenseValue(bonusExpenseValue)
                        .calculate()

                    dateLimit.expenseLimit = expenseLimit
                    dateLimit.bonusExpenseValue = bonusExpenseValue
                    dateLimit.limitExceededValue = Util.calculateLimitExceededValueOf(dateLimit, allExpenses)
                    updatedDateLimits.add(dateLimit)
                }

                return updatedDateLimits
            } else {
                throw NullPointerException("No date frame object provided. Please set a date frame object to create the date limits")
            }
    }

    fun reCalculateSubsequentExpenseLimits(
        selectedDateLimit: DateLimit,
        dateInclusive: Boolean,
        allDateLimits: List<DateLimit>,
        dateFrameWithTransactions: DateFrameWithTransactions,
    ): List<DateLimit> {
        if (dateFrame != null) {
            val issueDate = if (dateInclusive) {
                LocalDate.parse(selectedDateLimit.date, DateTimeFormatter.ISO_LOCAL_DATE)
            } else {
                LocalDate.parse(selectedDateLimit.date, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1)
            }

            val updatedDateLimits = mutableListOf<DateLimit>()
            val allExpenses = Util.extractExpensesFromTransactionList(dateFrameWithTransactions.transactions)

            for (dateLimit in allDateLimits) {
                if (LocalDate.parse(dateLimit.date, DateTimeFormatter.ISO_LOCAL_DATE) >= issueDate) {
                    val previousDayLimitExceedValue = Util.getPreviousDayLimitExceededValueFor(dateLimit, allDateLimits)
                    val bonusExpenseValue = if (dateLimit.isInTernaryCycle) {
                        Util.calculateBonusExpenseValueOf(
                            LocalDate.parse(dateLimit.date, DateTimeFormatter.ISO_LOCAL_DATE),
                            allDateLimits,
                            allExpenses
                        )
                    } else {
                        0.0
                    }

                    val expenseLimit = setPreviousDayLimitExceedValue(previousDayLimitExceedValue)
                        .setIssueDate(LocalDate.parse(dateLimit.date, DateTimeFormatter.ISO_LOCAL_DATE))
                        .setBonusExpenseValue(bonusExpenseValue)
                        .calculate()

                    dateLimit.expenseLimit = expenseLimit
                    dateLimit.bonusExpenseValue = bonusExpenseValue
                    dateLimit.limitExceededValue = Util.calculateLimitExceededValueOf(dateLimit, allExpenses)
                    updatedDateLimits.add(dateLimit)
                }
            }

            return updatedDateLimits
        } else {
            throw NullPointerException("No date frame object is provided. Please set a date frame object prior to recalculation")
        }
    }

    object Util {
        fun extractExpensesFromTransactionList(allTransactions: List<Transaction>): List<Transaction> {
           return allTransactions.filter { transaction -> transaction.transactionType == TRANSACTION_TYPE_EXPENSE }
        }

        fun getPreviousDayLimitExceededValueFor(dateLimit: DateLimit, allDateLimits: List<DateLimit>): Double {
            if (allDateLimits.isNotEmpty()) {
                val previousDay = LocalDate.parse(dateLimit.date, DateTimeFormatter.ISO_LOCAL_DATE).minusDays(1)
                val previousDayDateLimit = allDateLimits.find { dl -> dl.date == previousDay.format(DateTimeFormatter.ISO_LOCAL_DATE) }

                previousDayDateLimit?.let {
                    return it.limitExceededValue
                }
            }

            return 0.0
        }

        fun getSumOfExpensesOf(dateLimit: DateLimit, allExpenses: List<Transaction>): Double {
            var sumOfExpenses = BigDecimal.valueOf(0.0)

            for (expense in allExpenses) {
                if (expense.date == dateLimit.date) {
                    sumOfExpenses = sumOfExpenses.plus(expense.transactionAmount.toBigDecimal())
                }
            }

            return sumOfExpenses.toDouble()
        }

        fun calculateBonusExpenseValueOf (date: LocalDate, allDateLimits: List<DateLimit>, allExpenses: List<Transaction>): Double {
            val firstDateLimit = allDateLimits.find { dl ->
                LocalDate.parse(dl.date, DateTimeFormatter.ISO_LOCAL_DATE) == date.minusDays(3)
            }
            val firstDateLimitSumOfExpenses = getSumOfExpensesOf(firstDateLimit!!, allExpenses)
            val firstDateLimitAllowableExpense = firstDateLimit.expenseLimit.toBigDecimal()
                .multiply(BigDecimal.valueOf(70))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
            val firstDateLimitExpensesIsValid: Boolean = firstDateLimitSumOfExpenses < firstDateLimitAllowableExpense.toDouble()

            val secondDateLimit = allDateLimits.find { dl ->
                LocalDate.parse(dl.date, DateTimeFormatter.ISO_LOCAL_DATE) == date.minusDays(2)
            }
            val secondDateLimitSumOfExpenses = getSumOfExpensesOf(secondDateLimit!!, allExpenses)
            val secondDateLimitAllowableExpense = secondDateLimit.expenseLimit.toBigDecimal()
                .multiply(BigDecimal.valueOf(70))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
            val secondDateLimitExpensesIsValid: Boolean = secondDateLimitSumOfExpenses < secondDateLimitAllowableExpense.toDouble()

            val thirdDateLimit = allDateLimits.find { dl ->
                LocalDate.parse(dl.date, DateTimeFormatter.ISO_LOCAL_DATE) == date.minusDays(1)
            }
            val thirdDateLimitSumOfExpenses = getSumOfExpensesOf(thirdDateLimit!!, allExpenses)
            val thirdDateLimitAllowableExpense = thirdDateLimit.expenseLimit.toBigDecimal()
                .multiply(BigDecimal.valueOf(70))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
            val thirdDateLimitExpensesIsValid: Boolean = thirdDateLimitSumOfExpenses < thirdDateLimitAllowableExpense.toDouble()

            if (firstDateLimitExpensesIsValid && secondDateLimitExpensesIsValid && thirdDateLimitExpensesIsValid) {
                val firstDateLimitExpenseDifference = firstDateLimit.expenseLimit.toBigDecimal()
                    .minus(firstDateLimitSumOfExpenses.toBigDecimal())
                val secondDateLimitExpenseDifference = secondDateLimit.expenseLimit.toBigDecimal()
                    .minus(secondDateLimitSumOfExpenses.toBigDecimal())
                val thirdDateLimitExpenseDifference = thirdDateLimit.expenseLimit.toBigDecimal()
                    .minus(thirdDateLimitSumOfExpenses.toBigDecimal())

                val sumOfDifferences = firstDateLimitExpenseDifference.plus(secondDateLimitExpenseDifference).plus(thirdDateLimitExpenseDifference)

                val halfOfSumOfDifferences = sumOfDifferences.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_EVEN)

                val bonusExpenseValue = halfOfSumOfDifferences.multiply(BigDecimal.valueOf(30))
                    .divide(BigDecimal(100), 2, RoundingMode.HALF_EVEN).toDouble()

                return bonusExpenseValue
            } else {
                return 0.0
            }
        }

        fun calculateLimitExceededValueOf(dateLimit: DateLimit, sumOfExpenses: Double): Double {
            if (sumOfExpenses > dateLimit.expenseLimit) {
                val expenseRateOfLimit = sumOfExpenses.toBigDecimal()
                    .minus(dateLimit.expenseLimit.toBigDecimal())
                    .divide(sumOfExpenses.toBigDecimal(), 2, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100))

                val limitExceededValue = dateLimit.expenseLimit.toBigDecimal()
                    .multiply(expenseRateOfLimit)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
                    .toDouble()

                return limitExceededValue
            }

            return 0.0
        }

        fun calculateLimitExceededValueOf(dateLimit: DateLimit, allExpenses: List<Transaction>): Double {
            val sumOfExpenses = getSumOfExpensesOf(dateLimit, allExpenses)

            if (sumOfExpenses > dateLimit.expenseLimit) {
                val expenseRateOfLimit = sumOfExpenses.toBigDecimal()
                    .minus(dateLimit.expenseLimit.toBigDecimal())
                    .divide(sumOfExpenses.toBigDecimal(), 2, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100))

                val limitExceededValue = dateLimit.expenseLimit.toBigDecimal()
                    .multiply(expenseRateOfLimit)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN)
                    .toDouble()

                return limitExceededValue
            }

            return 0.0
        }
    }
}