package com.example.cashcontrol.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cashcontrol.data.entity.DateFrame
import com.example.cashcontrol.data.entity.DateLimit
import com.example.cashcontrol.data.entity.Profile
import com.example.cashcontrol.data.entity.Transaction
import com.example.cashcontrol.data.entity.User
import com.example.cashcontrol.data.entity.income.BatchIncome
import com.example.cashcontrol.data.entity.income.Income
import com.example.cashcontrol.data.entity.income.SingleIncome
import com.example.cashcontrol.data.entity.expense.BatchExpense
import com.example.cashcontrol.data.entity.expense.Expense
import com.example.cashcontrol.data.entity.expense.SingleExpense
import com.example.cashcontrol.util.converter.TypeConverter

@TypeConverters (TypeConverter::class)
@Database (
    entities = [User::class, Profile::class, DateFrame::class, DateLimit::class, SingleExpense::class, BatchExpense::class, SingleIncome::class, BatchIncome::class, Expense::class, Income::class, Transaction::class],
    version = 35,
    exportSchema = false)
abstract class CashControlDatabase: RoomDatabase() {

    abstract fun getUserDao (): UserDao
    abstract fun getProfileDao (): ProfileDao
    abstract fun getDateFrameDao (): DateFrameDao
    abstract fun getDateLimitDao (): DateLimitDao
    abstract fun getTransactionDao (): TransactionDao

}