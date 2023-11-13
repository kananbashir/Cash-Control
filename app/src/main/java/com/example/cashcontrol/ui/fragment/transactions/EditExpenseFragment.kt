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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.databinding.FragmentEditExpenseBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.UIStateConstant
import com.example.cashcontrol.util.constant.UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.CUSTOM_TRANSACTION_DATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.INITIAL_TRANSACTION_CATEGORY_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TRANSACTION_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TRANSACTION_CURRENCY_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TRANSACTION_DESCRIPTION_KEY
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
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
    private val shrinkInsideAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_inside) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateLimitViewModel = ViewModelProvider(requireActivity()).get(DateLimitViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        dateFrameViewModel = ViewModelProvider(requireActivity()).get(DateFrameViewModel::class.java)
        transactionViewModel = ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        binding = FragmentEditExpenseBinding.inflate(layoutInflater)

        val currencyArrayAdapter = ArrayAdapter(requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies))
        binding.actvCurrencyFragEditExpense.setAdapter(currencyArrayAdapter)

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
                etAmountFragEditExpense.setText(bundle.getString(TRANSACTION_AMOUNT_KEY))
                actvCurrencyFragEditExpense.setSelection(bundle.getInt(TRANSACTION_CURRENCY_KEY))
                tvSelectedDateFragEditExpense.text = bundle.getString(CUSTOM_TRANSACTION_DATE_KEY)

                etExpenseCategoryFragEditExpense.setText(bundle.getString(INITIAL_TRANSACTION_CATEGORY_KEY))
                tvDescriptionFragEditExpense.text = bundle.getString(TRANSACTION_DESCRIPTION_KEY)

                val additionalExpenseCategoryList = bundle.getStringArrayList(ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY)?.toList()
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

