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
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.Profile
import com.example.cashcontrol.data.db.entity.User
import com.example.cashcontrol.databinding.FragmentUpdateEndPointDateBottomSheetBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.MessageUtil.showNotifyingMessage
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class UpdateEndPointDateBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentUpdateEndPointDateBottomSheetBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private var oldEndPointDate: LocalDate? = null
    private var oldStartPointDate: LocalDate? = null
    private var onlineUser: User? = null
    private var onlineProfile: Profile? = null
    private var unfinishedAndOnlineDateFrame: DateFrame? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        dateFrameViewModel = ViewModelProvider(requireActivity()).get(DateFrameViewModel::class.java)
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
                            oldEndPointDate = LocalDate.parse(unfinishedDateFrame.endPointDate, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
                            oldStartPointDate = LocalDate.parse(unfinishedDateFrame.startPointDate, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
                        }
                    }
                }
            }
        }

        binding.btDateFragUpdateEndPoint.setOnClickListener {
            unfinishedAndOnlineDateFrame?.let { unfinishedAndOnlineDateFrame ->
                oldEndPointDate?.let { oldDate ->
                    val dateValidator = DateValidatorPointForward.from(oldDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    val calendarConstraints = CalendarConstraints.Builder().setValidator(dateValidator)
                    val datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText(resources.getString(R.string.date_picker_title_text_end_point))
                        .setSelection(oldDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .setCalendarConstraints(calendarConstraints.build())
                        .build()

                    datePicker.addOnPositiveButtonClickListener { selection ->
                        val selectedDate: LocalDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                        if (selectedDate != oldEndPointDate) {
                            binding.btDateFragUpdateEndPoint.text = selectedDate.format(DateTimeFormatter.ofPattern(
                                DATE_LIMIT_DATE_PATTERN, Locale.US
                            ))
                        } else {
                            showErrorMessage(resources.getString(R.string.error_message_end_date_update_same_date), binding)
                        }
                    }

                    datePicker.show(childFragmentManager, "datePicker")
                }
            }
        }

        binding.btConfirmFragUpdateEndPoint.setOnClickListener {
            if (binding.btDateFragUpdateEndPoint.text.toString() != resources.getString(R.string.text_button_select_end_point)) {
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        val date = binding.btDateFragUpdateEndPoint.text.toString()
                        val parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
                        if ((parsedDate.year - oldStartPointDate!!.year) > 1) {
                            showErrorMessage(resources.getString(R.string.error_message_onboarding_dateframe_year_gap_not_allowed), binding)
                        } else {
                            showNotifyingMessage(resources.getString(R.string.notifying_message_end_date_update_success), binding)
                            dateFrameViewModel.updateDate(date, onlineProfile!!.profileId!!)
                            dismiss()
                        }
                    }
                }
            } else {
                showErrorMessage(resources.getString(R.string.error_message_end_date_update_no_date), binding)
            }
        }

        return binding.root
    }
}