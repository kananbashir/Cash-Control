package com.example.cashcontrol.ui.fragment.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cashcontrol.R
import com.example.cashcontrol.data.DateLimitCalculator
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.Profile
import com.example.cashcontrol.data.db.entity.User
import com.example.cashcontrol.databinding.FragmentUpdateEndPointDateBottomSheetBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.DateFrameUtil.checkBudgetAndSubsequentDaysCompatibility
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.MessageUtil.showNotifyingMessage
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.extension.convertDateFromIso8601To
import com.example.cashcontrol.util.extension.convertDateToIso8601From
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UpdateEndPointDateBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentUpdateEndPointDateBottomSheetBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private var oldEndPointDate: LocalDate? = null
    private var oldStartPointDate: LocalDate? = null
    private var onlineUser: User? = null
    private var onlineProfile: Profile? = null
    private var unfinishedAndOnlineDateFrame: DateFrame? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        dateFrameViewModel = ViewModelProvider(requireActivity())[DateFrameViewModel::class.java]
        dateLimitViewModel = ViewModelProvider(requireActivity())[DateLimitViewModel::class.java]
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        binding = FragmentUpdateEndPointDateBottomSheetBinding.inflate(layoutInflater)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.getOnlineUser()?.let { u ->
                    onlineUser = u
                    profileViewModel.getOnlineProfileById(onlineUser!!.userId!!)?.let { p ->
                        onlineProfile = p
                        dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile!!.profileId!!)?.let { unfinishedDateFrame ->
                            unfinishedAndOnlineDateFrame = unfinishedDateFrame
                            oldEndPointDate = LocalDate.parse(unfinishedDateFrame.endPointDate, DateTimeFormatter.ISO_LOCAL_DATE)
                            oldStartPointDate = LocalDate.parse(unfinishedDateFrame.startPointDate, DateTimeFormatter.ISO_LOCAL_DATE)
                        }
                    }
                }
            }
        }

        binding.btDateFragUpdateEndPoint.setOnClickListener {
            oldEndPointDate?.let { oldDate ->
                val dateValidator =
                    DateValidatorPointForward.from(oldDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                val calendarConstraints = CalendarConstraints.Builder().setValidator(dateValidator)
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(resources.getString(R.string.date_picker_title_text_end_point))
                    .setSelection(oldDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .setCalendarConstraints(calendarConstraints.build())
                    .build()

                datePicker.addOnPositiveButtonClickListener { selection ->
                    val selectedDate: LocalDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                    if (selectedDate != oldEndPointDate) {
                        binding.btDateFragUpdateEndPoint.text = selectedDate
                            .format(DateTimeFormatter.ISO_LOCAL_DATE)
                            .convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                    } else {
                        showErrorMessage(resources.getString(R.string.error_message_end_date_update_same_date), binding)
                    }
                }

                datePicker.show(childFragmentManager, "datePicker")
            }
        }

        binding.btConfirmFragUpdateEndPoint.setOnClickListener {
            unfinishedAndOnlineDateFrame?.let { unfinishedDateFrame ->
                if (binding.btDateFragUpdateEndPoint.text.toString() != resources.getString(R.string.text_button_select_end_point)) {
                    val date = binding.btDateFragUpdateEndPoint.text.toString().convertDateToIso8601From(DATE_LIMIT_DATE_PATTERN)
                    val parsedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                    if ((parsedDate.year - oldStartPointDate!!.year) <= 1) {
                        if (checkBudgetAndSubsequentDaysCompatibility(
                                binding.btDateFragUpdateEndPoint.text.toString().convertDateToIso8601From(DATE_LIMIT_DATE_PATTERN),
                                unfinishedDateFrame.getRemainingBudget()
                            )
                        ) {
                            viewLifecycleOwner.lifecycleScope.launch {
                                repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    showNotifyingMessage(resources.getString(R.string.notifying_message_end_date_update_success), binding)
                                    dateFrameViewModel.updateDate(date, onlineProfile!!.profileId!!)
                                    delay(300)
                                    reCalculateAllExpenseLimits()
                                }
                            }
                        } else {
                            showErrorMessage(resources.getString(R.string.error_message_end_date_update_incompatible_dateframe_and_budget), binding)
                        }
                    } else {
                        showErrorMessage(resources.getString(R.string.error_message_onboarding_dateframe_year_gap_not_allowed), binding)
                    }
                } else {
                    showErrorMessage(resources.getString(R.string.error_message_end_date_update_no_date), binding)
                }
            }
        }

        return binding.root
    }

    private suspend fun reCalculateAllExpenseLimits() {
        userViewModel.getOnlineUser()?.let { onlineUser ->
            profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile.profileId!!)?.let { unfinishedDf ->
                    dateFrameViewModel.getDateFrameWithTransactions(unfinishedDf.dateFrameId!!)?.let { dateFrameWithTransactions ->
                        dateFrameViewModel.getDateFrameWithDateLimits(unfinishedDf.dateFrameId!!)?.let { dateFrameWithDateLimits ->
                            val updatedDateLimitList = DateLimitCalculator()
                                .setDateFrame(unfinishedDf)
                                .reCalculateAllExpenseLimits(
                                dateFrameWithDateLimits.dateLimits,
                                dateFrameWithTransactions
                            )

                            dateLimitViewModel.upsertAllDateLimits(*updatedDateLimitList.toTypedArray())
                            transactionViewModel.newExpense = true
                            transactionViewModel.newIncome = true
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}