//        binding.apply {
//            val selectedTransaction = dateLimitViewModel.selectedTransaction
//            selectedTransaction?.let {
//                if (it.transactionCategory.isEmpty()) {
//                    etExpenseCategoryFragEditExpense.setText(it.transactionCategories[0])
//                    inflateCategories(it.transactionCategories)
//                } else {
//                    etExpenseCategoryFragEditExpense.setText(it.transactionCategory)
//                }
//
//                etAmountFragEditExpense.setText(it.transactionAmount.toString())
//                etDescriptionFragEditExpense.setText(it.transactionDescription)
//                tvSelectedDateFragEditExpense.text = it.date
//                setCacheCategoryDropDownList(etExpenseCategoryFragEditExpense)
//            }
//
//            btAddMoreFragEditExpense.setOnClickListener {
//                addNewItem()
//            }
//
//            btSelectDateFragEditExpense.setOnClickListener {
//                showMaterialDatePickerDialog {
//                    it.addOnPositiveButtonClickListener { selection ->
//                        val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
//
//                        dateFrameViewModel.unfinishedDateFrame.value?.let { unfinishedDf ->
//                            val startPointDate = LocalDate.parse(unfinishedDf.startPointDate, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
//
//                            if (selectedDate > LocalDate.now()) {
//                                showErrorMessage("You cannot choose future dates..")
//                            } else if (selectedDate < startPointDate) {
//                                showErrorMessage("The chosen date must not be earlier than the start point (${unfinishedDf.startPointDate}) date!")
//                            } else {
//                                tvSelectedDateFragEditExpense.text = selectedDate.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
//                            }
//                        }
//                    }
//                    it.show(childFragmentManager, "date_selection")
//                }
//            }
//
//            ivReturnBackFragEditExpense.setOnClickListener {
//                val materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
//                materialAlertDialog.setMessage("Are you sure you want to discard all changes?")
//                    .setPositiveButton("Yes") { _, _ ->
//                        findNavController().popBackStack()
//                    }
//                    .setNegativeButton("No") { dialogInterface, _ ->
//                        dialogInterface.cancel()
//                    }
//                    .show()
//            }
//
//            btSaveFragEditExpense.setOnClickListener {
//                lifecycleScope.launch {
//
////                    if (checkForEmptyColumns()) {
////                        dateLimitViewModel.currentDateLimit.value?.let {
////                            val transaction = dateLimitViewModel.selectedTransaction!!
////                            if (isSingleExpense()) {
////                                if (wasBatchExpense()) {
////                                    transactionViewModel.deleteTransaction(transaction)
////                                    transactionViewModel.upsertTransaction(
////                                        SingleExpense(
////                                            etAmountFragEditExpense.text.toString().toDouble(),
////                                            actvCurrencyFragEditExpense.text.toString(),
////                                            etExpenseCategoryFragEditExpense.text.toString(),
////                                            etDescriptionFragEditExpense.text.toString(),
////                                            tvSelectedDateFragEditExpense.text.toString()
////                                        )
////                                    )
////                                } else {
////                                    transaction.transactionAmount = etAmountFragEditExpense.text.toString().toDouble()
////                                    transaction.transactionCurrency = actvCurrencyFragEditExpense.text.toString()
////                                    transaction.transactionCategory = etExpenseCategoryFragEditExpense.text.toString()
////                                    transaction.transactionDescription = etDescriptionFragEditExpense.text.toString()
////                                    transaction.date = tvSelectedDateFragEditExpense.text.toString()
////                                    transaction.isSelected = false
////                                    transactionViewModel.upsertTransaction(transaction)
////                                }
////                                userViewModel.cacheNewExpenseCategory(etExpenseCategoryFragEditExpense.text.toString())
////                            } else {
////                                val expenseCategoryList = getAllExpenseCategoryList()
////                                if (wasSingleExpense()) {
////                                    transactionViewModel.deleteTransaction(transaction)
////                                    transactionViewModel.upsertTransaction(BatchExpense(
////                                        etAmountFragEditExpense.text.toString().toDouble(),
////                                        actvCurrencyFragEditExpense.text.toString(),
////                                        expenseCategoryList,
////                                        etDescriptionFragEditExpense.text.toString(),
////                                        tvSelectedDateFragEditExpense.text.toString()
////                                    ))
////                                } else {
////                                    transaction.transactionAmount = etAmountFragEditExpense.text.toString().toDouble()
////                                    transaction.transactionCurrency = actvCurrencyFragEditExpense.text.toString()
////                                    transaction.transactionCategories = expenseCategoryList
////                                    transaction.transactionDescription = etDescriptionFragEditExpense.text.toString()
////                                    transaction.date = tvSelectedDateFragEditExpense.text.toString()
////                                    transaction.isSelected = false
////                                    transactionViewModel.upsertTransaction(transaction)
////                                }
////                                userViewModel.cacheNewExpenseCategory(expenseCategoryList)
////                            }
////                            updateButtonToLoadingState()
////                            dateFrameViewModel.updateExpenseAmount(etAmountFragEditExpense.text.toString().toDouble())
////                            dateLimitViewModel.setExpensesForDate()
////                            profileViewModel.setProfileWithDateFrames()
////                            delay(1500) // JUST TO SIMULATE LOADING..
////                            dateFrameViewModel.uiAnimState = false
////                            dateLimitViewModel.uiAnimState = false
////                            findNavController().navigate(EditExpenseFragmentDirections.actionEditExpenseFragmentToTransactionsDetailFragment())
////                        }
////                    }
//                }
//            }
//
//            parentContainerLayoutFragEditExpense.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
//                override fun onChildViewAdded(parent: View?, child: View?) {
//                    val etCategory = child?.findViewById<AutoCompleteTextView>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)
//                    setCacheCategoryDropDownList(etCategory!!)
//                }
//
//                override fun onChildViewRemoved(parent: View?, child: View?) {}
//            })
//
//        }

        return binding.root
    }

    private fun addNewItem() {
        val parentLayout = binding.parentContainerLayoutFragEditExpense
        val childView = LayoutInflater.from(requireContext()).inflate(R.layout.item_layout_transaction_category, parentLayout, false)
        val ivDelete = childView.findViewById<ImageView>(R.id.ivDeleteItemLayoutExpenseAndIncomeCategory)

        ivDelete.setOnClickListener { parentLayout.removeView(childView) }

        parentLayout.addView(childView)
    }

    private fun addNewItem (additionalExpenseCategoryList: List<String>) {
        val parentLayout = binding.parentContainerLayoutFragEditExpense
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
        val expenseAmount = binding.etAmountFragEditExpense.text.toString()
        val currency = binding.actvCurrencyFragEditExpense.text.toString()

        val initialExpenseCategory = binding.etExpenseCategoryFragEditExpense.text.toString()
        var isAdditionalExpenseCategoriesNotEmpty = true

        val parentLayout = binding.parentContainerLayoutFragEditExpense

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
                showErrorMessage("Consider adding expense amount..")
                isReady = false
            }
            currency.isEmpty() -> {
                showErrorMessage("Please select currency..")
                isReady = false
            }
            initialExpenseCategory.isEmpty() -> {
                showErrorMessage("Please write at least one expense category..")
                isReady = false
            }
            !isAdditionalExpenseCategoriesNotEmpty -> {
                showErrorMessage("Please fill additionally added expense categories or delete unused ones..")
                isReady = false
            }
            else -> { isReady = true }
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
                outState.putStringArrayList(UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY, ArrayList(getAdditionalExpenseCategoryList()))
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

    private fun isSingleExpense (): Boolean {
        val parentLayout = binding.parentContainerLayoutFragEditExpense
        return parentLayout.childCount == 1
    }

