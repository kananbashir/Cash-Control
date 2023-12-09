package com.example.cashcontrol.ui.fragment.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.databinding.FragmentOnBoardingDateFrameBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.UIStateConstant.END_POINT_DATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.START_POINT_DATE_KEY
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class OnBoardingDateFrameFragment : Fragment() {
    private lateinit var binding: FragmentOnBoardingDateFrameBinding
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private var startPointDate: LocalDate? = null
    private var endPointDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateFrameViewModel = ViewModelProvider (requireActivity()).get(DateFrameViewModel::class.java)
        binding = FragmentOnBoardingDateFrameBinding.inflate(layoutInflater)

        savedInstanceState?.let { bundle ->
            binding.apply {
                val startPointDateFromBundle = bundle.getString(START_POINT_DATE_KEY)
                val endPointDateFromBundle = bundle.getString(END_POINT_DATE_KEY)
                btSelectStartPointFragOBDateFrame.text = startPointDateFromBundle
                btSelectEndPointFragOBDateFrame.text = endPointDateFromBundle

                if (startPointDateFromBundle != resources.getString(R.string.text_button_select_start_point)) {
                    startPointDate = LocalDate.parse(startPointDateFromBundle, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
                }

                if (endPointDateFromBundle != resources.getString(R.string.text_button_select_end_point)) {
                    endPointDate = LocalDate.parse(endPointDateFromBundle, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
                }

                if (startPointDateFromBundle != resources.getString(R.string.text_button_select_start_point) &&
                    endPointDateFromBundle != resources.getString(R.string.text_button_select_end_point)) {
                    tvNextFragOBDateFrame.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.apply {
            btSelectStartPointFragOBDateFrame.setOnClickListener {
                showMaterialDatePickerDialog (false) { materialDatePicker ->
                    materialDatePicker.addOnPositiveButtonClickListener { selection ->
                        startPointDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                        btSelectStartPointFragOBDateFrame.text = startPointDate?.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
                    }
                    materialDatePicker.show(childFragmentManager, "tag")
                }
            }

            btSelectEndPointFragOBDateFrame.setOnClickListener {
                showMaterialDatePickerDialog (true) { materialDatePicker ->
                    materialDatePicker.addOnPositiveButtonClickListener { selection ->
                        endPointDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                        if ((endPointDate!!.year - startPointDate!!.year) > 1) {
                            showErrorMessage(resources.getString(R.string.error_message_onboarding_dateframe_year_gap_not_allowed), binding)
                        } else {
                            btSelectEndPointFragOBDateFrame.text = endPointDate!!.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
                            tvNextFragOBDateFrame.visibility = View.VISIBLE
                        }
                    }
                    materialDatePicker.show(childFragmentManager, "tag")
                }
            }

            tvNextFragOBDateFrame.setOnClickListener {
                if (checkDateFrames()) {
                    val startPointDateString = startPointDate?.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
                    val endPointDateString = endPointDate!!.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
                    findNavController().navigate(OnBoardingDateFrameFragmentDirections.actionOnBoardingDateFrameFragmentToOnBoardingBudgetFragment(startPointDateString!!, endPointDateString!!))
                } else {
                    showErrorMessage(resources.getString(R.string.error_message_onboarding_dateframe_unappropriate_dateframe), binding)
                    tvNextFragOBDateFrame.visibility = View.INVISIBLE
                    btSelectEndPointFragOBDateFrame.text = getString(R.string.text_button_select_end_point)
                }
            }
        }


        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(START_POINT_DATE_KEY, binding.btSelectStartPointFragOBDateFrame.text.toString())
        outState.putString(END_POINT_DATE_KEY, binding.btSelectEndPointFragOBDateFrame.text.toString())

    }

    private fun showMaterialDatePickerDialog (isEndPointSelection: Boolean, callback: (MaterialDatePicker<Long>) -> Unit) {
        val materialDatePicker: MaterialDatePicker<Long>

        if (!isEndPointSelection) {
            val dateValidator = DateValidatorPointBackward.now()
            val calendarConstraints = CalendarConstraints.Builder()
                .setValidator(dateValidator)

            materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(resources.getString(R.string.date_picker_title_text_start_point))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(calendarConstraints.build())
                .build()
        } else {
            val dateValidator = DateValidatorPointForward.from(startPointDate!!.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            val calendarConstraints = CalendarConstraints.Builder()
                .setValidator(dateValidator)

            materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(resources.getString(R.string.date_picker_title_text_end_point))
                .setCalendarConstraints(calendarConstraints.build())
                .build()
        }

        callback (materialDatePicker)
    }

    private fun checkDateFrames (): Boolean {
        if (startPointDate!!.year <= endPointDate!!.year) {
            return true
        } else if (startPointDate!!.month <= endPointDate!!.month) {
            return true
        }
        return false
    }
}