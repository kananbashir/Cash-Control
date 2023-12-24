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
import com.example.cashcontrol.databinding.FragmentEditExpenseBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.TransactionUtil
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.UIStateConstant
import com.example.cashcontrol.util.extension.convertDateFromIso8601To
import com.example.cashcontrol.util.extension.convertDateToIso8601From
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class EditExpenseFragment : Fragment() {
    private lateinit var binding: FragmentEditExpenseBinding
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private val shrinkButtonAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button_anim) }
    private val expandButtonAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.expand_button_anim) }
    private var unfinishedAndOnlineDateFrame: DateFrame? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateLimitViewModel = ViewModelProvider(requireActivity())[DateLimitViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        dateFrameViewModel = ViewModelProvider(requireActivity())[DateFrameViewModel::class.java]
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        binding = FragmentEditExpenseBinding.inflate(layoutInflater)

        val currencyArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies)
        )
        binding.actvCurrencyFragEditExpense.setAdapter(currencyArrayAdapter)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                materialAlertDialog.setMessage(resources.getString(R.string.alert_message_edit_expense_discard))
                    .setPositiveButton(resources.getString(R.string.alert_dialog_positive_yes)) { _, _ ->
                        findNavController().popBackStack()
                    }
                    .setNegativeButton(resources.getString(R.string.alert_dialog_negative_no)) { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }
                    .show()
            }
        })

        savedInstanceState?.let { bundle ->
            binding.apply {
                etAmountFragEditExpense.setText(bundle.getString(UIStateConstant.TRANSACTION_AMOUNT_KEY))
                actvCurrencyFragEditExpense.setSelection(bundle.getInt(UIStateConstant.TRANSACTION_CURRENCY_KEY))
                tvSelectedDateFragEditExpense.text = bundle.getString(UIStateConstant.CUSTOM_TRANSACTION_DATE_KEY)

                etExpenseCategoryFragEditExpense.setText(bundle.getString(UIStateConstant.INITIAL_TRANSACTION_CATEGORY_KEY))
                tvDescriptionFragEditExpense.text = bundle.getString(UIStateConstant.TRANSACTION_DESCRIPTION_KEY)
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
                            dateFrameViewModel.selectedTransaction?.let { transaction ->
                            if (transaction.transactionCategory.isEmpty()) {
                                etExpenseCategoryFragEditExpense.setText(transaction.transactionCategories[0])
                                TransactionUtil.inflateTransactionCategories(
                                    transaction.transactionCategories,
                                    parentContainerLayoutFragEditExpense,
                                    requireContext()
                                )
                            } else {
                                etExpenseCategoryFragEditExpense.setText(transaction.transactionCategory)
                            }

                            etAmountFragEditExpense.setText(transaction.transactionAmount.toString())
                            etDescriptionFragEditExpense.setText(transaction.transactionDescription)
                            tvSelectedDateFragEditExpense.text = transaction.date.convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                            setCacheCategoryDropDownList(etExpenseCategoryFragEditExpense)
                        }

                        savedInstanceState?.let { bundle ->
                            val additionalExpenseCategoryList = bundle.getStringArrayList(UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY)?.toList()
                            if (additionalExpenseCategoryList!!.isNotEmpty()) {
                                TransactionUtil.addNewItem(
                                    additionalExpenseCategoryList,
                                    parentContainerLayoutFragEditExpense,
                                    requireContext()
                                )
                            }
                        }
                    }
                }
            }

            btAddMoreFragEditExpense.setOnClickListener {
                TransactionUtil.addNewItem(parentContainerLayoutFragEditExpense, requireContext())
            }

            btSelectDateFragEditExpense.setOnClickListener {
                unfinishedAndOnlineDateFrame?.let { unfinishedDateFrame ->
                    showMaterialDatePickerDialog {
                        it.addOnPositiveButtonClickListener { selection ->
                            val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()

                            val startPointDate = LocalDate.parse(
                                unfinishedDateFrame.startPointDate,
                                DateTimeFormatter.ISO_LOCAL_DATE
                            )

                            if (selectedDate > LocalDate.now()) {
                                showErrorMessage(resources.getString(R.string.error_message_edit_expense_future_date_selected), binding)
                            } else if (selectedDate < startPointDate) {
                                showErrorMessage(
                                    resources.getString(
                                        R.string.error_message_edit_expense_earlier_date_selected,
                                        unfinishedDateFrame.startPointDate.convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                                    ),
                                    binding
                                )
                            } else {
                                tvSelectedDateFragEditExpense.text =
                                    selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE).convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                            }
                        }
                        it.show(childFragmentManager, "date_selection")
                    }
                }
            }

            ivReturnBackFragEditExpense.setOnClickListener {
                val materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                materialAlertDialog.setMessage(resources.getString(R.string.alert_message_edit_expense_discard))
                    .setPositiveButton(resources.getString(R.string.alert_dialog_positive_yes)) { _, _ ->
                        findNavController().popBackStack()
                    }
                    .setNegativeButton(resources.getString(R.string.alert_dialog_negative_no)) { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }
                    .show()
            }

            btSaveFragEditExpense.setOnClickListener {
                if (checkForEmptyColumns()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            unfinishedAndOnlineDateFrame?.let { unfinishedDateFrame ->
                                    transactionViewModel.getTransactionOfDateFrameById(
                                        unfinishedDateFrame.dateFrameId!!,
                                        dateFrameViewModel.selectedTransaction?.transactionId!!
                                    )?.let { transaction ->
                                        buttonLoadingState(true)
                                        delay(500)// JUST TO SIMULATE LOADING..

                                        val expenseDateDateLimit = dateLimitViewModel.getDateLimitOfDateFrameByDate(
                                            unfinishedDateFrame.dateFrameId!!,
                                            tvSelectedDateFragEditExpense.text.toString().convertDateToIso8601From(DATE_LIMIT_DATE_PATTERN)
                                        )
                                        if (TransactionUtil.isSingleTransaction(parentContainerLayoutFragEditExpense)) {
                                            transaction.transactionAmount = etAmountFragEditExpense.text.toString().toDouble()
                                            transaction.transactionCurrency = actvCurrencyFragEditExpense.text.toString()
                                            transaction.transactionCategory = etExpenseCategoryFragEditExpense.text.toString()
                                            transaction.transactionCategories = mutableListOf()
                                            transaction.transactionDescription = etDescriptionFragEditExpense.text.toString()
                                            transaction.dateLimitId = expenseDateDateLimit?.dateLimitId!!
                                            transaction.date = expenseDateDateLimit.date
                                            transaction.isSelected = false
                                            transactionViewModel.upsertTransaction(transaction)
                                            userViewModel.cacheNewExpenseCategory(etExpenseCategoryFragEditExpense.text.toString())
                                        } else {
                                            val expenseCategoryList = TransactionUtil.getAllTransactionCategoryList(
                                                parentContainerLayoutFragEditExpense,
                                                etExpenseCategoryFragEditExpense.text.toString()
                                            )
                                            transaction.transactionAmount = etAmountFragEditExpense.text.toString().toDouble()
                                            transaction.transactionCurrency = actvCurrencyFragEditExpense.text.toString()
                                            transaction.transactionCategory = ""
                                            transaction.transactionCategories = expenseCategoryList
                                            transaction.transactionDescription = etDescriptionFragEditExpense.text.toString()
                                            transaction.dateLimitId = expenseDateDateLimit?.dateLimitId!!
                                            transaction.date = expenseDateDateLimit.date
                                            transaction.isSelected = false
                                            transactionViewModel.upsertTransaction(transaction)
                                            userViewModel.cacheNewExpenseCategory(expenseCategoryList)
                                        }

                                        dateFrameViewModel.updateTotalExpenseAmountOf(unfinishedDateFrame)
                                        dateFrameViewModel.clearAllTransactionPairs()

                                        dateLimitViewModel.getDateLimitOfDateFrameByDate(unfinishedDateFrame.dateFrameId!!, transaction.date)?.let { oldDateLimit ->
                                            checkForLimitExceededValue(oldDateLimit)
                                        }

                                        if (expenseDateDateLimit.date != transaction.date) {
                                            delay(200)
                                            reCalculateSubsequentExpenseLimits(expenseDateDateLimit)
                                        }

                                        delay(300) // JUST TO SIMULATE LOADING..
                                        findNavController().popBackStack()
                                    }
                            }
                        }
                    }
                }
            }

            parentContainerLayoutFragEditExpense.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    val etCategory = child?.findViewById<AutoCompleteTextView>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)
                    setCacheCategoryDropDownList(etCategory!!)
                }

                override fun onChildViewRemoved(parent: View?, child: View?) {}
            })

        }

        return binding.root
    }

    private suspend fun reCalculateSubsequentExpenseLimits(selectedDateLimit: DateLimit) {
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

    private fun checkForEmptyColumns(): Boolean {
        val isReady: Boolean
        val expenseAmount = binding.etAmountFragEditExpense.text.toString()
        val currency = binding.actvCurrencyFragEditExpense.text.toString()

        val initialExpenseCategory = binding.etExpenseCategoryFragEditExpense.text.toString()

        when {
            expenseAmount.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_edit_expense_no_amount_added), binding)
                isReady = false
            }

            currency.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_edit_expense_no_currency_selected), binding)
                isReady = false
            }

            initialExpenseCategory.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_add_expense_no_expense_category_added), binding)
                isReady = false
            }

            !TransactionUtil.isAdditionalExpenseCategoryListEmpty(binding.parentContainerLayoutFragEditExpense) -> {
                showErrorMessage(resources.getString(R.string.error_message_edit_expense_empty_additional_category_column), binding)
                isReady = false
            }

            else -> {
                isReady = true
            }
        }

        return isReady
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.apply {
            outState.putString(UIStateConstant.TRANSACTION_AMOUNT_KEY, etAmountFragEditExpense.text.toString())
            outState.putString(UIStateConstant.INITIAL_TRANSACTION_CATEGORY_KEY, etExpenseCategoryFragEditExpense.text.toString())
            outState.putString(UIStateConstant.TRANSACTION_DESCRIPTION_KEY, etDescriptionFragEditExpense.text.toString())
            outState.putString(UIStateConstant.CUSTOM_TRANSACTION_DATE_KEY, tvSelectedDateFragEditExpense.text.toString())

            if (parentContainerLayoutFragEditExpense.childCount > 1) {
                outState.putStringArrayList(
                    UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY,
                    ArrayList(TransactionUtil.getAdditionalTransactionCategoryList(parentContainerLayoutFragEditExpense))
                )
            } else {
                outState.putStringArrayList(UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY, ArrayList(listOf()))
            }

            actvCurrencyFragEditExpense.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    outState.putInt(UIStateConstant.TRANSACTION_CURRENCY_KEY, position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun showMaterialDatePickerDialog(callback: (MaterialDatePicker<Long>) -> Unit) {
        val date =
            LocalDate.parse(dateFrameViewModel.selectedTransaction?.date, DateTimeFormatter.ISO_LOCAL_DATE)
        val materialDatePicker: MaterialDatePicker<Long>?
        materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(resources.getString(R.string.date_picker_title_text_expense_date))
            .setSelection(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()

        callback(materialDatePicker)
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

    private fun buttonLoadingState (isLoading: Boolean) {
        binding.apply {
            when (isLoading) {
                true -> {
                    btSaveFragEditExpense.startAnimation(shrinkButtonAnim)
                    ltLoadingFragEditExpense.visibility = View.VISIBLE
                    btSaveFragEditExpense.isClickable = false
                    btSaveFragEditExpense.visibility = View.INVISIBLE
                }
                else -> {
                    btSaveFragEditExpense.startAnimation(expandButtonAnim)
                    ltLoadingFragEditExpense.visibility = View.INVISIBLE
                    btSaveFragEditExpense.isClickable = true
                    btSaveFragEditExpense.visibility = View.VISIBLE
                }
            }
        }
    }
}