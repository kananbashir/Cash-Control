package com.example.cashcontrol.ui.fragment.transactions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.data.DateLimitCalculator
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.income.Income
import com.example.cashcontrol.databinding.FragmentAddIncomeBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.TransactionUtil
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.CUSTOM_TRANSACTION_DATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.INITIAL_TRANSACTION_CATEGORY_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.RADIO_GROUP_DATE_SELECT_STATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TRANSACTION_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TRANSACTION_CURRENCY_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TRANSACTION_DESCRIPTION_KEY
import com.example.cashcontrol.util.extension.convertDateFromIso8601To
import com.example.cashcontrol.util.extension.convertDateToIso8601From
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddIncomeFragment : Fragment() {
    private lateinit var binding: FragmentAddIncomeBinding
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private val shrinkButtonAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button_anim) }
    private val expandButtonAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.expand_button_anim) }
    private var unfinishedAndOnlineDateFrame: DateFrame? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        dateFrameViewModel = ViewModelProvider(requireActivity())[DateFrameViewModel::class.java]
        dateLimitViewModel = ViewModelProvider(requireActivity())[DateLimitViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        binding = FragmentAddIncomeBinding.inflate(layoutInflater)

        val currencyArrayAdapter = ArrayAdapter(requireContext(), R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies))
        binding.actvCurrencyFragAddIncome.setAdapter(currencyArrayAdapter)

        setCacheSourceDropDownList(binding.etIncomeSourceFragAddIncome)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

        savedInstanceState?.let { bundle ->
            binding.apply {
                etAmountFragAddIncome.setText(bundle.getString(TRANSACTION_AMOUNT_KEY))
                actvCurrencyFragAddIncome.setSelection(bundle.getInt(TRANSACTION_CURRENCY_KEY))
                rgDateSelectionFragAddIncome.check(bundle.getInt(RADIO_GROUP_DATE_SELECT_STATE_KEY))

                if (bundle.getString(CUSTOM_TRANSACTION_DATE_KEY)?.isNotEmpty()!!) {
                    tvHyphenFragHome.visibility = View.VISIBLE
                    tvSelectedDateFragAddIncome.visibility = View.VISIBLE
                    tvSelectedDateFragAddIncome.text = bundle.getString(CUSTOM_TRANSACTION_DATE_KEY)
                }

                etIncomeSourceFragAddIncome.setText(bundle.getString(INITIAL_TRANSACTION_CATEGORY_KEY))
                etDescriptionFragAddIncome.setText(bundle.getString(TRANSACTION_DESCRIPTION_KEY))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding.apply {

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    getUnfinishedDateFrame()?.let {
                        unfinishedAndOnlineDateFrame = it

                        savedInstanceState?.let { bundle ->
                            val additionalExpenseCategoryList = bundle.getStringArrayList(ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY)?.toList()
                            if (additionalExpenseCategoryList!!.isNotEmpty()) {
                                TransactionUtil.addNewItem(additionalExpenseCategoryList, parentContainerLayoutFragAddIncome, requireContext())
                            }
                        }
                    }
                }
            }

            parentContainerLayoutFragAddIncome.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    val etIncomeCategory = child?.findViewById<AutoCompleteTextView>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)
                    setCacheSourceDropDownList(etIncomeCategory!!)
                    updateAddMoreButton()
                }

                override fun onChildViewRemoved(parent: View?, child: View?) {
                    updateAddMoreButton()
                }
            })

            btAddMoreFragAddIncome.setOnClickListener { TransactionUtil.addNewItem(parentContainerLayoutFragAddIncome, requireContext()) }
            ivReturnBackFragAddIncome.setOnClickListener { findNavController().popBackStack() }

            rgDateSelectionFragAddIncome.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    rbTodayFragAddIncome.id -> {
                        btSelectDateFragAddIncome.visibility = View.GONE
                        tvHyphenFragHome.visibility = View.GONE
                        tvSelectedDateFragAddIncome.visibility = View.GONE
                    }

                    rbCustomDateFragAddIncome.id -> {
                        btSelectDateFragAddIncome.visibility = View.VISIBLE
                    }
                }
            }

            btSelectDateFragAddIncome.setOnClickListener {
                unfinishedAndOnlineDateFrame?.let { unfinishedDateFrame ->
                    showMaterialDatePickerDialog {
                        it.addOnPositiveButtonClickListener { selection ->
                            val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()

                            val startPointDate = LocalDate.parse(
                                unfinishedDateFrame.startPointDate,
                                DateTimeFormatter.ISO_LOCAL_DATE
                            )

                            if (selectedDate > LocalDate.now()) {
                                showErrorMessage(
                                    resources.getString(R.string.error_message_add_income_future_date_selected),
                                    binding
                                )
                            } else if (selectedDate < startPointDate) {
                                showErrorMessage(
                                    resources.getString(
                                        R.string.error_message_add_income_earlier_date_selected,
                                        unfinishedDateFrame.startPointDate
                                    ), binding
                                )
                            } else {
                                tvHyphenFragHome.visibility = View.VISIBLE
                                tvSelectedDateFragAddIncome.visibility = View.VISIBLE
                                tvSelectedDateFragAddIncome.text =
                                    selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE).convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                            }
                        }
                        it.show(childFragmentManager, "date_selection")
                    }
                }
            }

            btAddIncomeFragAddIncome.setOnClickListener {
                if (checkForEmptyColumns()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            unfinishedAndOnlineDateFrame?.let { unfinishedDateFrame ->
                                dateLimitViewModel.getCurrentDateLimitByDateFrame(unfinishedDateFrame.dateFrameId!!)?.let { currentDateLimit ->
                                    buttonLoadingState(true)
                                    delay(500)// JUST TO SIMULATE LOADING..

                                    val selectedDateLimit = when {
                                        rbCustomDateFragAddIncome.isChecked -> {
                                            dateLimitViewModel.getDateLimitOfDateFrameByDate(
                                                unfinishedDateFrame.dateFrameId!!,
                                                tvSelectedDateFragAddIncome.text.toString().convertDateToIso8601From(DATE_LIMIT_DATE_PATTERN)
                                            )
                                        }

                                        else -> {
                                            currentDateLimit
                                        }
                                    }

                                    if (TransactionUtil.isSingleTransaction(parentContainerLayoutFragAddIncome)) {
                                        transactionViewModel.upsertTransaction(
                                            Income(
                                                etAmountFragAddIncome.text.toString().toDouble(),
                                                actvCurrencyFragAddIncome.text.toString(),
                                                etIncomeSourceFragAddIncome.text.toString(),
                                                mutableListOf(),
                                                etDescriptionFragAddIncome.text.toString(),
                                                selectedDateLimit?.dateLimitId!!,
                                                selectedDateLimit.date,
                                                unfinishedDateFrame.dateFrameId!!
                                            )
                                        )
                                        userViewModel.cacheNewIncomeSource(etIncomeSourceFragAddIncome.text.toString())
                                    } else {
                                        val incomeSourceList = TransactionUtil.getAllTransactionCategoryList(
                                            parentContainerLayoutFragAddIncome,
                                            etIncomeSourceFragAddIncome.text.toString()
                                        )
                                        transactionViewModel.upsertTransaction(
                                            Income(
                                                etAmountFragAddIncome.text.toString().toDouble(),
                                                actvCurrencyFragAddIncome.text.toString(),
                                                "",
                                                incomeSourceList,
                                                etDescriptionFragAddIncome.text.toString(),
                                                selectedDateLimit?.dateLimitId!!,
                                                selectedDateLimit.date,
                                                unfinishedDateFrame.dateFrameId!!
                                            )
                                        )
                                        userViewModel.cacheNewIncomeSource(incomeSourceList)
                                    }
                                    dateFrameViewModel.updateTotalIncomeAmountOf(unfinishedDateFrame)
                                    reCalculateSubsequentExpenseLimits(selectedDateLimit)

                                    delay(300) // JUST TO SIMULATE LOADING..
                                    findNavController().popBackStack()
                                }
                            }
                        }
                    }
                }
            }

        }

        return binding.root
    }

    private suspend fun reCalculateSubsequentExpenseLimits(dateLimit: DateLimit) {
        getUnfinishedDateFrame()?.let { unfinishedDateFrame ->
            dateFrameViewModel.getDateFrameWithDateLimits(unfinishedDateFrame.dateFrameId!!)?.let { dateFrameWithDateLimits ->
                dateFrameViewModel.getDateFrameWithTransactions(unfinishedDateFrame.dateFrameId!!)?.let { dateFrameWithTransactions ->
                    val updatedDateLimits = DateLimitCalculator()
                        .setDateFrame(unfinishedDateFrame)
                        .reCalculateSubsequentExpenseLimits(
                            dateLimit,
                            true,
                            dateFrameWithDateLimits.dateLimits,
                            dateFrameWithTransactions
                        )
                    dateLimitViewModel.upsertAllDateLimits(*updatedDateLimits.toTypedArray())
                    transactionViewModel.newExpense = true
                    transactionViewModel.newIncome = true
                }
            }
        }
    }

    private suspend fun getUnfinishedDateFrame(): DateFrame? {
        userViewModel.getOnlineUser()?.let { onlineUser ->
            profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile.profileId!!)?.let { unfinishedDateFrame ->
                    return unfinishedDateFrame
                }
            }
        }

        return null
    }

    private fun setCacheSourceDropDownList(autoCompleteTextView: AutoCompleteTextView) {
        if (userViewModel.cachedIncomeSources.isNotEmpty()) {
            val cachedCategoriesListAdapter = ArrayAdapter(
                requireContext(),
                R.layout.item_layout_cached_categories, R.id.tvCategoryName, userViewModel.cachedIncomeSources.toTypedArray()
            )
            autoCompleteTextView.setAdapter(cachedCategoriesListAdapter)
        }
    }

    private fun updateAddMoreButton() {
        binding.btAddMoreFragAddIncome.isClickable = binding.parentContainerLayoutFragAddIncome.childCount != 10
    }

    private fun checkForEmptyColumns(): Boolean {
        val isReady: Boolean
        val expenseAmount = binding.etAmountFragAddIncome.text.toString()
        val currency = binding.actvCurrencyFragAddIncome.text.toString()

        val initialExpenseCategory = binding.etIncomeSourceFragAddIncome.text.toString()

        when {
            expenseAmount.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_add_income_no_amount_added), binding)
                isReady = false
            }

            currency.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_add_income_no_currency_selected), binding)
                isReady = false
            }

            initialExpenseCategory.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_add_income_no_income_source_added), binding)
                isReady = false
            }

            !TransactionUtil.isAdditionalExpenseCategoryListEmpty(binding.parentContainerLayoutFragAddIncome) -> {
                showErrorMessage(resources.getString(R.string.error_message_add_income_empty_additional_category_column), binding)
                isReady = false
            }

            else -> {
                isReady = if (binding.rgDateSelectionFragAddIncome.checkedRadioButtonId == binding.rbCustomDateFragAddIncome.id) {
                    if (binding.tvSelectedDateFragAddIncome.text.isEmpty()) {
                        showErrorMessage(resources.getString(R.string.error_message_add_income_no_date_selected), binding)
                        false
                    } else {
                        true
                    }
                } else {
                    true
                }
            }
        }

        return isReady
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        binding.apply {
            outState.putString(TRANSACTION_AMOUNT_KEY, etAmountFragAddIncome.text.toString())
            outState.putString(INITIAL_TRANSACTION_CATEGORY_KEY, etIncomeSourceFragAddIncome.text.toString())
            outState.putString(TRANSACTION_DESCRIPTION_KEY, etDescriptionFragAddIncome.text.toString())

            if (tvSelectedDateFragAddIncome.text.toString().isNotEmpty()) {
                outState.putString(CUSTOM_TRANSACTION_DATE_KEY, tvSelectedDateFragAddIncome.text.toString())
            } else {
                outState.putString(CUSTOM_TRANSACTION_DATE_KEY, "")
            }

            if (parentContainerLayoutFragAddIncome.childCount > 1) {
                outState.putStringArrayList(
                    ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY,
                    ArrayList(TransactionUtil.getAdditionalTransactionCategoryList(parentContainerLayoutFragAddIncome))
                )
            } else {
                outState.putStringArrayList(ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY, ArrayList(listOf()))
            }

            actvCurrencyFragAddIncome.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    outState.putInt(TRANSACTION_CURRENCY_KEY, position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            rgDateSelectionFragAddIncome.setOnCheckedChangeListener { _, checkedId ->
                outState.putInt(RADIO_GROUP_DATE_SELECT_STATE_KEY, checkedId)
            }
        }

    }

    private fun showMaterialDatePickerDialog(callback: (MaterialDatePicker<Long>) -> Unit) {
        val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(resources.getString(R.string.date_picker_title_text_income_date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        callback(materialDatePicker)
    }

    private fun buttonLoadingState (isLoading: Boolean) {
        binding.apply {
            when (isLoading) {
                true -> {
                    btAddIncomeFragAddIncome.startAnimation(shrinkButtonAnim)
                    ltLoadingFragAddIncome.visibility = View.VISIBLE
                    btAddIncomeFragAddIncome.isClickable = false
                    btAddIncomeFragAddIncome.visibility = View.INVISIBLE
                }
                else -> {
                    btAddIncomeFragAddIncome.startAnimation(expandButtonAnim)
                    ltLoadingFragAddIncome.visibility = View.INVISIBLE
                    btAddIncomeFragAddIncome.isClickable = true
                    btAddIncomeFragAddIncome.visibility = View.VISIBLE
                }
            }
        }
    }
}