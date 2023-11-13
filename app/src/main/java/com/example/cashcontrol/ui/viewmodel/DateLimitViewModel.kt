package com.example.cashcontrol.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.adapter.TransactionPair
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.data.entity.DateLimit
import com.example.cashcontrol.data.entity.Transaction
import com.example.cashcontrol.data.entity.relation.DateFrameWithDateLimits
import com.example.cashcontrol.data.entity.relation.DateLimitWithTransactions
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_INCOME
import com.example.cashcontrol.util.extension.concatenateCategories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DateLimitViewModel @Inject constructor(private val cashControlRepository: CashControlRepository): ViewModel() {

    val emittingComplete: MutableLiveData<Boolean> = MutableLiveData()

    private var _allDateLimits: Flow<List<DateLimit>> = cashControlRepository.dateLimitLocal.getAllDateLimitsFromDb()
    val allDateLimits: Flow<List<DateLimit>> get() = _allDateLimits

    private var _currentDateLimit: MutableStateFlow<DateLimit?> = MutableStateFlow(null)
    val currentDateLimit: StateFlow<DateLimit?> get() = _currentDateLimit

    private var _dateLimitWithTransactions: List<DateLimitWithTransactions> = listOf()
    val dateLimitWithTransactions: List<DateLimitWithTransactions> get() = _dateLimitWithTransactions

    private var _allDateLimitsWithTransactions: MutableLiveData<List<TransactionPair>> = MutableLiveData()
    val allDateLimitsWithTransactions: LiveData<List<TransactionPair>> get() = _allDateLimitsWithTransactions

    var selectedTransaction: Transaction? = null

    var uiAnimState: Boolean = false // while it is true, incrementing animations in UI will not work..

    init {
        viewModelScope.launch {
            currentDateLimit.collect {
                it?.let { currentDateLimit ->
                    _dateLimitWithTransactions = cashControlRepository.dateLimitLocal.getDateLimitWithTransactions(currentDateLimit.dateLimitId!!)
                }
            }
        }
    }

    fun upsertDateLimit (dateLimit: DateLimit) = viewModelScope.launch {
        cashControlRepository.dateLimitLocal.upsertDateLimit(dateLimit)
    }

    fun upsertAllDateLimits (vararg dateLimit: DateLimit) = viewModelScope.launch {
        cashControlRepository.dateLimitLocal.upsertAllDateLimit(*dateLimit)
    }

    fun deleteDateLimit (dateLimit: DateLimit) = viewModelScope.launch {
        cashControlRepository.dateLimitLocal.deleteDateLimit(dateLimit)
    }

    fun checkCurrentDateLimit (dateFrameWithDateLimits: DateFrameWithDateLimits, foundCurrentDateLimit: (DateLimit?) -> Unit) {
        val found = dateFrameWithDateLimits.dateLimits.find { dl ->
            dl.date == LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
        }
        found?.let {
            _currentDateLimit.value = it
        }

        foundCurrentDateLimit(found)
    }

    fun getExpensesForCurrentDate (callback: (List<Transaction>) -> Unit) {
        viewModelScope.launch {
            val expenseList = mutableListOf<Transaction>()
            val dateLimitWithTransactions = cashControlRepository.dateLimitLocal.getDateLimitWithTransactions(currentDateLimit.value?.dateLimitId!!)
            for (transaction in dateLimitWithTransactions.first().transactions) {
                if (transaction.transactionType == TRANSACTION_TYPE_EXPENSE) {
                    expenseList.add(transaction)
                }
            }

            callback (expenseList)
        }
    }

    fun getIncomesForCurrentDate (callback: (List<Transaction>) -> Unit) {
        viewModelScope.launch {
            val incomeList = mutableListOf<Transaction>()
            val dateLimitWithTransactions = cashControlRepository.dateLimitLocal.getDateLimitWithTransactions(currentDateLimit.value?.dateLimitId!!)
            for (transaction in dateLimitWithTransactions.first().transactions) {
                if (transaction.transactionType == TRANSACTION_TYPE_INCOME) {
                    incomeList.add(transaction)
                }
            }

            callback (incomeList)
        }
    }

    fun setExpenseLimit (expenseLimit: Double) {
        _currentDateLimit.value?.let {
            it.expenseLimit = expenseLimit
            upsertDateLimit(it)
        }
    }

    fun startCollectingPairs() {
        viewModelScope.launch {
            allDateLimits.collect { allDateLimits ->
                delay(1500)
                _allDateLimitsWithTransactions.postValue(collectAllDateLimitsWithTransactions(
                    allDateLimits.sortedByDescending { LocalDate.parse(it.date, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN)) }))
            }
        }
    }

    private suspend fun collectAllDateLimitsWithTransactions(allDateLimit: List<DateLimit>): MutableList<TransactionPair> {
        var allDateLimitsWithTransactions = mutableListOf<TransactionPair>()

        if (allDateLimit.isNotEmpty()) {
            for (dateLimit in allDateLimit) {
                val dateLimitWithTransactions = cashControlRepository.dateLimitLocal.getDateLimitWithTransactions(dateLimit.dateLimitId!!)
                if (dateLimitWithTransactions.isNotEmpty()) {
                    val transactions = dateLimitWithTransactions.first().transactions
                    if (transactions.isNotEmpty()) {
                        val pair = TransactionPair(dateLimit, transactions)
                        allDateLimitsWithTransactions.add(pair)
                    }
                }
            }
        }

        return allDateLimitsWithTransactions
    }

    fun getFilteredList (query: String): List<TransactionPair> {
        val foundTransactionPairsList = mutableListOf<TransactionPair>()

        for (i in allDateLimitsWithTransactions.value!!) {
            for (j in i.transactionList) {
                when {
                    j.transactionCategory.contains(query, true) -> { foundTransactionPairsList.add(i) }
                    j.transactionCategories.concatenateCategories().contains(query, true) -> { foundTransactionPairsList.add(i) }
                    j.transactionSource.contains(query, true) -> { foundTransactionPairsList.add(i) }
                    j.transactionSources.concatenateCategories().contains(query, true) -> { foundTransactionPairsList.add(i) }
                    j.transactionAmount.toString().contains(query, true) -> { foundTransactionPairsList.add(i) }
                    j.transactionDescription.contains(query, true) -> { foundTransactionPairsList.add(i) }
                    j.transactionCurrency.contains(query, true) -> { foundTransactionPairsList.add(i) }
                }
            }
        }
        return foundTransactionPairsList.toList()
    }

    fun setSelectionState (transaction: Transaction) {
        var updatedPairList = listOf<TransactionPair>()

            if (selectedTransaction == null) {
                updatedPairList = updateTransactionPairList(allDateLimitsWithTransactions.value!! ,transaction, true)
            } else {
                if (selectedTransaction?.transactionId == transaction.transactionId) {
                    updatedPairList = updateTransactionPairList(allDateLimitsWithTransactions.value!!, selectedTransaction!!, false)
                } else {

                    updatedPairList = updateTransactionPairList(
                        updateTransactionPairList(allDateLimitsWithTransactions.value!!, selectedTransaction!!, false),
                        transaction,
                        true)
                }
            }

        _allDateLimitsWithTransactions.postValue(updatedPairList)

    }

    private fun updateTransactionPairList (transactionPairList: List<TransactionPair> ,transaction: Transaction, state: Boolean): List<TransactionPair> {
        var updatedPairList = listOf<TransactionPair>()
            val foundTransactionPair = transactionPairList.find { tr -> tr.dateLimit.date == transaction.date }
            val transactionList = foundTransactionPair?.transactionList
            val updatedTransactionList = updateTransactionListWith(state, transactionList!!, transaction)
            if (state) {
                selectedTransaction = updatedTransactionList.find { t -> t.isSelected }
            } else {
                selectedTransaction = null
            }
            val updatedPair = foundTransactionPair.copy(transactionList = updatedTransactionList)
            updatedPairList = transactionPairList.map { transactionPair ->
                if (transactionPair.dateLimit.date == updatedPair.dateLimit.date) {
                    updatedPair
                } else {
                    transactionPair
                }
            }
        return updatedPairList
    }

    private fun updateTransactionListWith (state: Boolean, transactionList: List<Transaction>, newTransaction: Transaction): List<Transaction> {
        return transactionList.map { mappedTransaction ->
            if (mappedTransaction.transactionId == newTransaction.transactionId) {
                val updatedTransaction = Transaction().apply {
                    transactionId = mappedTransaction.transactionId
                    transactionAmount = mappedTransaction.transactionAmount
                    transactionCurrency = mappedTransaction.transactionCurrency
                    transactionCategory = mappedTransaction.transactionCategory
                    transactionCategories = mappedTransaction.transactionCategories
                    transactionSource = mappedTransaction.transactionSource
                    transactionSources = mappedTransaction.transactionSources
                    transactionDescription = mappedTransaction.transactionDescription
                    date = mappedTransaction.date
                    transactionType = mappedTransaction.transactionType
                    isSelected = state
                }
                updatedTransaction
            } else {
                mappedTransaction
            }
        }
    }

    fun clearPairList () {
       _allDateLimitsWithTransactions.postValue(listOf())
    }
}