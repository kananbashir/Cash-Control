package com.example.cashcontrol.ui.fragment.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.databinding.FragmentOnBoardingSavingsBinding
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.constant.UIStateConstant.SAVINGS_AMOUNT_INPUT_KEY

class OnBoardingSavingsFragment : Fragment() {
    private lateinit var binding: FragmentOnBoardingSavingsBinding
    private lateinit var args: OnBoardingSavingsFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = OnBoardingSavingsFragmentArgs.fromBundle(requireArguments())
        binding = FragmentOnBoardingSavingsBinding.inflate(layoutInflater)

        if (savedInstanceState != null) {
            binding.etSaveFragOBSavings.setText(savedInstanceState.getString(SAVINGS_AMOUNT_INPUT_KEY))
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
            tvNextFragOBBudget.setOnClickListener {
                val saving = binding.etSaveFragOBSavings.text.toString()

                if (saving.isNotEmpty()) {
                    if (saving.toDouble() < args.budget.toDouble()) {
                        findNavController().navigate(OnBoardingSavingsFragmentDirections.actionOnBoardingSavingsFragmentToOnBoardingFinishFragment(
                            args.startPointDate,
                            args.endPointDate,
                            args.budget,
                            args.currency,
                            saving
                        ))
                    } else {
                        showErrorMessage(resources.getString(R.string.error_message_onboarding_savings_exceeded_budget), binding)
                    }
                } else {
                    findNavController().navigate(OnBoardingSavingsFragmentDirections.actionOnBoardingSavingsFragmentToOnBoardingFinishFragment(
                        args.startPointDate,
                        args.endPointDate,
                        args.budget,
                        args.currency,
                        "0"
                    ))
                }
            }

            tvPreviousFragOBBudget.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(SAVINGS_AMOUNT_INPUT_KEY, binding.etSaveFragOBSavings.text.toString())

    }
}