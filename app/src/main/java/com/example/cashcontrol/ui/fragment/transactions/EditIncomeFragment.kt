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
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.databinding.FragmentEditIncomeBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.constant.DateConstant
import com.example.cashcontrol.util.constant.UIStateConstant
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EditIncomeFragment : Fragment() {
    private lateinit var binding: FragmentEditIncomeBinding
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private val shrinkInsideAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_inside) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateLimitViewModel = ViewModelProvider(requireActivity()).get(DateLimitViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        dateFrameViewModel = ViewModelProvider(requireActivity()).get(DateFrameViewModel::class.java)
        transactionViewModel = ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        binding = FragmentEditIncomeBinding.inflate(layoutInflater)

        val currencyArrayAdapter = ArrayAdapter(requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies))
        binding.actvCurrencyFragEditIncome.setAdapter(currencyArrayAdapter)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                materialAlertDialog.setMessage("Are you sure you want to discard all changes?")
                    .setPositiveButton("Yes") { _, _ ->
                        findNavController().popBackStack()
                    }
                    .setNegativeButton("No") { dialogInterface, _ ->
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

                val additionalExpenseCategoryList = bundle.getStringArrayList(UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY)?.toList()
                if (additionalExpenseCategoryList!!.isNotEmpty()) {
                    addNewItem(additionalExpenseCategoryList)
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.apply {
            val selectedTransaction = dateFrameViewModel.selectedTransaction
            selectedTransaction?.let {
                if (it.transactionSource.isEmpty()) {
                    etIncomeSourceFragEditIncome.setText(it.transactionSources[0])
                    inflateCategories(it.transactionSources)
                } else {
                    etIncomeSourceFragEditIncome.setText(it.transactionSource)
                }

                etAmountFragEditIncome.setText(it.transactionAmount.toString())
                etDescriptionFragEditIncome.setText(it.transactionDescription)
                tvSelectedDateFragEditIncome.text = it.date
                setCacheCategoryDropDownList(etIncomeSourceFragEditIncome)
            }

            btAddMoreFragEditIncome.setOnClickListener {
                addNewItem()
            }

            btSelectDateFragEditIncome.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        getUnfinishedDateFrame()?.let { unfinishedDateFrame ->
                            showMaterialDatePickerDialog {
                                it.addOnPositiveButtonClickListener { selection ->
                                    val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()

                                    val startPointDate = LocalDate.parse(unfinishedDateFrame.startPointDate, DateTimeFormatter.ofPattern(
                                        DateConstant.DATE_LIMIT_DATE_PATTERN
                                    ))

                                    if (selectedDate > LocalDate.now()) {
                                        showErrorMessage("You cannot choose future dates..", binding)
                                    } else if (selectedDate < startPointDate) {
                                        showErrorMessage(
                                            "The chosen date must not be earlier than the start point (${unfinishedDateFrame.startPointDate}) date!",
                                            binding
                                        )
                                    } else {
                                        tvSelectedDateFragEditIncome.text = selectedDate.format(DateTimeFormatter.ofPattern(DateConstant.DATE_LIMIT_DATE_PATTERN))
                                    }
                                }
                                it.show(childFragmentManager, "date_selection")
                            }
                        }
                    }
                }
            }

            ivReturnBackFragEditIncome.setOnClickListener {
                val materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                materialAlertDialog.setMessage("Are you sure you want to discard all changes?")
                    .setPositiveButton("Yes") { _, _ ->
                        findNavController().popBackStack()
                    }
                    .setNegativeButton("No") { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }
                    .show()
            }

            btSaveFragEditIncome.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        getUnfinishedDateFrame()?.let { unfinishedDateFrame ->
                            dateLimitViewModel.getCurrentDateLimitByDateFrame(unfinishedDateFrame.dateFrameId!!)?.let { currentDateLimit ->
                                if (checkForEmptyColumns()) {
                                    transactionViewModel.getTransactionOfDateFrameById(unfinishedDateFrame.dateFrameId!!, dateFrameViewModel.selectedTransaction?.transactionId!!)?.let { transaction ->
                                        val incomeDate = dateLimitViewModel.getDateLimitOfDateFrameByDate(unfinishedDateFrame.dateFrameId!!, tvSelectedDateFragEditIncome.text.toString())
                                        if (isSingleIncome()) {
                                            transaction.transactionAmount = etAmountFragEditIncome.text.toString().toDouble()
                                            transaction.transactionCurrency = actvCurrencyFragEditIncome.text.toString()
                                            transaction.transactionSource = etIncomeSourceFragEditIncome.text.toString()
                                            transaction.transactionSources = mutableListOf()
                                            transaction.transactionDescription = etDescriptionFragEditIncome.text.toString()
                                            transaction.dateLimitId = incomeDate?.dateLimitId!!
                                            transaction.date = incomeDate.date
                                            transaction.isSelected = false
                                            transactionViewModel.upsertTransaction(transaction)
                                            userViewModel.cacheNewIncomeSource(etIncomeSourceFragEditIncome.text.toString())
                                        } else {
                                            val expenseCategoryList = getAllIncomeSourceList()
                                            transaction.transactionAmount = etAmountFragEditIncome.text.toString().toDouble()
                                            transaction.transactionCurrency = actvCurrencyFragEditIncome.text.toString()
                                            transaction.transactionSource = ""
                                            transaction.transactionSources = expenseCategoryList
                                            transaction.transactionDescription = etDescriptionFragEditIncome.text.toString()
                                            transaction.dateLimitId = incomeDate?.dateLimitId!!
                                            transaction.date = incomeDate.date
                                            transaction.isSelected = false
                                            transactionViewModel.upsertTransaction(transaction)
                                            userViewModel.cacheNewIncomeSource(expenseCategoryList)
                                        }
                                        updateButtonToLoadingState()
                                        dateFrameViewModel.updateIncomeAmount(etAmountFragEditIncome.text.toString().toDouble(), unfinishedDateFrame)
                                        dateFrameViewModel.clearAllTransactionPairs()
                                        delay(1500) // JUST TO SIMULATE LOADING..
                                        findNavController().navigate(EditIncomeFragmentDirections.actionEditIncomeFragmentToTransactionsDetailFragment())
                                    }
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

    private fun addNewItem() {
        val parentLayout = binding.parentContainerLayoutFragEditIncome
        val childView = LayoutInflater.from(requireContext()).inflate(R.layout.item_layout_transaction_category, parentLayout, false)
        val ivDelete = childView.findViewById<ImageView>(R.id.ivDeleteItemLayoutExpenseAndIncomeCategory)

        ivDelete.setOnClickListener { parentLayout.removeView(childView) }

        parentLayout.addView(childView)
    }

    private fun addNewItem (additionalExpenseCategoryList: List<String>) {
        val parentLayout = binding.parentContainerLayoutFragEditIncome
        lifecycleScope.launch {
            for (i in additionalExpenseCategoryList.indices) {

                delay(10) // I don't know why but it only works properly with 10ms or higher delay..
                val childView = LayoutInflater.from(requireContext()).inflate(R.layout.item_layout_transaction_category,  parentLayout, false)
                val deleteButton = childView.findViewById<ImageView>(R.id.ivDeleteItemLayoutExpenseAndIncomeCategory)
                val etExpenseCategory = childView.findViewById<AutoCompleteTextView>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

                etExpenseCategory.setText(additionalExpenseCategoryList[i])
                deleteButton.setOnClickListener { parentLayout.removeView(childView) }

                parentLayout.addView(childView)

            }
        }
    }

    private fun checkForEmptyColumns (): Boolean {
        val isReady: Boolean
        val expenseAmount = binding.etAmountFragEditIncome.text.toString()
        val currency = binding.actvCurrencyFragEditIncome.text.toString()

        val initialIncomeSource = binding.etIncomeSourceFragEditIncome.text.toString()
        var isAdditionalExpenseCategoriesNotEmpty = true

        val parentLayout = binding.parentContainerLayoutFragEditIncome

        for (i in 1 until parentLayout.childCount) {
            val childView = parentLayout.getChildAt(i) as ConstraintLayout
            val expenseCategory = childView.findViewById<EditText>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

            if (expenseCategory.text.toString().isEmpty()) {
                isAdditionalExpenseCategoriesNotEmpty = false
                break
            }
        }

        when {
            expenseAmount.isEmpty() -> {
                showErrorMessage("Consider adding expense amount..", binding)
                isReady = false
            }
            currency.isEmpty() -> {
                showErrorMessage("Please select currency..", binding)
                isReady = false
            }
            initialIncomeSource.isEmpty() -> {
                showErrorMessage("Please write at least one expense category..", binding)
                isReady = false
            }
            !isAdditionalExpenseCategoriesNotEmpty -> {
                showErrorMessage("Please fill additionally added expense categories or delete unused ones..", binding)
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
                outState.putStringArrayList(UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY, ArrayList(getAdditionalIncomeSourceList()))
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

    private fun isSingleIncome (): Boolean {
        val parentLayout = binding.parentContainerLayoutFragEditIncome
        return parentLayout.childCount == 1
    }

    private fun inflateCategories(categoriesList: List<String>) {
        val parentLayout = binding.parentContainerLayoutFragEditIncome

        lifecycleScope.launch {
            delay(10) // I don't know why but it only works properly with 10ms or higher delay..
            for (i in 1 until categoriesList.size) {
                val childView = LayoutInflater.from(requireContext()).inflate(R.layout.item_layout_transaction_category, parentLayout, false) as ConstraintLayout
                val etCategory = childView.findViewById<AutoCompleteTextView>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)
                val ivDelete = childView.findViewById<ImageView>(R.id.ivDeleteItemLayoutExpenseAndIncomeCategory)
                etCategory.setText(categoriesList[i])

                ivDelete.setOnClickListener { parentLayout.removeView(childView) }

                parentLayout.addView(childView)
            }
        }
    }

    private fun getAdditionalIncomeSourceList (): MutableList<String> {
        val parentLayout = binding.parentContainerLayoutFragEditIncome
        val incomeSourceList: MutableList<String> = mutableListOf()

        for (i in 1 until parentLayout.childCount) {
            val childView = parentLayout.getChildAt(i) as ConstraintLayout
            val expenseCat = childView.findViewById<EditText>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

            incomeSourceList.add(expenseCat.text.toString())
        }

        return incomeSourceList
    }


    private fun getAllIncomeSourceList (): MutableList<String> {
        val parentLayout = binding.parentContainerLayoutFragEditIncome
        val incomeSourceList: MutableList<String> = mutableListOf()

        incomeSourceList.add(binding.etIncomeSourceFragEditIncome.text.toString())

        for (i in 1 until parentLayout.childCount) {
            val childView = parentLayout.getChildAt(i) as ConstraintLayout
            val expenseCat = childView.findViewById<EditText>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

            incomeSourceList.add(expenseCat.text.toString())
        }

        return incomeSourceList
    }

    private suspend fun getUnfinishedDateFrame(): DateFrame? {
        userViewModel.getOnlineUser()?.let { onlineUser ->
            profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                dateFrameViewModel.getUnfinishedDateFrameByProfile(onlineProfile.profileId!!)?.let { unfinishedDateFrame ->
                    return unfinishedDateFrame
                }
            }
        }
        return null
    }

    private fun showMaterialDatePickerDialog (callback: (MaterialDatePicker<Long>) -> Unit) {
        val date = LocalDate.parse(dateFrameViewModel.selectedTransaction?.date, DateTimeFormatter.ofPattern(DateConstant.DATE_LIMIT_DATE_PATTERN))
        var materialDatePicker: MaterialDatePicker<Long>? = null
        materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select expense date")
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

    private fun updateButtonToLoadingState () {
        binding.apply {
            btSaveFragEditIncome.apply {
                startAnimation(shrinkInsideAnim)
                isClickable = false
                visibility = View.INVISIBLE
            }
            ltLoadingFragEditIncome.visibility = View.VISIBLE
        }
    }
}