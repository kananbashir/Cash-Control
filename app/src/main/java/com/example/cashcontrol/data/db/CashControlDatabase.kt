package com.example.cashcontrol.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Profile
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.data.db.entity.User
import com.example.cashcontrol.data.db.entity.income.Income
import com.example.cashcontrol.data.db.entity.expense.Expense
import com.example.cashcontrol.util.converter.TypeConverter

@TypeConverters (TypeConverter::class)
@Database (
    entities = [User::class, Profile::class, DateFrame::class, DateLimit::class, Expense::class, Income::class, Transaction::class],
    version = 40,
    exportSchema = false)
abstract class CashControlDatabase: RoomDatabase() {

    abstract fun getUserDao (): UserDao
    abstract fun getProfileDao (): ProfileDao
    abstract fun getDateFrameDao (): DateFrameDao
    abstract fun getDateLimitDao (): DateLimitDao
    abstract fun getTransactionDao (): TransactionDao

}