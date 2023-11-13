package com.example.cashcontrol.data.network.local

import com.example.cashcontrol.data.db.TransactionDao
import com.example.cashcontrol.data.entity.Transaction
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

    suspend fun getAllExpenses (transactionType: String): List<Transaction> {
        return transactionDao.getAllExpenses(transactionType)
    }

    suspend fun getAllIncomes (transactionType: String): List<Transaction> {
        return transactionDao.getAllIncomes(transactionType)
    }

}