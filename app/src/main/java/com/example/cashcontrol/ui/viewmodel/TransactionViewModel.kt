package com.example.cashcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_INCOME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(private val cashControlRepository: CashControlRepository): ViewModel() {

    var newExpense: Boolean = true
    var newIncome: Boolean = true

    fun upsertTransaction (transaction: Transaction) = viewModelScope.launch {
        cashControlRepository.transactionsLocal.upsertTransaction(transaction)
        if (transaction.transactionType == TRANSACTION_TYPE_EXPENSE) {
            newExpense = true
        } else if (transaction.transactionType == TRANSACTION_TYPE_INCOME) {
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