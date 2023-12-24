package com.example.cashcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.relation.DateLimitWithTransactions
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DateLimitViewModel @Inject constructor(private val cashControlRepository: CashControlRepository): ViewModel() {

    fun upsertDateLimit (dateLimit: DateLimit) = viewModelScope.launch {
        cashControlRepository.dateLimitLocal.upsertDateLimit(dateLimit)
    }

    fun upsertAllDateLimits (vararg dateLimit: DateLimit) = viewModelScope.launch {
        cashControlRepository.dateLimitLocal.upsertAllDateLimit(*dateLimit)
    }

    fun deleteAllDateLimits (vararg dateLimit: DateLimit) = viewModelScope.launch {
        cashControlRepository.dateLimitLocal.deleteAllDateLimits(*dateLimit)
    }

    suspend fun getDateLimitWithTransactions (dateLimitId: Int): DateLimitWithTransactions? {
        val dateLimitWithTransactions = cashControlRepository.dateLimitLocal.getDateLimitWithTransactions(dateLimitId)
        if (dateLimitWithTransactions.isNotEmpty()) {
            return dateLimitWithTransactions.first()
        }
        return null
    }

    suspend fun getCurrentDateLimitByDateFrame (dateFrameId: Int): DateLimit? {
        val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dateLimitList = cashControlRepository.dateLimitLocal.getCurrentDateLimitByDateFrame(dateFrameId, currentDate)
        if (dateLimitList.isNotEmpty()) {
            return dateLimitList.first()
        }
        return null
    }

    suspend fun getDateLimitOfDateFrameByDate(dateFrameId: Int, date: String): DateLimit? {
        val list = cashControlRepository.dateLimitLocal.getDateLimitOfDateFrameByDate(dateFrameId, date)
        if (list.isNotEmpty()) {
            return list.first()
        }
        return null
    }

    fun getTotalExpensesForDate(dateLimitWithTransactions: DateLimitWithTransactions): Double {
        val allExpenses = dateLimitWithTransactions.transactions.filter { transaction -> transaction.transactionType == TRANSACTION_TYPE_EXPENSE }
        var totalExpense = 0.0
        for (expenses in allExpenses) {
            totalExpense += expenses.transactionAmount
        }
        return totalExpense
    }

    fun setExpenseLimit (expenseLimit: Double, currentDateLimit: DateLimit) {
        currentDateLimit.expenseLimit = expenseLimit
        upsertDateLimit(currentDateLimit)
    }

    fun updateLimitExceededValueOf (dateLimit: DateLimit, limitExceededValue: Double) {
        dateLimit.limitExceededValue = limitExceededValue
        upsertDateLimit(dateLimit)
    }
}