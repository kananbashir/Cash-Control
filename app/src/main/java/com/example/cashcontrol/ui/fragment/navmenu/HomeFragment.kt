package com.example.cashcontrol.ui.fragment.navmenu

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashcontrol.R
import com.example.cashcontrol.adapter.TransactionAdapter
import com.example.cashcontrol.data.DateLimitCalculator
import com.example.cashcontrol.data.entity.DateFrame
import com.example.cashcontrol.data.entity.DateLimit
import com.example.cashcontrol.data.entity.Transaction
import com.example.cashcontrol.databinding.FragmentHomeBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.UIStateConstant.DAILY_LIMIT_VALUE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.LOADING_STATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TOTAL_SPENT_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.WIDGET_EXPENSE_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.WIDGET_INCOME_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.WIDGET_SAVINGS_AMOUNT_KEY
import com.example.cashcontrol.util.extension.getCurrencySymbol
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    private val rotateOpenAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateCloseAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottomAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_anim) }
    private val toBottomAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom_anim) }
    private var isExpanded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateLimitViewModel = ViewModelProvider(requireActivity()).get(DateLimitViewModel::class.java)
        dateFrameViewModel = ViewModelProvider(requireActivity()).get(DateFrameViewModel::class.java)
        transactionViewModel = ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)
        transactionAdapter = TransactionAdapter()
        binding = FragmentHomeBinding.inflate(layoutInflater)

        savedInstanceState?.let { bundle ->
            binding.apply {
                tvDailyLimitFragHome.text = bundle.getString(DAILY_LIMIT_VALUE_KEY)
                tvSpentMoneyFragHome.text = bundle.getString(TOTAL_SPENT_AMOUNT_KEY)
                tvWidgetIncomeNumberFragHome.text = bundle.getString(WIDGET_INCOME_AMOUNT_KEY)
                tvWidgetExpenseNumberFragHome.text = bundle.getString(WIDGET_EXPENSE_AMOUNT_KEY)
                tvWidgetSavingsNumberFragHome.text = bundle.getString(WIDGET_SAVINGS_AMOUNT_KEY)
                ltLoadingDailyLimitFragHome.visibility = bundle.getInt(LOADING_STATE_KEY)
                ltLoadingSpentMoneyFragHome.visibility = bundle.getInt(LOADING_STATE_KEY)
                if (bundle.getInt(LOADING_STATE_KEY) == View.GONE) {
                    tvDailyLimitFragHome.visibility = View.VISIBLE
                    tvSpentMoneyFragHome.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.apply {

            rvLastTransactionsFragHome.adapter = transactionAdapter
            rvLastTransactionsFragHome.layoutManager = LinearLayoutManager(requireContext())

            fabPlusFragHome.setOnClickListener {
                onAddButtonClicked()
            }

            fabAddExpenseFragHome.setOnClickListener {
                onAddButtonClicked()
                findNavController().navigate(HomeFragmentDirections.actionGlobalAddExpenseFragment())
            }

            fabAddIncomeFragHome.setOnClickListener {
                onAddButtonClicked()
                findNavController().navigate(HomeFragmentDirections.actionGlobalAddIncomeFragment())
            }

            cvSeeAllFragHome.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.actionGlobalExpensesDetailFragment2())
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateLimitViewModel.emittingComplete.observe(viewLifecycleOwner) { complete ->
            if (complete) {
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        dateFrameViewModel.unfinishedDateFrame.zip(dateLimitViewModel.currentDateLimit) { unfinishedDf, currentDateLimit ->
                            unfinishedDf?.let { dateFrame ->
                                if (savedInstanceState == null) {
                                    binding.tvStartPointDateFragHome.text = dateFrame.startPointDate
                                    binding.tvEndPointDateFragHome.text = dateFrame.endPointDate
                                    binding.tvCurrentDateFragHome.text = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN))

                                    when {
                                        currentDateLimit == null -> {
                                            dateLimitViewModel.upsertDateLimit(
                                                DateLimit(
                                                    LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN)),
                                                    dateFrame.dateFrameId!!,
                                                    0.0,
                                                    0.0)
                                            )
                                            delay(350) // JUST FOR SIMULATE LOADING..
                                        }

                                        currentDateLimit.expenseLimit == 0.0 -> {
                                            dateLimitViewModel.setExpenseLimit(calculateExpenseLimit(dateFrame))
                                            delay(350) // JUST FOR SIMULATE LOADING..
                                        }

                                        else -> {
                                            val totalExpenseForCurrentDailyLimit = getTotalExpenseForToday()
                                            val initialBudgetWithIncome = dateFrame.initialBudget + dateFrame.totalIncomeOfAll
                                            val currencySymbol = dateFrame.mainCurrency.getCurrencySymbol()
                                            delay(800) // JUST FOR SIMULATE LOADING..
                                            setLoadingAnimation(false)
                                            startCounterAnimation(2, currentDateLimit.expenseLimit.toInt(), 2000, currencySymbol, binding.tvDailyLimitFragHome)
                                            startCounterAnimation(1, initialBudgetWithIncome.toInt(), 1000, currencySymbol, binding.tvWidgetIncomeNumberFragHome)
                                            startCounterAnimation(1, dateFrame.totalExpenseOfAll.toInt(), 1000, currencySymbol, binding.tvWidgetExpenseNumberFragHome)
                                            startCounterAnimation(1, dateFrame.savedMoney.toInt(), 1000, currencySymbol, binding.tvWidgetSavingsNumberFragHome)
                                            startCounterAnimationWithProgressBar(
                                                0,
                                                totalExpenseForCurrentDailyLimit.toInt(),
                                                1000,
                                                currentDateLimit.expenseLimit,
                                                currencySymbol,
                                                binding.tvSpentMoneyFragHome,
                                                binding.progressBarExpenseFragHome
                                            )
                                            dateLimitViewModel.uiAnimState = true
                                            dateFrameViewModel.uiAnimState = true
                                        }
                                    }
                                }
                            }
                        }.collect()
                    }
                }
            }
        }

