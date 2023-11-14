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
import com.example.cashcontrol.data.db.entity.income.Income
import com.example.cashcontrol.databinding.FragmentAddIncomeBinding
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
    private val shrinkInsideAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_inside) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionViewModel = ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)
        dateFrameViewModel = ViewModelProvider(requireActivity()).get(DateFrameViewModel::class.java)
        dateLimitViewModel = ViewModelProvider(requireActivity()).get(DateLimitViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        binding = FragmentAddIncomeBinding.inflate(layoutInflater)

        val currencyArrayAdapter = ArrayAdapter(requireContext(), R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies))
        binding.actvCurrencyFragAddIncome.setAdapter(currencyArrayAdapter)

        setCacheCategoryDropDownList(binding.etIncomeSourceFragAddIncome)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
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

            parentContainerLayoutFragAddIncome.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    val etIncomeCategory = child?.findViewById<AutoCompleteTextView>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)
                    setCacheCategoryDropDownList(etIncomeCategory!!)
                    updateAddMoreButton()
                }

                override fun onChildViewRemoved(parent: View?, child: View?) { updateAddMoreButton() }
            })

            btAddMoreFragAddIncome.setOnClickListener { addNewItem() }
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
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        getUnfinishedDateFrame()?.let { unfinishedDateFrame ->
                            showMaterialDatePickerDialog {
                                it.addOnPositiveButtonClickListener { selection ->
                                    val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()

                                    val startPointDate = LocalDate.parse(unfinishedDateFrame.startPointDate, DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))

                                    if (selectedDate > LocalDate.now()) {
                                        showErrorMessage("You cannot choose future dates..", binding)
                                    } else if (selectedDate < startPointDate) {
                                        showErrorMessage("The chosen date must not be earlier than the start point (${unfinishedDateFrame.startPointDate}) date!", binding)
                                    } else {
                                        tvHyphenFragHome.visibility = View.VISIBLE
                                        tvSelectedDateFragAddIncome.visibility = View.VISIBLE
                                        tvSelectedDateFragAddIncome.text = selectedDate.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))
                                    }
                                }
                                it.show(childFragmentManager, "date_selection")
                            }
                        }
                    }
                }
            }

            btAddIncomeFragAddIncome.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        getUnfinishedDateFrame()?.let { unfinishedDateFrame ->
                            dateLimitViewModel.getCurrentDateLimitByDateFrame(unfinishedDateFrame.dateFrameId!!)?.let { currentDateLimit ->
                                val dateLimitIdForSelectedExpenseDate = when {
                                    rbCustomDateFragAddIncome.isChecked -> {
                                        dateLimitViewModel.getDateLimitOfDateFrameByDate(
                                            unfinishedDateFrame.dateFrameId!!,
                                            tvSelectedDateFragAddIncome.text.toString()
                                        )
                                    }
                                    else -> { currentDateLimit }
                                }

                                if (checkForEmptyColumns()) {
                                    if (isSingleExpense()) {
                                        transactionViewModel.upsertTransaction(
                                            Income(
                                                etAmountFragAddIncome.text.toString().toDouble(),
                                                actvCurrencyFragAddIncome.text.toString(),
                                                etIncomeSourceFragAddIncome.text.toString(),
                                                mutableListOf(),
                                                etDescriptionFragAddIncome.text.toString(),
                                                dateLimitIdForSelectedExpenseDate?.dateLimitId!!,
                                                dateLimitIdForSelectedExpenseDate.date,
                                                unfinishedDateFrame.dateFrameId!!
                                            )
                                        )
                                        userViewModel.cacheNewIncomeSource(etIncomeSourceFragAddIncome.text.toString())
                                    } else {
                                        val incomeSourceList = getAllIncomeSourceList()
                                        transactionViewModel.upsertTransaction(
                                            Income(
                                                etAmountFragAddIncome.text.toString().toDouble(),
                                                actvCurrencyFragAddIncome.text.toString(),
                                                "",
                                                incomeSourceList,
                                                etDescriptionFragAddIncome.text.toString(),
                                                dateLimitIdForSelectedExpenseDate?.dateLimitId!!,
                                                dateLimitIdForSelectedExpenseDate.date,
                                                unfinishedDateFrame.dateFrameId!!
                                            )
                                        )
                                        userViewModel.cacheNewIncomeSource(incomeSourceList)
                                    }
                                    updateButtonToLoadingState()
                                    dateFrameViewModel.updateIncomeAmount(etAmountFragAddIncome.text.toString().toDouble(), unfinishedDateFrame)
                                    delay(1500) // JUST TO SIMULATE LOADING..
                                    findNavController().navigate(AddIncomeFragmentDirections.actionAddIncomeFragmentToMainSession())
                                }
                            }
                        }
                    }
                }
            }

        }

        return binding.root
    }

    private fun addNewItem () {
        val parentLayout = binding.parentContainerLayoutFragAddIncome
        val childView = LayoutInflater.from(requireContext()).inflate(R.layout.item_layout_transaction_category, parentLayout, false)
        val ivDelete = childView.findViewById<ImageView>(R.id.ivDeleteItemLayoutExpenseAndIncomeCategory)

        ivDelete.setOnClickListener { parentLayout.removeView(childView) }

        parentLayout.addView(childView)
    }

    private fun addNewItem (additionalIncomeCategories: List<String>) {
        val parentLayout = binding.parentContainerLayoutFragAddIncome
        lifecycleScope.launch {
            for (i in additionalIncomeCategories.indices) {

                delay(10) // I don't know why but it only works properly with 10ms or higher delay..
                val childView = LayoutInflater.from(requireContext()).inflate(R.layout.item_layout_transaction_category,  parentLayout, false)
                val deleteButton = childView.findViewById<ImageView>(R.id.ivDeleteItemLayoutExpenseAndIncomeCategory)
                val etExpenseCategory = childView.findViewById<AutoCompleteTextView>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

                etExpenseCategory.setText(additionalIncomeCategories[i])
                deleteButton.setOnClickListener { parentLayout.removeView(childView) }

                parentLayout.addView(childView)

            }
        }
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

    private fun setCacheCategoryDropDownList (autoCompleteTextView: AutoCompleteTextView) {
        if (userViewModel.cachedIncomeSources.isNotEmpty()) {
            val cachedCategoriesListAdapter = ArrayAdapter(
                requireContext(),
                R.layout.item_layout_cached_categories, R.id.tvCategoryName, userViewModel.cachedIncomeSources.toTypedArray()
            )
            autoCompleteTextView.setAdapter(cachedCategoriesListAdapter)
        }
    }

    private fun updateAddMoreButton () {
        binding.btAddMoreFragAddIncome.isClickable = binding.parentContainerLayoutFragAddIncome.childCount != 10
    }

    private fun checkForEmptyColumns (): Boolean {
        val isReady: Boolean
        val expenseAmount = binding.etAmountFragAddIncome.text.toString()
        val currency = binding.actvCurrencyFragAddIncome.text.toString()

        val initialExpenseCategory = binding.etIncomeSourceFragAddIncome.text.toString()
        var isAdditionalExpenseCategoriesNotEmpty = true

        val parentLayout = binding.parentContainerLayoutFragAddIncome

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
                if (binding.rgDateSelectionFragAddIncome.checkedRadioButtonId == binding.rbCustomDateFragAddIncome.id) {
                    if (binding.tvSelectedDateFragAddIncome.text.isEmpty()) {
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

    private fun getAdditionalIncomeSourceList (): MutableList<String> {
        val parentLayout = binding.parentContainerLayoutFragAddIncome
        val expenseCategoryList: MutableList<String> = mutableListOf()

        for (i in 1 until parentLayout.childCount) {
            val childView = parentLayout.getChildAt(i) as ConstraintLayout
            val expenseCat = childView.findViewById<EditText>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

            expenseCategoryList.add(expenseCat.text.toString())
        }

        return expenseCategoryList
    }

    private fun getAllIncomeSourceList (): MutableList<String> {
        val parentLayout = binding.parentContainerLayoutFragAddIncome
        val expenseCategoryList: MutableList<String> = mutableListOf()

        expenseCategoryList.add(binding.etIncomeSourceFragAddIncome.text.toString())

        for (i in 1 until parentLayout.childCount) {
            val childView = parentLayout.getChildAt(i) as ConstraintLayout
            val expenseCat = childView.findViewById<EditText>(R.id.etCategoryItemLayoutExpenseAndIncomeCategory)

            expenseCategoryList.add(expenseCat.text.toString())
        }

        return expenseCategoryList
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
                outState.putStringArrayList(ADDITIONAL_TRANSACTION_CATEGORY_LIST_KEY, ArrayList(getAdditionalIncomeSourceList()))
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

    private fun isSingleExpense (): Boolean {
        val parentLayout = binding.parentContainerLayoutFragAddIncome
        return parentLayout.childCount == 1
    }

    private fun showMaterialDatePickerDialog (callback: (MaterialDatePicker<Long>) -> Unit) {
        val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select income date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        callback (materialDatePicker)
    }

    private fun updateButtonToLoadingState () {
        binding.apply {
            btAddIncomeFragAddIncome.apply {
                startAnimation(shrinkInsideAnim)
                isClickable = false
                visibility = View.INVISIBLE
            }
            ltLoadingFragAddIncome.visibility = View.VISIBLE
        }
    }
}