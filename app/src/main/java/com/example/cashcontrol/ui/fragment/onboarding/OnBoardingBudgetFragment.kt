package com.example.cashcontrol.ui.fragment.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.databinding.FragmentOnBoardingBudgetBinding
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.constant.UIStateConstant.BUDGET_AMOUNT_INPUT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.BUDGET_CURRENCY_INPUT_KEY

class OnBoardingBudgetFragment : Fragment() {
    private lateinit var binding: FragmentOnBoardingBudgetBinding
    private lateinit var args: OnBoardingBudgetFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = OnBoardingBudgetFragmentArgs.fromBundle(requireArguments())
        binding = FragmentOnBoardingBudgetBinding.inflate(layoutInflater)

        val arrayAdapter = ArrayAdapter(requireContext(),
        R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies))
        binding.actvCurrencyFragOBBudget.setAdapter(arrayAdapter)

        if (savedInstanceState != null) {
            binding.apply {
                etBudgetFragOBBudget.setText(savedInstanceState.getString(BUDGET_AMOUNT_INPUT_KEY))
                actvCurrencyFragOBBudget.setSelection(savedInstanceState.getInt(BUDGET_CURRENCY_INPUT_KEY))
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
            tvNextFragOBBudget.setOnClickListener {
                val budget = etBudgetFragOBBudget.text.toString()
                val currency: String = actvCurrencyFragOBBudget.text.toString()

                if (budget.isNotEmpty()) {
                    if (currency.isNotEmpty()) {
                        if (budget.length >= 3) {
                            findNavController().navigate(OnBoardingBudgetFragmentDirections.actionOnBoardingBudgetFragmentToOnBoardingSavingsFragment(
                                args.startPointDate,
                                args.endPointDate,
                                budget,
                                currency
                            ))
                        } else {
                            showErrorMessage(resources.getString(R.string.error_message_onboarding_budget_allowable_min_budget_value), binding)
                        }
                    } else {
                        showErrorMessage(resources.getString(R.string.error_message_onboarding_budget_currency_not_selected), binding)
                    }
                } else {
                    showErrorMessage(resources.getString(R.string.error_message_onboarding_budget_no_budget_info), binding)
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

        outState.putString(BUDGET_AMOUNT_INPUT_KEY, binding.etBudgetFragOBBudget.text.toString())
        binding.actvCurrencyFragOBBudget.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                outState.putInt(BUDGET_CURRENCY_INPUT_KEY, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }
}