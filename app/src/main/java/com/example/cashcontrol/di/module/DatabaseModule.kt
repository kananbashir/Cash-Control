package com.example.cashcontrol.di.module

import android.content.Context
import androidx.room.Room
import com.example.cashcontrol.data.db.CashControlDatabase
import com.example.cashcontrol.data.db.DateFrameDao
import com.example.cashcontrol.data.db.DateLimitDao
import com.example.cashcontrol.data.db.ProfileDao
import com.example.cashcontrol.data.db.TransactionDao
import com.example.cashcontrol.data.db.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn (SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideCashControlDatabase (@ApplicationContext context: Context): CashControlDatabase {
        return Room.databaseBuilder(
            context,
            CashControlDatabase::class.java,
            "Cash_control_db.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideUserDao (cashControlDatabase: CashControlDatabase): UserDao {
        return cashControlDatabase.getUserDao()
    }

    @Singleton
    @Provides
    fun provideProfileDao (cashControlDatabase: CashControlDatabase): ProfileDao {
        return cashControlDatabase.getProfileDao()
    }

    @Singleton
    @Provides
    fun provideTransactionDao (cashControlDatabase: CashControlDatabase): TransactionDao {
        return cashControlDatabase.getTransactionDao()
    }

    @Singleton
    @Provides
    fun provideDateLimitDao (cashControlDatabase: CashControlDatabase): DateLimitDao {
        return cashControlDatabase.getDateLimitDao()
    }

    @Singleton
    @Provides
    fun provideDateFrameDao (cashControlDatabase: CashControlDatabase): DateFrameDao {
        return cashControlDatabase.getDateFrameDao()
    }

}