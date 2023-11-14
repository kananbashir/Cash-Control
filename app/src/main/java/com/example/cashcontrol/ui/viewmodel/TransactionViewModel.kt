package com.example.cashcontrol.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_INCOME
import dagger.hilt.android.lifecycle.HiltViewModel
import hilt_aggregated_deps._com_example_cashcontrol_ui_viewmodel_DateLimitViewModel_HiltModules_BindsModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(private val cashControlRepository: CashControlRepository): ViewModel() {

    private var _allTransactions: Flow<List<Transaction>> = emptyFlow()
    val allTransactions: Flow<List<Transaction>> get() = _allTransactions

    var newExpense: Boolean = true
    var newIncome: Boolean = true


    fun upsertTransaction (transaction: Transaction) = viewModelScope.launch {
        cashControlRepository.transactionsLocal.upsertTransaction(transaction)
        if (transaction.transactionType == TRANSACTION_TYPE_EXPENSE) {
            newExpense = true
        } else {
            newIncome = true
        }
    }

    fun upsertAllTransactions (vararg transaction: Transaction) = viewModelScope.launch {
        cashControlRepository.transactionsLocal.upsertAllTransactions(*transaction)
    }

    fun deleteTransaction (transaction: Transaction) = viewModelScope.launch {
        cashControlRepository.transactionsLocal.deleteTransaction(transaction)
    }

    suspend fun getTransactionOfDateFrameById (dateFrameId: Int, transactionId: Int): Transaction? {
        val transactionList = cashControlRepository.transactionsLocal.getTransactionOfDateFrameById(dateFrameId, transactionId)
        if (transactionList.isNotEmpty()) {
            return transactionList.first()
        }
        return null
    }
}