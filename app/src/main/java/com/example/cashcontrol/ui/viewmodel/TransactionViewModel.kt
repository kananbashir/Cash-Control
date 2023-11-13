package com.example.cashcontrol.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.data.entity.Transaction
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

    private var _allExpenses: List<Transaction> = listOf()
    val allExpenses: List<Transaction> get() = _allExpenses

    private var _allIncomes: List<Transaction> = listOf()
    val allIncomes: List<Transaction> get() = _allIncomes

    init {
        _allTransactions = cashControlRepository.transactionsLocal.getAllTransactionsFromDb()

        viewModelScope.launch {
            _allTransactions.collect { allTransactions ->
                _allExpenses = cashControlRepository.transactionsLocal.getAllExpenses(TRANSACTION_TYPE_EXPENSE)
                _allIncomes = cashControlRepository.transactionsLocal.getAllIncomes(TRANSACTION_TYPE_INCOME)
            }
        }
    }


    fun upsertTransaction (transaction: Transaction) = viewModelScope.launch {
        cashControlRepository.transactionsLocal.upsertTransaction(transaction)
    }

    fun upsertAllTransactions (vararg transaction: Transaction) = viewModelScope.launch {
        cashControlRepository.transactionsLocal.upsertAllTransactions(*transaction)
    }

    fun deleteTransaction (transaction: Transaction) = viewModelScope.launch {
        cashControlRepository.transactionsLocal.deleteTransaction(transaction)
    }

//    private fun sortTransactionsByDateDescending(allUnsortedTransactions: List<Transaction>): List<Transaction> {
//        val formatter = DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN)
//        val sortedTransactions = allUnsortedTransactions.sortedByDescending {
//            LocalDate.parse(it.date, formatter)
//        }
//        return sortedTransactions
//    }
}