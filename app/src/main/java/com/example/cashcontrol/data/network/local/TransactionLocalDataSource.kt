package com.example.cashcontrol.data.network.local

import com.example.cashcontrol.data.db.TransactionDao
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_INCOME
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionLocalDataSource @Inject constructor(private val transactionDao: TransactionDao) {

    suspend fun upsertTransaction (transaction: Transaction) {
        transactionDao.upsertTransaction(transaction)
    }

    suspend fun upsertAllTransactions (vararg transaction: Transaction) {
        transactionDao.upsertAllTransactions(*transaction)
    }

    suspend fun deleteTransaction (transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getAllTransactionsFromDb (): Flow<List<Transaction>> {
        return transactionDao.getAllTransactionsFromDb()
    }

    suspend fun getAllTransactionsByDateFrame (dateFrameId: Int): List<Transaction> {
        return transactionDao.getAllTransactionsByDateFrame(dateFrameId)
    }

    suspend fun getTransactionOfDateFrameById (dateFrameId: Int, transactionId: Int): List<Transaction> {
        return transactionDao.getTransactionOfDateFrameById(dateFrameId, transactionId)
    }

    suspend fun getAllExpensesByDateFrame (dateFrameId: Int): List<Transaction> {
        return transactionDao.getAllExpensesByDateFrame(TRANSACTION_TYPE_EXPENSE, dateFrameId)
    }

    suspend fun getAllIncomesByDateFrame (dateFrameId: Int): List<Transaction> {
        return transactionDao.getAllIncomesByDateFrame(TRANSACTION_TYPE_INCOME, dateFrameId)
    }

}