//    private fun wasBatchExpense (): Boolean {
//        return dateLimitViewModel.selectedTransaction!!.transactionCategory.isEmpty()
//    }
//
//    private fun wasSingleExpense (): Boolean {
//        return dateLimitViewModel.selectedTransaction!!.transactionCategory.isNotEmpty()
//    }

    private fun inflateCategories(categoriesList: List<String>) {
        val parentLayout = binding.parentContainerLayoutFragEditExpense

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

    private fun getAdditionalExpenseCategoryList (): MutableList<String> {
        val parentLayout = binding.parentContainerLayoutFragEditExpense
        val expenseCategoryList: MutableList<String> = mutableListOf()

        for (i in 1 until parentLayout.childCount) {
            val childView = parentLayout.getChildAt(i) as ConstraintLayout
            val expenseCat = childView.findViewById<EditText>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

            expenseCategoryList.add(expenseCat.text.toString())
        }

        return expenseCategoryList
    }


    private fun getAllExpenseCategoryList (): MutableList<String> {
        val parentLayout = binding.parentContainerLayoutFragEditExpense
        val expenseCategoryList: MutableList<String> = mutableListOf()

        expenseCategoryList.add(binding.etExpenseCategoryFragEditExpense.text.toString())

        for (i in 1 until parentLayout.childCount) {
            val childView = parentLayout.getChildAt(i) as ConstraintLayout
            val expenseCat = childView.findViewById<EditText>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

            expenseCategoryList.add(expenseCat.text.toString())
        }

        return expenseCategoryList
    }

//    private fun showMaterialDatePickerDialog (callback: (MaterialDatePicker<Long>) -> Unit) {
//        val date = LocalDate.parse(dateLimitViewModel.selectedTransaction?.date, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
//        var materialDatePicker: MaterialDatePicker<Long>? = null
//        materialDatePicker = MaterialDatePicker.Builder.datePicker()
//            .setTitleText("Select expense date")
//            .setSelection(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
//            .build()
//
//        callback (materialDatePicker)
//    }
//
//    private fun setCacheCategoryDropDownList (autoCompleteTextView: AutoCompleteTextView) {
//        userViewModel.onlineUser.value?.let {
//            if (it.cachedExpenseCategories.isNotEmpty()) {
//                val cachedCategoriesAdapter = ArrayAdapter (requireContext(),
//                    R.layout.item_layout_cached_categories, R.id.tvCategoryName, it.cachedExpenseCategories.toTypedArray())
//                autoCompleteTextView.setAdapter(cachedCategoriesAdapter)
//            }
//        }
//    }

    private fun updateButtonToLoadingState () {
        binding.apply {
            btSaveFragEditExpense.apply {
                startAnimation(shrinkInsideAnim)
                isClickable = false
                visibility = View.INVISIBLE
            }
            ltLoadingFragEditExpense.visibility = View.VISIBLE
        }
    }

    private fun showErrorMessage (message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_SHORT)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
            .setBackgroundTint(resources.getColor(R.color.bittersweet_red, null))
            .show()
    }
}