package com.example.cashcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashcontrol.adapter.TransactionPair
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithDateLimits
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithTransactions
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.extension.concatenateCategories
import com.example.cashcontrol.util.extension.sortDateLimitsByDateDescending
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DateFrameViewModel @Inject constructor(
    private val cashControlRepository: CashControlRepository
): ViewModel() {

    private var _allDateFrames: Flow<List<DateFrame>> = cashControlRepository.dateFrameLocal.getAllDateFramesFromDb()
    val allDateFrames: Flow<List<DateFrame>> get() = _allDateFrames

    private var _allTransactionPairs: MutableStateFlow<List<TransactionPair>> = MutableStateFlow(listOf())
    val allTransactionPairs: StateFlow<List<TransactionPair>> get() = _allTransactionPairs

    private var _selectedTransaction: Transaction? = null
    val selectedTransaction: Transaction? get() = _selectedTransaction

    fun upsertDateFrame (dateFrame: DateFrame) = viewModelScope.launch {
        cashControlRepository.dateFrameLocal.upsertDateFrame(dateFrame)
    }

    fun deleteDateFrame (dateFrame: DateFrame) = viewModelScope.launch {
        cashControlRepository.dateFrameLocal.deleteDateFrame(dateFrame)
    }

    suspend fun getUnfinishedAndOnlineDateFrameByProfile (profileId: Int): DateFrame? {
        val dateFrameList = cashControlRepository.dateFrameLocal.getUnfinishedAndOnlineDateFrameByProfile(profileId)
        if (dateFrameList.isNotEmpty()) {
            return dateFrameList.first()
        }
        return null
    }

    suspend fun getOnlineDateFrameByProfile (profileId: Int): DateFrame? {
        val dateFrameList = cashControlRepository.dateFrameLocal.getOnlineDateFrameByProfile(profileId)
        if (dateFrameList.isNotEmpty()) {
            return dateFrameList.first()
        }
        return null
    }

    suspend fun getDateFrameOfProfileByDates (startPointDate: String, endPointDate: String, profileId: Int): DateFrame? {
        val dateFrameList = cashControlRepository.dateFrameLocal.getDateFrameOfProfileByDates(startPointDate, endPointDate, profileId)
        if (dateFrameList.isNotEmpty()) {
            return dateFrameList.first()
        }
        return null
    }

    suspend fun getDateFrameWithDateLimits (dateFrameId: Int): DateFrameWithDateLimits? {
        val list = cashControlRepository.dateFrameLocal.getDateFrameWithDateLimits(dateFrameId)
        if (list.isNotEmpty()) {
            return list.first()
        }
        return null
    }

    fun getPreviousDayLimitExceedValue (dateFrameWithDateLimits: DateFrameWithDateLimits): Double {
        val allDateLimits = dateFrameWithDateLimits.dateLimits
        if (allDateLimits.isNotEmpty()) {
            val previousDay = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
            val previousDayDateLimit = allDateLimits.find { dl -> dl.date == previousDay }
            previousDayDateLimit?.let {
                return it.limitExceededValue
            }
        }

        return 0.0
    }

    fun updateExpenseAmount (expenseAmount: Double, unfinishedDateFrame: DateFrame) {
        unfinishedDateFrame.totalExpenseOfAll += expenseAmount
        upsertDateFrame(unfinishedDateFrame)
    }

    fun updateIncomeAmount (incomeAmount: Double, unfinishedDateFrame: DateFrame) {
        unfinishedDateFrame.totalIncomeOfAll += incomeAmount
        upsertDateFrame(unfinishedDateFrame)
    }

    suspend fun getDateFrameWithTransactions (dateFrameId: Int): DateFrameWithTransactions? {
        val dateFrameWithTransactions = cashControlRepository.dateFrameLocal.getDateFrameWithTransactions(dateFrameId)
        if (dateFrameWithTransactions.isNotEmpty()) {
            return dateFrameWithTransactions.first()
        }
        return null
    }

    suspend fun getTransactionPairs (unfinishedDateFrame: DateFrame): List<TransactionPair> {
        if (allTransactionPairs.value.isEmpty()) {
            getDateFrameWithDateLimits(unfinishedDateFrame.dateFrameId!!)?.let { dateFrameWithDateLimits ->
                val allDateLimits = dateFrameWithDateLimits.dateLimits.sortDateLimitsByDateDescending()
                val allTransactionPairs = mutableListOf<TransactionPair>()
                if (allDateLimits.isNotEmpty()) {
                    for (dateLimit in allDateLimits) {
                        val dateLimitWithTransactions =
                            cashControlRepository.dateLimitLocal.getDateLimitWithTransactions(dateLimit.dateLimitId!!)
                        if (dateLimitWithTransactions.isNotEmpty()) {
                            val transactions = dateLimitWithTransactions.first().transactions
                            if (transactions.isNotEmpty()) {
                                val transactionPair = TransactionPair(dateLimit, transactions)
                                allTransactionPairs.add(transactionPair)
                                _allTransactionPairs.value = allTransactionPairs
                            }
                        }
                    }
                    return allTransactionPairs
                }
            }
        }

        return allTransactionPairs.value
    }

    suspend fun updateDate (date: String, profileId: Int) {
        getUnfinishedAndOnlineDateFrameByProfile(profileId)?.let { unfinishedDateFrame ->
            unfinishedDateFrame.endPointDate = date
            upsertDateFrame(unfinishedDateFrame)
        }
    }

    suspend fun updateSavingAmount (savingAmount: Double, profileId: Int) {
        getUnfinishedAndOnlineDateFrameByProfile(profileId)?.let { unfinishedDateFrame ->
            unfinishedDateFrame.savedMoney = savingAmount
            upsertDateFrame(unfinishedDateFrame)
        }
    }

    fun clearAllTransactionPairs () {
        _allTransactionPairs.value = listOf()
        _selectedTransaction = null
    }

    fun setSelectionState (transaction: Transaction) {
        val updatedPairList: List<TransactionPair>

        if (selectedTransaction == null) {
            updatedPairList = updateTransactionPairList(allTransactionPairs.value, transaction, true)
        } else {
            if (selectedTransaction?.transactionId == transaction.transactionId) {
                updatedPairList = updateTransactionPairList(allTransactionPairs.value, selectedTransaction!!, false)
            } else {

                updatedPairList = updateTransactionPairList(
                    updateTransactionPairList(allTransactionPairs.value, selectedTransaction!!, false),
                    transaction,
                    true)
            }
        }

        _allTransactionPairs.value = updatedPairList
    }

    private fun updateTransactionPairList (transactionPairList: List<TransactionPair>, transaction: Transaction, state: Boolean): List<TransactionPair> {
        val updatedPairList: List<TransactionPair>
        val foundTransactionPair = transactionPairList.find { tr -> tr.dateLimit.date == transaction.date }
        val transactionList = foundTransactionPair?.transactionList
        val updatedTransactionList = updateTransactionListWith(state, transactionList!!, transaction)
        if (state) {
            _selectedTransaction = updatedTransactionList.find { t -> t.isSelected }
        } else {
            _selectedTransaction = null
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
                    dateFrameId = mappedTransaction.dateFrameId
                    dateLimitId = mappedTransaction.dateLimitId
                    isSelected = state
                }
                updatedTransaction
            } else {
                mappedTransaction
            }
        }
    }

    fun getFilteredList (query: String): List<TransactionPair> {
        val foundTransactionPairsList = mutableListOf<TransactionPair>()

        if (allTransactionPairs.value.isNotEmpty()) {
            for (transactionPair in allTransactionPairs.value) {
                val tempTransactionList: MutableList<Transaction> = mutableListOf()
                for (transaction in transactionPair.transactionList) {
                    if ( transaction.transactionCategory.contains(query, true) ||
                        transaction.transactionCategories.concatenateCategories().contains(query, true) ||
                        transaction.transactionSource.contains(query, true) ||
                        transaction.transactionSources.concatenateCategories().contains(query, true) ||
                        transaction.transactionAmount.toString().contains(query, true) ||
                        transaction.transactionDescription.contains(query, true) ||
                        transaction.transactionCurrency.contains(query, true)) {

                        tempTransactionList.add(transaction)
                    }
                }
                if (tempTransactionList.isNotEmpty()) {
                    foundTransactionPairsList.add(TransactionPair(transactionPair.dateLimit, tempTransactionList))
                }
            }
            return foundTransactionPairsList.toList()
        }

        return listOf()
    }

    fun setDateFrameOffline(dateFrame: DateFrame) {
        dateFrame.isOnline = false
        upsertDateFrame(dateFrame)
    }

    fun setDateFrameOnline(dateFrame: DateFrame) {
        dateFrame.isOnline = true
        upsertDateFrame(dateFrame)
    }
}