//        viewLifecycleOwner.lifecycleScope.launch {
//            transactionViewModel.sortedAllTransactions.observe(viewLifecycleOwner) {
//                if (it.isNotEmpty()) {
//                    transactionAdapter.differ.submitList(it)
//                    binding.tvNoExpenseDataFragHome.visibility = View.GONE
//                    binding.rvLastTransactionsFragHome.visibility = View.VISIBLE
//                    binding.cvSeeAllFragHome.isClickable = true
//                }
//            }
//        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                transactionViewModel.allTransactions.collect {
                    if (dateFrameViewModel.dateFrameWithTransactions.isNotEmpty()) {
                        transactionAdapter.differ.submitList(dateFrameViewModel.getSortedTransactions())
                        binding.tvNoExpenseDataFragHome.visibility = View.GONE
                        binding.rvLastTransactionsFragHome.visibility = View.VISIBLE
                        binding.cvSeeAllFragHome.isClickable = true
                    }
                }
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.apply {
            outState.putString(DAILY_LIMIT_VALUE_KEY, tvDailyLimitFragHome.text.toString())
            outState.putString(TOTAL_SPENT_AMOUNT_KEY, tvSpentMoneyFragHome.text.toString())
            outState.putString(WIDGET_INCOME_AMOUNT_KEY, tvWidgetIncomeNumberFragHome.text.toString())
            outState.putString(WIDGET_EXPENSE_AMOUNT_KEY, tvWidgetExpenseNumberFragHome.text.toString())
            outState.putString(WIDGET_SAVINGS_AMOUNT_KEY, tvWidgetSavingsNumberFragHome.text.toString())
            outState.putInt(LOADING_STATE_KEY, ltLoadingDailyLimitFragHome.visibility)
        }
    }

    private fun calculateExpenseLimit(unfinishedDateFrame: DateFrame): Double {
        return DateLimitCalculator(
            unfinishedDateFrame.totalBudget,
            unfinishedDateFrame.remainingBudget,
            dateFrameViewModel.getPreviousDayLimitExceedValue(),
            unfinishedDateFrame.startPointDate,
            unfinishedDateFrame.endPointDate
        ).calculate()
    }

    private fun getTotalExpenseForToday (): Double {
        if (dateLimitViewModel.dateLimitWithTransactions.isNotEmpty()) {
            var allExpenses = listOf<Transaction>()
            dateLimitViewModel.getExpensesForCurrentDate {
                allExpenses = it
            }
            var totalExpense = 0.0
            for (expenses in allExpenses) {
                totalExpense += expenses.transactionAmount
            }
            return totalExpense
        }
        return 0.0
    }

    private fun setLoadingAnimation (condition: Boolean) {
        binding.apply {
            when (condition) {
                true -> {
                    ltLoadingDailyLimitFragHome.visibility = View.VISIBLE
                    ltLoadingSpentMoneyFragHome.visibility = View.VISIBLE
                }
                else -> {
                    ltLoadingDailyLimitFragHome.visibility = View.GONE
                    ltLoadingSpentMoneyFragHome.visibility = View.GONE
                }
            }
        }
    }

    private fun onAddButtonClicked() {
        setFabVisibility(isExpanded)
        setFabAnimation(isExpanded)
        isExpanded = !isExpanded
    }

    private fun setFabVisibility(isExpanded: Boolean) {
        if (!isExpanded) {
            binding.fabCreateNewSessionFragHome.visibility = View.VISIBLE
            binding.fabAddExpenseFragHome.visibility = View.VISIBLE
            binding.fabAddIncomeFragHome.visibility = View.VISIBLE
        } else {
            binding.fabCreateNewSessionFragHome.visibility = View.INVISIBLE
            binding.fabAddExpenseFragHome.visibility = View.INVISIBLE
            binding.fabAddIncomeFragHome.visibility = View.INVISIBLE
        }
    }

    private fun setFabAnimation(isExpanded: Boolean) {
        if (!isExpanded) {
            binding.fabPlusFragHome.startAnimation(rotateOpenAnim)
            binding.fabCreateNewSessionFragHome.startAnimation(fromBottomAnim)
            binding.fabAddExpenseFragHome.startAnimation(fromBottomAnim)
            binding.fabAddIncomeFragHome.startAnimation(fromBottomAnim)
        } else {
            binding.fabPlusFragHome.startAnimation(rotateCloseAnim)
            binding.fabCreateNewSessionFragHome.startAnimation(toBottomAnim)
            binding.fabAddExpenseFragHome.startAnimation(toBottomAnim)
            binding.fabAddIncomeFragHome.startAnimation(toBottomAnim)
        }
    }

    private fun startCounterAnimation (startNo: Int, endNo: Int, duration: Long, currencySymbol: String, textView: TextView) {
        if (!dateFrameViewModel.uiAnimState && !dateLimitViewModel.uiAnimState) {
            textView.visibility = View.VISIBLE
            val valueAnimator = ValueAnimator.ofInt(startNo, endNo)
            valueAnimator.duration = duration
            valueAnimator.addUpdateListener {
                textView.text = "${it.animatedValue}$currencySymbol"
            }
            valueAnimator.start()
        } else {
            textView.text = "$endNo$currencySymbol"
        }
    }

    private fun startCounterAnimationWithProgressBar (startNo: Int, endNo: Int, duration: Long, expenseLimit: Double, currencySymbol: String, textView: TextView, progressBar: ProgressBar) {
        if (!dateFrameViewModel.uiAnimState && !dateLimitViewModel.uiAnimState) {
            textView.visibility = View.VISIBLE
            val valueAnimator = ValueAnimator.ofInt(startNo, endNo)
            valueAnimator.duration = duration
            valueAnimator.addUpdateListener {
                textView.text = "${it.animatedValue}$currencySymbol"
                progressBar.apply {
                    min = 0
                    max = expenseLimit.toInt()
                    progress = it.animatedValue.toString().toInt()
                }
            }
            valueAnimator.start()
        } else {
            progressBar.apply {
                min = 0
                max = expenseLimit.toInt()
                progress = endNo
            }
            textView.text = "$endNo$currencySymbol"
        }
    }
}