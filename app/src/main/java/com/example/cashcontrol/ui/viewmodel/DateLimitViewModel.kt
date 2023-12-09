package com.example.cashcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.data.db.entity.relation.DateLimitWithTransactions
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DateLimitViewModel @Inject constructor(private val cashControlRepository: CashControlRepository): ViewModel() {

    private var _allDateLimits: Flow<List<DateLimit>> = cashControlRepository.dateLimitLocal.getAllDateLimitsFromDb()
    val allDateLimits: Flow<List<DateLimit>> get() = _allDateLimits

    fun upsertDateLimit (dateLimit: DateLimit) = viewModelScope.launch {
        cashControlRepository.dateLimitLocal.upsertDateLimit(dateLimit)
    }

    fun upsertAllDateLimits (vararg dateLimit: DateLimit) = viewModelScope.launch {
        cashControlRepository.dateLimitLocal.upsertAllDateLimit(*dateLimit)
    }

    fun deleteDateLimit (dateLimit: DateLimit) = viewModelScope.launch {
        cashControlRepository.dateLimitLocal.deleteDateLimit(dateLimit)
    }

    suspend fun getDateLimitWithTransactions (dateLimitId: Int): DateLimitWithTransactions? {
        val dateLimitWithTransactions = cashControlRepository.dateLimitLocal.getDateLimitWithTransactions(dateLimitId)
        if (dateLimitWithTransactions.isNotEmpty()) {
            return dateLimitWithTransactions.first()
        }
        return null
    }

    suspend fun getCurrentDateLimitByDateFrame (dateFrameId: Int): DateLimit? {
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
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

    fun getExpensesForCurrentDate (dateLimitWithTransactions: DateLimitWithTransactions): List<Transaction> {
        val expenseList = mutableListOf<Transaction>()
        for (transaction in dateLimitWithTransactions.transactions) {
            if (transaction.transactionType == TRANSACTION_TYPE_EXPENSE) {
                expenseList.add(transaction)
            }
        }
        return expenseList
    }

    fun setExpenseLimit (expenseLimit: Double, currentDateLimit: DateLimit) {
        currentDateLimit.expenseLimit = expenseLimit
        upsertDateLimit(currentDateLimit)
    }
}