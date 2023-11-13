package com.example.cashcontrol.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavArgs
import com.example.cashcontrol.data.repository.CashControlRepository
import com.example.cashcontrol.data.entity.DateFrame
import com.example.cashcontrol.data.entity.Transaction
import com.example.cashcontrol.data.entity.relation.DateFrameWithDateLimits
import com.example.cashcontrol.data.entity.relation.DateFrameWithTransactions
import com.example.cashcontrol.data.entity.relation.ProfileWithDateFrames
import com.example.cashcontrol.ui.fragment.onboarding.OnBoardingFinishFragmentArgs
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DateFrameViewModel @Inject constructor(
    private val cashControlRepository: CashControlRepository
): ViewModel() {

    val emittingComplete: MutableLiveData<Boolean> = MutableLiveData()

    private var _allDateFrames: Flow<List<DateFrame>> = cashControlRepository.dateFrameLocal.getAllDateFramesFromDb()
    val allDateFrames: Flow<List<DateFrame>> get() = _allDateFrames

    private var _unfinishedDateFrame: MutableStateFlow<DateFrame?> = MutableStateFlow(null)
    val unfinishedDateFrame: StateFlow<DateFrame?> get() = _unfinishedDateFrame

    private var _dateFrameWithDateLimits: List<DateFrameWithDateLimits> = listOf()
    val dateFrameWithDateLimits: List<DateFrameWithDateLimits> get() = _dateFrameWithDateLimits

    private var _dateFrameWithTransactions: List<DateFrameWithTransactions> = listOf()
    val dateFrameWithTransactions: List<DateFrameWithTransactions> get() = _dateFrameWithTransactions

    var uiAnimState: Boolean = false // while it is true, incrementing animations in UI will not work..

    init {
        viewModelScope.launch {
            unfinishedDateFrame.collect {
                it?.let { unfinishedDf ->
                    _dateFrameWithDateLimits = cashControlRepository.dateFrameLocal.getDateFrameWithDateLimits(unfinishedDf.dateFrameId!!)
                    _dateFrameWithTransactions = cashControlRepository.dateFrameLocal.getDateFrameWithTransactions(unfinishedDf.dateFrameId!!)
                }
            }
        }
    }

    fun upsertDateFrame (dateFrame: DateFrame) = viewModelScope.launch {
        cashControlRepository.dateFrameLocal.upsertDateFrame(dateFrame)
    }

    fun deleteDateFrame (dateFrame: DateFrame) = viewModelScope.launch {
        cashControlRepository.dateFrameLocal.deleteDateFrame(dateFrame)
    }

    fun checkUnfinishedDateFrame (profileWithDateFrames: ProfileWithDateFrames, foundUnfinishedDf: (DateFrame?) -> Unit) {
        Log.i("TCPQQ","Checking unfinished date frame")
        val found = profileWithDateFrames.dateFrames.find { df -> df.isUnfinished }
        found?.let {
            _unfinishedDateFrame.value = it
        }

        foundUnfinishedDf(found)
    }

    fun updateUnfinishedDateFrame (profileId: Int) = viewModelScope.launch {
        val allDateFramesList = allDateFrames.firstOrNull()
        allDateFramesList?.let {
            if (it.isNotEmpty()) {
                val foundDateFrame = it.find { df -> df.isUnfinished && df.profileId == profileId }
                foundDateFrame?.let { unfinishedDf ->
                    _unfinishedDateFrame.value = unfinishedDf
                }
            }
        }
    }

    fun getPreviousDayLimitExceedValue (): Double {
        if (dateFrameWithDateLimits.isNotEmpty()) {
            val previousDay = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
            val allDateLimits = dateFrameWithDateLimits.first().dateLimits
            val previousDayDateLimit = allDateLimits.find { dl -> dl.date == previousDay }

            previousDayDateLimit?.let {
                return it.limitExceededValue
            }
        }

        return 0.0
    }

    fun updateExpenseAmount (expenseAmount: Double) {
        _unfinishedDateFrame.value?.let {
            it.totalExpenseOfAll += expenseAmount
            upsertDateFrame(it)
        }
    }

    fun updateIncomeAmount (incomeAmount: Double) {
        _unfinishedDateFrame.value?.let {
            it.totalIncomeOfAll += incomeAmount
            upsertDateFrame(it)
        }
    }

    fun getSortedTransactions(): List<Transaction> {
        val formatter = DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN)
        val sortedTransactions = dateFrameWithTransactions.first().transactions.sortedByDescending {
            LocalDate.parse(it.date, formatter)
        }

        return sortedTransactions
    }

    fun checkDateFrames (profileWithDateFrames: ProfileWithDateFrames, navArgs: OnBoardingFinishFragmentArgs): Boolean {
        for (dateFrame in profileWithDateFrames.dateFrames) {
            if (dateFrame.startPointDate == navArgs.startPointDate && dateFrame.endPointDate == navArgs.endPointDate) {
                return false
            }
        }
        upsertDateFrame(
            DateFrame(
                navArgs.startPointDate,
                navArgs.endPointDate,
                navArgs.budget.toDouble(),
                navArgs.currency,
                navArgs.saving.toDouble(),
                true,
                profileWithDateFrames.profile.profileId!!
            )
        )
        return true
    }
}