package com.example.cashcontrol.data.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.data.db.entity.expense.Expense
import com.example.cashcontrol.util.constant.TransactionConstant.TRANSACTION_TYPE_EXPENSE
import kotlinx.coroutines.flow.Flow

//@Dao
interface ExpenseDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExpense (transaction: Transaction)

    @Delete
    suspend fun deleteExpense (transaction: Transaction)

    @Query ("SELECT * FROM transaction_table")
    fun getAllTransactionsFromDb (): Flow<List<Transaction>>

    @Query ("SELECT * FROM transaction_table WHERE transactionType = $TRANSACTION_TYPE_EXPENSE")
    fun getAllExpenses (): Flow<List<Expense>>

//    @Query ("SELECT * FROM single_expense_table")
//    fun getAllSingleExpensesFromDatabase (): Flow<List<SingleExpense>>

//    @Query ("SELECT * FROM bulk_expense_table")
//    fun getAllBulkExpensesFromDatabase (): Flow<List<BulkExpense>>
//
//    @Query ("SELECT * FROM single_expense_table UNION SELECT * FROM bulk_expense_table")
//    fun getAllExpensesFromDatabase (): Flow<List<Expense>>



}