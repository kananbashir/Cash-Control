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
import com.example.cashcontrol.data.entity.expense.BatchExpense
import com.example.cashcontrol.data.entity.expense.SingleExpense
import com.example.cashcontrol.databinding.FragmentAddExpenseBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.UIStateConstant.ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.CUSTOM_TRANSACTION_DATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.INITIAL_TRANSACTION_CATEGORY_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.RADIO_GROUP_DATE_SELECT_STATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TRANSACTION_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TRANSACTION_CURRENCY_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TRANSACTION_DESCRIPTION_KEY
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
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
    private val shrinkInsideAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_inside) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionViewModel = ViewModelProvider (requireActivity()).get(TransactionViewModel::class.java)
        dateFrameViewModel = ViewModelProvider (requireActivity()).get(DateFrameViewModel::class.java)
        dateLimitViewModel = ViewModelProvider (requireActivity()).get(DateLimitViewModel::class.java)
        userViewModel = ViewModelProvider (requireActivity()).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        binding = FragmentAddExpenseBinding.inflate(layoutInflater)

        val currencyArrayAdapter = ArrayAdapter( requireContext(),
        R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies))
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

        binding.apply {

            parentContainerLayoutFragAddExpense.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    val etExpenseCategory = child?.findViewById<AutoCompleteTextView>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)
                    setCacheCategoryDropDownList(etExpenseCategory!!)
                    updateAddMoreButton()
                }

                override fun onChildViewRemoved(parent: View?, child: View?) { updateAddMoreButton() }
            })

            btAddMoreFragAddExpense.setOnClickListener { addNewItem() }
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
                showMaterialDatePickerDialog {
                    it.addOnPositiveButtonClickListener { selection ->
                        val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()

                        dateFrameViewModel.unfinishedDateFrame.value?.let { unfinishedDf ->
                            val startPointDate = LocalDate.parse(unfinishedDf.startPointDate, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))

                            if (selectedDate > LocalDate.now()) {
                                showErrorMessage("You cannot choose future dates..", binding)
                            } else if (selectedDate < startPointDate) {
                                showErrorMessage("The chosen date must not be earlier than the start point (${unfinishedDf.startPointDate}) date!", binding)
                            } else {
                                tvHyphenFragAddExpense.visibility = View.VISIBLE
                                tvSelectedDateFragAddExpense.visibility = View.VISIBLE
                                tvSelectedDateFragAddExpense.text = selectedDate.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
                            }
                        }

                    }
                    it.show(childFragmentManager, "date_selection")
                }
            }

            btAddExpenseFragAddExpense.setOnClickListener {
                lifecycleScope.launch {
                    val expenseDate = if (rbCustomDateFragAddExpense.isChecked) {
                        tvSelectedDateFragAddExpense.text.toString()
                    } else {
                        LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
                    }

                    if (checkForEmptyColumns()) {
                        dateLimitViewModel.currentDateLimit.value?.let { currentDateLimit ->
                            dateFrameViewModel.unfinishedDateFrame.value?.let { unfinishedDf ->
                                if (isSingleExpense()) {
                                    transactionViewModel.upsertTransaction(
                                        SingleExpense(
                                            etAmountFragAddExpense.text.toString().toDouble(),
                                            actvCurrencyFragAddExpense.text.toString(),
                                            etExpenseCategoryFragAddExpense.text.toString(),
                                            etDescriptionFragAddExpense.text.toString(),
                                            currentDateLimit.date,
                                            currentDateLimit.dateLimitId!!,
                                            unfinishedDf.dateFrameId!!
                                        )
                                    )
                                    userViewModel.cacheNewExpenseCategory(etExpenseCategoryFragAddExpense.text.toString())
                                } else {
                                    val expenseCategoryList = getAllExpenseCategoryList()
                                    transactionViewModel.upsertTransaction(
                                        BatchExpense(
                                            etAmountFragAddExpense.text.toString().toDouble(),
                                            actvCurrencyFragAddExpense.text.toString(),
                                            expenseCategoryList,
                                            etDescriptionFragAddExpense.text.toString(),
                                            currentDateLimit.date,
                                            currentDateLimit.dateLimitId!!,
                                            unfinishedDf.dateFrameId!!
                                        )
                                    )
                                    userViewModel.cacheNewExpenseCategory(expenseCategoryList)
                                }
                                updateButtonToLoadingState()
                                dateFrameViewModel.updateExpenseAmount(etAmountFragAddExpense.text.toString().toDouble())
//                                dateLimitViewModel.setExpensesForDate()
//                                profileViewModel.setProfileWithDateFrames()
                                delay(1500) // JUST TO SIMULATE LOADING..
                                dateFrameViewModel.uiAnimState = false
                                dateLimitViewModel.uiAnimState = false
                                findNavController().navigate(AddExpenseFragmentDirections.actionAddExpenseFragmentToMainSession())
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun addNewItem () {
        val parentLayout = binding.parentContainerLayoutFragAddExpense
        val childView = LayoutInflater.from(requireContext()).inflate(R.layout.item_layout_transaction_category, parentLayout, false)
        val deleteButton = childView.findViewById<ImageView>(R.id.ivDeleteItemLayoutExpenseAndIncomeCategory)

        deleteButton.setOnClickListener { parentLayout.removeView(childView) }

        parentLayout.addView(childView)
    }

    private fun addNewItem (additionalExpenseCategoryList: List<String>) {
        val parentLayout = binding.parentContainerLayoutFragAddExpense
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

    private fun updateAddMoreButton () {
        binding.btAddMoreFragAddExpense.isClickable = binding.parentContainerLayoutFragAddExpense.childCount != 10
    }

    private fun checkForEmptyColumns (): Boolean {
        val isReady: Boolean
        val expenseAmount = binding.etAmountFragAddExpense.text.toString()
        val currency = binding.actvCurrencyFragAddExpense.text.toString()

        val initialExpenseCategory = binding.etExpenseCategoryFragAddExpense.text.toString()
        var isAdditionalExpenseCategoriesNotEmpty = true

        val parentLayout = binding.parentContainerLayoutFragAddExpense

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
            initialExpenseCategory.isEmpty() -> {
                showErrorMessage("Please write at least one expense category..", binding)
                isReady = false
            }
            !isAdditionalExpenseCategoriesNotEmpty -> {
                showErrorMessage("Please fill additionally added expense categories or delete unused ones..", binding)
                isReady = false
            }
            else -> {
                if (binding.rgDateSelectionFragAddExpense.checkedRadioButtonId == binding.rbCustomDateFragAddExpense.id) {
                    if (binding.tvSelectedDateFragAddExpense.text.isEmpty()) {
                        showErrorMessage("Please select an expense date..", binding)
                        isReady = false
                    } else {
                        isReady = true
                    }
                } else {
                    isReady = true
                }
            }
        }

        return isReady
    }

    private fun isSingleExpense (): Boolean {
        val parentLayout = binding.parentContainerLayoutFragAddExpense
        return parentLayout.childCount == 1
    }

    private fun getAdditionalExpenseCategoryList (): MutableList<String> {
        val parentLayout = binding.parentContainerLayoutFragAddExpense
        val expenseCategoryList: MutableList<String> = mutableListOf()

        for (i in 1 until parentLayout.childCount) {
            val childView = parentLayout.getChildAt(i) as ConstraintLayout
            val expenseCat = childView.findViewById<EditText>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

            expenseCategoryList.add(expenseCat.text.toString())
        }

        return expenseCategoryList
    }

    private fun getAllExpenseCategoryList (): MutableList<String> {
        val parentLayout = binding.parentContainerLayoutFragAddExpense
        val expenseCategoryList: MutableList<String> = mutableListOf()

        expenseCategoryList.add(binding.etExpenseCategoryFragAddExpense.text.toString())

        for (i in 1 until parentLayout.childCount) {
            val childView = parentLayout.getChildAt(i) as ConstraintLayout
            val expenseCat = childView.findViewById<EditText>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

            expenseCategoryList.add(expenseCat.text.toString())
        }

        return expenseCategoryList
    }

    private fun setCacheCategoryDropDownList (autoCompleteTextView: AutoCompleteTextView) {
        userViewModel.onlineUser.value?.let { onlineUser ->
            if (onlineUser.cachedExpenseCategories.isNotEmpty()) {
                val cachedCategoriesListAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.item_layout_cached_categories, R.id.tvCategoryName, onlineUser.cachedExpenseCategories.toTypedArray())
                autoCompleteTextView.setAdapter(cachedCategoriesListAdapter)
            }
        }
    }

    private fun showMaterialDatePickerDialog (callback: (MaterialDatePicker<Long>) -> Unit) {
        var materialDatePicker: MaterialDatePicker<Long>? = null
            materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select expense date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        callback (materialDatePicker)
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
                outState.putStringArrayList(ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY, ArrayList(getAdditionalExpenseCategoryList()))
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

    private fun updateButtonToLoadingState () {
        binding.apply {
            btAddExpenseFragAddExpense.apply {
                startAnimation(shrinkInsideAnim)
                isClickable = false
                visibility = View.INVISIBLE
            }
            ltLoadingFragAddExpense.visibility = View.VISIBLE
        }
    }

//    private fun showErrorMessage (message: String) {
//        Snackbar.make(binding.root,message, Snackbar.LENGTH_SHORT)
//            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
//            .setBackgroundTint(resources.getColor(R.color.bittersweet_red, null))
//            .show()
//    }
}