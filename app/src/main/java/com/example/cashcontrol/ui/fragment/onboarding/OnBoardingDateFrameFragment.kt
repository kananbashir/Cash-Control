package com.example.cashcontrol.ui.fragment.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.databinding.FragmentOnBoardingDateFrameBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.UIStateConstant.END_POINT_DATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.START_POINT_DATE_KEY
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
                    startPointDate = LocalDate.parse(startPointDateFromBundle, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
                }

                if (endPointDateFromBundle != resources.getString(R.string.text_button_select_end_point)) {
                    endPointDate = LocalDate.parse(endPointDateFromBundle, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
                }

                if (startPointDateFromBundle != resources.getString(R.string.text_button_select_start_point) &&
                    endPointDateFromBundle != resources.getString(R.string.text_button_select_end_point)) {
                    tvNextFragOBDateFrame.visibility = View.VISIBLE
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

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
                        btSelectStartPointFragOBDateFrame.text = startPointDate?.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
                    }
                    materialDatePicker.show(childFragmentManager, "tag")
                }
            }

            btSelectEndPointFragOBDateFrame.setOnClickListener {
                showMaterialDatePickerDialog (true) { materialDatePicker ->
                    materialDatePicker.addOnPositiveButtonClickListener { selection ->
                        endPointDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                        if ((endPointDate!!.year - startPointDate!!.year) > 1) {
                            showErrorMessage("Year gap is not allowed..")
                        } else {
                            btSelectEndPointFragOBDateFrame.text = endPointDate!!.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
                            tvNextFragOBDateFrame.visibility = View.VISIBLE
                        }
                    }
                    materialDatePicker.show(childFragmentManager, "tag")
                }
            }

            tvNextFragOBDateFrame.setOnClickListener {
                if (checkDateFrames()) {
                    val startPointDateString = startPointDate?.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
                    val endPointDateString = endPointDate!!.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
                    findNavController().navigate(OnBoardingDateFrameFragmentDirections.actionOnBoardingDateFrameFragmentToOnBoardingBudgetFragment(startPointDateString!!, endPointDateString!!))
                } else {
                    showErrorMessage("The date frame is not appropriate! The date for the start point should be less than end point.")
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
        var materialDatePicker: MaterialDatePicker<Long>? = null

        if (!isEndPointSelection) {
            val dateValidator = DateValidatorPointBackward.now()
            val calendarConstraints = CalendarConstraints.Builder()
                .setValidator(dateValidator)

            materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select start point")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(calendarConstraints.build())
                .build()
        } else {
            val dateValidator = DateValidatorPointForward.from(startPointDate!!.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            val calendarConstraints = CalendarConstraints.Builder()
                .setValidator(dateValidator)

            materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select end point")
                .setCalendarConstraints(calendarConstraints.build())
                .build()
        }

        callback (materialDatePicker)
    }

    private fun checkDateFrames (): Boolean {
        if (startPointDate!!.month <= endPointDate!!.month) {
            if (startPointDate!!.year <= endPointDate!!.year) {
                return true
            }
        }
        return false
    }

    private fun showErrorMessage (message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_SHORT)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
            .setBackgroundTint(resources.getColor(R.color.bittersweet_red, null))
            .show()
    }
}