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
import com.example.cashcontrol.databinding.FragmentEditIncomeBinding
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class EditIncomeFragment : Fragment() {
    private lateinit var binding: FragmentEditIncomeBinding
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
        binding = FragmentEditIncomeBinding.inflate(layoutInflater)

        val currencyArrayAdapter = ArrayAdapter(requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies))
        binding.actvCurrencyFragEditIncome.setAdapter(currencyArrayAdapter)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                materialAlertDialog.setMessage(resources.getString(R.string.alert_message_edit_income_discard))
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
                etAmountFragEditIncome.setText(bundle.getString(UIStateConstant.TRANSACTION_AMOUNT_KEY))
                actvCurrencyFragEditIncome.setSelection(bundle.getInt(UIStateConstant.TRANSACTION_CURRENCY_KEY))
                tvSelectedDateFragEditIncome.text = bundle.getString(UIStateConstant.CUSTOM_TRANSACTION_DATE_KEY)

                etIncomeSourceFragEditIncome.setText(bundle.getString(UIStateConstant.INITIAL_TRANSACTION_CATEGORY_KEY))
                tvDescriptionFragEditIncome.text = bundle.getString(UIStateConstant.TRANSACTION_DESCRIPTION_KEY)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.apply {

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    getUnfinishedDateFrame()?.let {
                        unfinishedAndOnlineDateFrame = it

                        val selectedTransaction = dateFrameViewModel.selectedTransaction
                        selectedTransaction?.let { transaction ->
                            if (transaction.transactionSource.isEmpty()) {
                                etIncomeSourceFragEditIncome.setText(transaction.transactionSources[0])
                                TransactionUtil.inflateTransactionCategories(
                                    transaction.transactionSources,
                                    parentContainerLayoutFragEditIncome,
                                    requireContext()
                                )
                            } else {
                                etIncomeSourceFragEditIncome.setText(transaction.transactionSource)
                            }

                            etAmountFragEditIncome.setText(transaction.transactionAmount.toString())
                            etDescriptionFragEditIncome.setText(transaction.transactionDescription)
                            tvSelectedDateFragEditIncome.text = transaction.date.convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                            setCacheCategoryDropDownList(etIncomeSourceFragEditIncome)

                            savedInstanceState?.let { bundle ->
                                val additionalExpenseCategoryList = bundle.getStringArrayList(UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY)?.toList()
                                if (additionalExpenseCategoryList!!.isNotEmpty()) {
                                    TransactionUtil.addNewItem(additionalExpenseCategoryList, parentContainerLayoutFragEditIncome, requireContext())
                                }
                            }
                        }
                    }
                }
            }

            btAddMoreFragEditIncome.setOnClickListener {
                TransactionUtil.addNewItem(parentContainerLayoutFragEditIncome, requireContext())
            }

            btSelectDateFragEditIncome.setOnClickListener {
                unfinishedAndOnlineDateFrame?.let { unfinishedDateFrame ->
                    showMaterialDatePickerDialog {
                        it.addOnPositiveButtonClickListener { selection ->
                            val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()

                            val startPointDate = LocalDate.parse(
                                unfinishedDateFrame.startPointDate, DateTimeFormatter.ISO_LOCAL_DATE
                            )

                            if (selectedDate > LocalDate.now()) {
                                showErrorMessage(resources.getString(R.string.error_message_edit_income_future_date_selected), binding)
                            } else if (selectedDate < startPointDate) {
                                showErrorMessage(
                                    resources.getString(
                                        R.string.error_message_edit_income_earlier_date_selected,
                                        unfinishedDateFrame.startPointDate.convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                                    ),
                                    binding
                                )
                            } else {
                                tvSelectedDateFragEditIncome.text =
                                    selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE).convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                            }
                        }
                        it.show(childFragmentManager, "date_selection")
                    }
                }
            }

            ivReturnBackFragEditIncome.setOnClickListener {
                val materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                materialAlertDialog.setMessage(resources.getString(R.string.alert_message_edit_income_discard))
                    .setPositiveButton(resources.getString(R.string.alert_dialog_positive_yes)) { _, _ ->
                        findNavController().popBackStack()
                    }
                    .setNegativeButton(resources.getString(R.string.alert_dialog_negative_no)) { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }
                    .show()
            }

            btSaveFragEditIncome.setOnClickListener {
                if (checkForEmptyColumns()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            getUnfinishedDateFrame()?.let { unfinishedDateFrame ->
                                transactionViewModel.getTransactionOfDateFrameById(
                                    unfinishedDateFrame.dateFrameId!!,
                                    dateFrameViewModel.selectedTransaction?.transactionId!!
                                )?.let { transaction ->
                                    buttonLoadingState(true)
                                    delay(500)// JUST TO SIMULATE LOADING..

                                    val incomeDateDateLimit = dateLimitViewModel.getDateLimitOfDateFrameByDate(
                                        unfinishedDateFrame.dateFrameId!!,
                                        tvSelectedDateFragEditIncome.text.toString().convertDateToIso8601From(DATE_LIMIT_DATE_PATTERN)
                                    )
                                    if (TransactionUtil.isSingleTransaction(parentContainerLayoutFragEditIncome)) {
                                        transaction.transactionAmount = etAmountFragEditIncome.text.toString().toDouble()
                                        transaction.transactionCurrency = actvCurrencyFragEditIncome.text.toString()
                                        transaction.transactionSource = etIncomeSourceFragEditIncome.text.toString()
                                        transaction.transactionSources = mutableListOf()
                                        transaction.transactionDescription = etDescriptionFragEditIncome.text.toString()
                                        transaction.dateLimitId = incomeDateDateLimit?.dateLimitId!!
                                        transaction.date = incomeDateDateLimit.date
                                        transaction.isSelected = false
                                        transactionViewModel.upsertTransaction(transaction)
                                        userViewModel.cacheNewIncomeSource(etIncomeSourceFragEditIncome.text.toString())
                                    } else {
                                        val expenseCategoryList = TransactionUtil.getAllTransactionCategoryList(
                                            parentContainerLayoutFragEditIncome,
                                            etIncomeSourceFragEditIncome.text.toString()
                                        )
                                        transaction.transactionAmount = etAmountFragEditIncome.text.toString().toDouble()
                                        transaction.transactionCurrency = actvCurrencyFragEditIncome.text.toString()
                                        transaction.transactionSource = ""
                                        transaction.transactionSources = expenseCategoryList
                                        transaction.transactionDescription = etDescriptionFragEditIncome.text.toString()
                                        transaction.dateLimitId = incomeDateDateLimit?.dateLimitId!!
                                        transaction.date = incomeDateDateLimit.date
                                        transaction.isSelected = false
                                        transactionViewModel.upsertTransaction(transaction)
                                        userViewModel.cacheNewIncomeSource(expenseCategoryList)
                                    }

                                    dateFrameViewModel.updateTotalIncomeAmountOf(unfinishedDateFrame)
                                    dateFrameViewModel.clearAllTransactionPairs()
                                    delay(200)
                                    reCalculateSubsequentExpenseLimits(incomeDateDateLimit)

                                    delay(300) // JUST TO SIMULATE LOADING..
                                    findNavController().popBackStack()
                                }
                            }
                        }
                    }
                }
            }

            parentContainerLayoutFragEditIncome.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    val etCategory = child?.findViewById<AutoCompleteTextView>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)
                    setCacheCategoryDropDownList(etCategory!!)
                }

                override fun onChildViewRemoved(parent: View?, child: View?) {}
            })

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

    private fun checkForEmptyColumns (): Boolean {
        val isReady: Boolean
        val incomeAmount = binding.etAmountFragEditIncome.text.toString()
        val currency = binding.actvCurrencyFragEditIncome.text.toString()

        val initialIncomeSource = binding.etIncomeSourceFragEditIncome.text.toString()

        when {
            incomeAmount.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_edit_income_no_amount_added), binding)
                isReady = false
            }
            currency.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_edit_income_no_currency_selected), binding)
                isReady = false
            }
            initialIncomeSource.isEmpty() -> {
                showErrorMessage(resources.getString(R.string.error_message_edit_income_no_income_source_added), binding)
                isReady = false
            }
            !TransactionUtil.isAdditionalExpenseCategoryListEmpty(binding.parentContainerLayoutFragEditIncome) -> {
                showErrorMessage(resources.getString(R.string.error_message_edit_income_empty_additional_category_column), binding)
                isReady = false
            }
            else -> { isReady = true }
        }

        return isReady
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.apply {
            outState.putString(UIStateConstant.TRANSACTION_AMOUNT_KEY, etAmountFragEditIncome.text.toString())
            outState.putString(UIStateConstant.INITIAL_TRANSACTION_CATEGORY_KEY, etIncomeSourceFragEditIncome.text.toString())
            outState.putString(UIStateConstant.TRANSACTION_DESCRIPTION_KEY, etDescriptionFragEditIncome.text.toString())
            outState.putString(UIStateConstant.CUSTOM_TRANSACTION_DATE_KEY, tvSelectedDateFragEditIncome.text.toString())

            if (parentContainerLayoutFragEditIncome.childCount > 1) {
                outState.putStringArrayList(UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY, ArrayList(
                    TransactionUtil.getAdditionalTransactionCategoryList(parentContainerLayoutFragEditIncome)
                ))
            } else {
                outState.putStringArrayList(UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY, ArrayList(listOf()))
            }

            actvCurrencyFragEditIncome.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    outState.putInt(UIStateConstant.TRANSACTION_CURRENCY_KEY, position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
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

    private fun showMaterialDatePickerDialog (callback: (MaterialDatePicker<Long>) -> Unit) {
        val date = LocalDate.parse(dateFrameViewModel.selectedTransaction?.date, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
        val materialDatePicker: MaterialDatePicker<Long> = MaterialDatePicker.Builder.datePicker()
            .setTitleText(resources.getString(R.string.date_picker_title_text_income_date))
            .setSelection(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .build()

        callback (materialDatePicker)
    }

    private fun setCacheCategoryDropDownList (autoCompleteTextView: AutoCompleteTextView) {
        if (userViewModel.cachedIncomeSources.isNotEmpty()) {
            val cachedCategoriesListAdapter = ArrayAdapter(
                requireContext(),
                R.layout.item_layout_cached_categories, R.id.tvCategoryName, userViewModel.cachedIncomeSources.toTypedArray()
            )
            autoCompleteTextView.setAdapter(cachedCategoriesListAdapter)
        }
    }

    private fun buttonLoadingState (isLoading: Boolean) {
        binding.apply {
            when (isLoading) {
                true -> {
                    btSaveFragEditIncome.startAnimation(shrinkButtonAnim)
                    ltLoadingFragEditIncome.visibility = View.VISIBLE
                    btSaveFragEditIncome.isClickable = false
                    btSaveFragEditIncome.visibility = View.INVISIBLE
                }
                else -> {
                    btSaveFragEditIncome.startAnimation(expandButtonAnim)
                    ltLoadingFragEditIncome.visibility = View.INVISIBLE
                    btSaveFragEditIncome.isClickable = true
                    btSaveFragEditIncome.visibility = View.VISIBLE
                }
            }
        }
    }
}