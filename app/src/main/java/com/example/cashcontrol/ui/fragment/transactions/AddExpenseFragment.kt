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
import com.example.cashcontrol.data.db.entity.expense.Expense
import com.example.cashcontrol.databinding.FragmentAddExpenseBinding
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

class AddExpenseFragment : Fragment() {
    private lateinit var binding: FragmentAddExpenseBinding
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userViewModel: UserViewModel
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
        binding = FragmentAddExpenseBinding.inflate(layoutInflater)

        val currencyArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies)
        )
        binding.actvCurrencyFragAddExpense.setAdapter(currencyArrayAdapter)

        setCacheCategoryDropDownList(binding.etExpenseCategoryFragAddExpense)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

        savedInstanceState?.let { bundle ->
            binding.apply {
                etAmountFragAddExpense.setText(bundle.getString(TRANSACTION_AMOUNT_KEY))
                actvCurrencyFragAddExpense.setSelection(bundle.getInt(TRANSACTION_CURRENCY_KEY))
                rgDateSelectionFragAddExpense.check(bundle.getInt(RADIO_GROUP_DATE_SELECT_STATE_KEY))

                if (bundle.getString(CUSTOM_TRANSACTION_DATE_KEY)?.isNotEmpty()!!) {
                    tvHyphenFragAddExpense.visibility = View.VISIBLE
                    tvSelectedDateFragAddExpense.visibility = View.VISIBLE
                    tvSelectedDateFragAddExpense.text = bundle.getString(CUSTOM_TRANSACTION_DATE_KEY)
                }

                etExpenseCategoryFragAddExpense.setText(bundle.getString(INITIAL_TRANSACTION_CATEGORY_KEY))
                etDescriptionFragAddExpense.setText(bundle.getString(TRANSACTION_DESCRIPTION_KEY))
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
                                TransactionUtil.addNewItem(additionalExpenseCategoryList, parentContainerLayoutFragAddExpense, requireContext())
                            }
                        }
                    }
                }
            }

            parentContainerLayoutFragAddExpense.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    val etExpenseCategory = child?.findViewById<AutoCompleteTextView>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)
                    setCacheCategoryDropDownList(etExpenseCategory!!)
                    updateAddMoreButton()
                }

                override fun onChildViewRemoved(parent: View?, child: View?) {
                    updateAddMoreButton()
                }
            })

            btAddMoreFragAddExpense.setOnClickListener { TransactionUtil.addNewItem(parentContainerLayoutFragAddExpense, requireContext()) }
            ivReturnBackFragAddExpense.setOnClickListener { findNavController().popBackStack() }

            rgDateSelectionFragAddExpense.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    rbTodayFragAddExpense.id -> {
                        btSelectDateFragAddExpense.visibility = View.GONE
                        tvSelectedDateFragAddExpense.visibility = View.GONE
                        tvHyphenFragAddExpense.visibility = View.GONE
                    }

                    rbCustomDateFragAddExpense.id -> {
                        btSelectDateFragAddExpense.visibility = View.VISIBLE
                    }
                }
            }

            btSelectDateFragAddExpense.setOnClickListener {
                unfinishedAndOnlineDateFrame?.let { unfinishedDateFrame ->
                    showMaterialDatePickerDialog {
                        it.addOnPositiveButtonClickListener { selection ->
                            val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()

                            val startPointDate = LocalDate.parse(
                                unfinishedDateFrame.startPointDate,
                                DateTimeFormatter.ISO_LOCAL_DATE
                            )

                            if (selectedDate > LocalDate.now()) {
                                showErrorMessage(resources.getString(R.string.error_message_add_expense_future_date_selected), binding)
                            } else if (selectedDate < startPointDate) {
                                showErrorMessage(
                                    resources.getString(
                                        R.string.error_message_add_expense_earlier_date_selected,
                                        unfinishedDateFrame.startPointDate
                                    ),
                                    binding
                                )
                            } else {
                                tvHyphenFragAddExpense.visibility = View.VISIBLE
                                tvSelectedDateFragAddExpense.visibility = View.VISIBLE
                                tvSelectedDateFragAddExpense.text =
                                    selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE).convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                            }

                        }
                        it.show(childFragmentManager, "date_selection")
                    }
                }

            }

            btAddExpenseFragAddExpense.setOnClickListener {
                if (checkForEmptyColumns()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            unfinishedAndOnlineDateFrame?.let { unfinishedDateFrame ->
                                dateLimitViewModel.getCurrentDateLimitByDateFrame(unfinishedDateFrame.dateFrameId!!)
                                    ?.let { currentDateLimit ->
                                        buttonLoadingState(true)
                                        delay(500) // JUST TO SIMULATE LOADING..

                                        val selectedDateLimit = when {
                                            rbCustomDateFragAddExpense.isChecked -> {
                                                dateLimitViewModel.getDateLimitOfDateFrameByDate(
                                                    unfinishedDateFrame.dateFrameId!!,
                                                    tvSelectedDateFragAddExpense.text.toString().convertDateToIso8601From(DATE_LIMIT_DATE_PATTERN)
                                                )
                                            }

                                            else -> {
                                                currentDateLimit
                                            }
                                        }

                                        selectedDateLimit?.let {
                                            if (TransactionUtil.isSingleTransaction(parentContainerLayoutFragAddExpense)) {
                                                transactionViewModel.upsertTransaction(
                                                    Expense(
                                                        etAmountFragAddExpense.text.toString().toDouble(),
                                                        actvCurrencyFragAddExpense.text.toString(),
                                                        etExpenseCategoryFragAddExpense.text.toString(),
                                                        mutableListOf(),
                                                        etDescriptionFragAddExpense.text.toString(),
                                                        selectedDateLimit.dateLimitId!!,
                                                        selectedDateLimit.date,
                                                        unfinishedDateFrame.dateFrameId!!
                                                    )
                                                )
                                                userViewModel.cacheNewExpenseCategory(etExpenseCategoryFragAddExpense.text.toString())
                                            } else {
                                                val expenseCategoryList = TransactionUtil.getAllTransactionCategoryList(
                                                    parentContainerLayoutFragAddExpense,
                                                    etExpenseCategoryFragAddExpense.text.toString()
                                                )
                                                transactionViewModel.upsertTransaction(
                                                    Expense(
                                                        etAmountFragAddExpense.text.toString().toDouble(),
                                                        actvCurrencyFragAddExpense.text.toString(),
                                                        "",
                                                        expenseCategoryList,
                                                        etDescriptionFragAddExpense.text.toString(),
                                                        selectedDateLimit.dateLimitId!!,
                                                        selectedDateLimit.date,
                                                        unfinishedDateFrame.dateFrameId!!
                                                    )
                                                )
                                                userViewModel.cacheNewExpenseCategory(expenseCategoryList)
                                            }

                                            dateFrameViewModel.updateTotalExpenseAmountOf(unfinishedDateFrame)
                                            checkForLimitExceededValue(selectedDateLimit)

                                            if (selectedDateLimit.date != currentDateLimit.date) {
                                                delay(200)
                                                reCalculateSubsequentExpenseLimitsFrom(selectedDateLimit)
                                            }

                                            delay(300) // JUST TO SIMULATE LOADING..
                                            findNavController().popBackStack()
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private suspend fun reCalculateSubsequentExpenseLimitsFrom(selectedDateLimit: DateLimit) {
        getUnfinishedDateFrame()?.let { unfinishedDateFrame ->
            dateFrameViewModel.getDateFrameWithDateLimits(unfinishedDateFrame.dateFrameId!!)?.let { dateFrameWithDateLimits ->
                dateFrameViewModel.getDateFrameWithTransactions(unfinishedDateFrame.dateFrameId!!)?.let { dateFrameWithTransactions ->
                    val updatedDateLimits = DateLimitCalculator()
                        .setDateFrame(unfinishedDateFrame)
                        .reCalculateSubsequentExpenseLimits(
                        selectedDateLimit,
                        false,
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

    private suspend fun checkForLimitExceededValue(selectedDateLimit: DateLimit) {
        dateLimitViewModel.getDateLimitWithTransactions(selectedDateLimit.dateLimitId!!)?.let {
            val allExpenses = DateLimitCalculator.Util.extractExpensesFromTransactionList(it.transactions)
            val sumOfExpenses = DateLimitCalculator.Util.getSumOfExpensesOf(selectedDateLimit, allExpenses)

            if (sumOfExpenses > selectedDateLimit.expenseLimit) {
                dateLimitViewModel.updateLimitExceededValueOf(
                    selectedDateLimit,
                    DateLimitCalculator.Util.calculateLimitExceededValueOf(selectedDateLimit, sumOfExpenses)
                )
            } else if (selectedDateLimit.limitExceededValue != 0.0){
                dateLimitViewModel.updateLimitExceededValueOf(
                    selectedDateLimit,
                    0.0
                )
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

    private fun updateAddMoreButton() {
        binding.btAddMoreFragAddExpense.isClickable = binding.parentContainerLayoutFragAddExpense.childCount != 10
    }

    private fun checkForEmptyColumns(): Boolean {
        val isReady: Boolean
        val expenseAmount = binding.etAmountFragAddExpense.text.toString()
        val currency = binding.actvCurrencyFragAddExpense.text.toString()

        val initialExpenseCategory = binding.etExpenseCategoryFragAddExpense.text.toString()

        when {
            expenseAmount.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_add_expense_no_amount_added), binding)
                isReady = false
            }

            currency.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_add_expense_no_currency_selected), binding)
                isReady = false
            }

            initialExpenseCategory.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_add_expense_no_expense_category_added), binding)
                isReady = false
            }

            !TransactionUtil.isAdditionalExpenseCategoryListEmpty(binding.parentContainerLayoutFragAddExpense) -> {
                showErrorMessage(resources.getString(R.string.error_message_add_expense_empty_additional_category_column), binding)
                isReady = false
            }

            else -> {
                isReady = if (binding.rgDateSelectionFragAddExpense.checkedRadioButtonId == binding.rbCustomDateFragAddExpense.id) {
                    if (binding.tvSelectedDateFragAddExpense.text.isEmpty()) {
                        showErrorMessage(resources.getString(R.string.error_message_add_expense_no_date_selected), binding)
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

    private fun setCacheCategoryDropDownList(autoCompleteTextView: AutoCompleteTextView) {
        if (userViewModel.cachedExpenseCategories.isNotEmpty()) {
            val cachedCategoriesListAdapter = ArrayAdapter(
                requireContext(),
                R.layout.item_layout_cached_categories, R.id.tvCategoryName, userViewModel.cachedExpenseCategories.toTypedArray()
            )
            autoCompleteTextView.setAdapter(cachedCategoriesListAdapter)
        }
    }

    private fun showMaterialDatePickerDialog(callback: (MaterialDatePicker<Long>) -> Unit) {
        val materialDatePicker: MaterialDatePicker<Long> = MaterialDatePicker.Builder.datePicker()
            .setTitleText(resources.getString(R.string.date_picker_title_text_expense_date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        callback(materialDatePicker)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.apply {
            outState.putString(TRANSACTION_AMOUNT_KEY, etAmountFragAddExpense.text.toString())
            outState.putString(INITIAL_TRANSACTION_CATEGORY_KEY, etExpenseCategoryFragAddExpense.text.toString())
            outState.putString(TRANSACTION_DESCRIPTION_KEY, etDescriptionFragAddExpense.text.toString())

            if (tvSelectedDateFragAddExpense.text.toString().isNotEmpty()) {
                outState.putString(CUSTOM_TRANSACTION_DATE_KEY, tvSelectedDateFragAddExpense.text.toString())
            } else {
                outState.putString(CUSTOM_TRANSACTION_DATE_KEY, "")
            }

            if (parentContainerLayoutFragAddExpense.childCount > 1) {
                outState.putStringArrayList(
                    ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY,
                    ArrayList(TransactionUtil.getAdditionalTransactionCategoryList(parentContainerLayoutFragAddExpense))
                )
            } else {
                outState.putStringArrayList(ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY, ArrayList(listOf()))
            }

            actvCurrencyFragAddExpense.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    outState.putInt(TRANSACTION_CURRENCY_KEY, position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            rgDateSelectionFragAddExpense.setOnCheckedChangeListener { _, checkedId ->
                outState.putInt(RADIO_GROUP_DATE_SELECT_STATE_KEY, checkedId)
            }
        }
    }

    private fun buttonLoadingState (isLoading: Boolean) {
        binding.apply {
            when (isLoading) {
                true -> {
                    btAddExpenseFragAddExpense.startAnimation(shrinkButtonAnim)
                    ltLoadingFragAddExpense.visibility = View.VISIBLE
                    btAddExpenseFragAddExpense.isClickable = false
                    btAddExpenseFragAddExpense.visibility = View.INVISIBLE
                }
                else -> {
                    btAddExpenseFragAddExpense.startAnimation(expandButtonAnim)
                    ltLoadingFragAddExpense.visibility = View.INVISIBLE
                    btAddExpenseFragAddExpense.isClickable = true
                    btAddExpenseFragAddExpense.visibility = View.VISIBLE
                }
            }
        }
    }
}