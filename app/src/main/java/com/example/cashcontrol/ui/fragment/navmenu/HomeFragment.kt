package com.example.cashcontrol.ui.fragment.navmenu

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.data.db.entity.DateLimit
import com.example.cashcontrol.data.db.entity.Transaction
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithDateLimits
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithTransactions
import com.example.cashcontrol.databinding.FragmentHomeBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.UIStateConstant
import com.example.cashcontrol.util.extension.convertDateFromIso8601To
import com.example.cashcontrol.util.extension.getCurrencySymbol
import com.example.cashcontrol.util.extension.sortTransactionsByDateDescending
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var transactionAdapter: TransactionAdapter
    private var unfinishedAndOnlineDateFrame: DateFrame? = null

    private val rotateOpenAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateCloseAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottomAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_anim) }
    private val toBottomAnim: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom_anim) }
    private var isFabExpanded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateLimitViewModel = ViewModelProvider(requireActivity())[DateLimitViewModel::class.java]
        dateFrameViewModel = ViewModelProvider(requireActivity())[DateFrameViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        transactionAdapter = TransactionAdapter()
        binding = FragmentHomeBinding.inflate(layoutInflater)

        binding.apply {
            if (transactionViewModel.newExpense) {
                ltLoadingSpentMoneyFragHome.visibility = View.VISIBLE
            } else {
                ltLoadingSpentMoneyFragHome.visibility = View.INVISIBLE
            }

            if (transactionViewModel.newIncome) {
                ltLoadingDailyLimitFragHome.visibility = View.VISIBLE
            } else {
                ltLoadingDailyLimitFragHome.visibility = View.INVISIBLE
            }

            savedInstanceState?.let {
                tvDailyLimitFragHome.text = it.getString(UIStateConstant.DAILY_LIMIT_VALUE_KEY)
                tvSpentMoneyFragHome.text = it.getString(UIStateConstant.TOTAL_SPENT_AMOUNT_KEY)
                tvWidgetIncomeNumberFragHome.text = it.getString(UIStateConstant.WIDGET_INCOME_AMOUNT_KEY)
                tvWidgetExpenseNumberFragHome.text = it.getString(UIStateConstant.WIDGET_EXPENSE_AMOUNT_KEY)
                tvWidgetSavingsNumberFragHome.text = it.getString(UIStateConstant.WIDGET_SAVINGS_AMOUNT_KEY)
                ltLoadingDailyLimitFragHome.visibility = it.getInt(UIStateConstant.LOADING_STATE_KEY)
                ltLoadingSpentMoneyFragHome.visibility = it.getInt(UIStateConstant.LOADING_STATE_KEY)
                progressBarExpenseFragHome.apply {
                    min = it.getInt(UIStateConstant.PROGRESS_BAR_MIN_KEY)
                    max = it.getInt(UIStateConstant.PROGRESS_BAR_MAX_KEY)
                    progress = it.getInt(UIStateConstant.PROGRESS_BAR_PROGRESS_KEY)
                }

                if (it.getInt(UIStateConstant.LOADING_STATE_KEY) == View.GONE) {
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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getUnfinishedAndOnlineDateFrame()?.let { unfinishedDateFrame ->
                    unfinishedAndOnlineDateFrame = unfinishedDateFrame
                    dateFrameViewModel.getDateFrameWithTransactions(unfinishedDateFrame.dateFrameId!!)
                        ?.let { dateFrameWithTransactions ->
                            setAdapterItems(dateFrameWithTransactions)

                            binding.tvStartPointDateFragHome.text = unfinishedDateFrame.startPointDate.convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                            binding.tvEndPointDateFragHome.text = unfinishedDateFrame.endPointDate.convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                            binding.tvCurrentDateFragHome.text = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.getDefault()))

                            if (savedInstanceState == null) {
                                dateFrameViewModel.getDateFrameWithDateLimits(unfinishedDateFrame.dateFrameId!!)?.let { dateFrameWithDateLimits ->
                                    val currentDateLimit = dateLimitViewModel.getCurrentDateLimitByDateFrame(unfinishedDateFrame.dateFrameId!!)

                                    when {
                                        currentDateLimit == null -> {
                                            createDateLimits(dateFrameWithDateLimits, dateFrameWithTransactions)
                                            transactionViewModel.newExpense = true
                                            transactionViewModel.newIncome = true

                                            delay(200)
                                            dateLimitViewModel.getCurrentDateLimitByDateFrame(unfinishedDateFrame.dateFrameId!!)
                                                ?.let { currentDl ->
                                                    setUIData(unfinishedDateFrame, currentDl)
                                                }
                                        }

                                        currentDateLimit.expenseLimit == 0.0 -> {
                                            dateLimitViewModel.setExpenseLimit(
                                                calculateExpenseLimitOf(
                                                    currentDateLimit,
                                                    unfinishedDateFrame,
                                                    dateFrameWithDateLimits.dateLimits,
                                                    dateFrameWithTransactions.transactions
                                                ),
                                                currentDateLimit
                                            )
                                            transactionViewModel.newExpense = true
                                            transactionViewModel.newIncome = true
                                            setUIData(unfinishedDateFrame, currentDateLimit)
                                        }

                                        else -> {
                                            setUIData(unfinishedDateFrame, currentDateLimit)
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }

        binding.apply {
            rvLastTransactionsFragHome.adapter = transactionAdapter
            rvLastTransactionsFragHome.layoutManager = LinearLayoutManager(requireContext())

            fabPlusFragHome.setOnClickListener {
                onFabButtonClicked()
            }

            fabAddExpenseFragHome.setOnClickListener {
                onFabButtonClicked()
                findNavController().navigate(HomeFragmentDirections.actionGlobalAddExpenseFragment())
            }

            fabAddIncomeFragHome.setOnClickListener {
                onFabButtonClicked()
                findNavController().navigate(HomeFragmentDirections.actionGlobalAddIncomeFragment())
            }

            fabCreateNewSessionFragHome.setOnClickListener {
                onFabButtonClicked()
                findNavController().navigate(HomeFragmentDirections.actionGlobalOnboardingSession2())
            }

            cvSeeAllFragHome.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.actionGlobalTransactionsDetailFragment())
            }
        }

        return binding.root
    }

    private fun createDateLimits(dateFrameWithDateLimits: DateFrameWithDateLimits, dateFrameWithTransactions: DateFrameWithTransactions) {
        unfinishedAndOnlineDateFrame?.let { unfinishedDateFrame ->
            if (!dateFrameViewModel.isDateFrameFinished(unfinishedDateFrame)) {
                val createdDateLimits = DateLimitCalculator()
                    .setDateFrame(unfinishedDateFrame)
                    .createSubsequentDateLimits(
                        dateFrameWithDateLimits.dateLimits,
                        dateFrameWithTransactions.transactions
                    )
                dateLimitViewModel.upsertAllDateLimits(*createdDateLimits.toTypedArray())
                transactionViewModel.newExpense = true
                transactionViewModel.newIncome = true
            } else {
                findNavController().navigate(HomeFragmentDirections.actionGlobalOnboardingSession3())
            }
        }
    }

    private suspend fun getUnfinishedAndOnlineDateFrame (): DateFrame? {
        userViewModel.getOnlineUser()?.let { onlineUser ->
            profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile.profileId!!)?.let { unfinishedDateFrame ->
                    return unfinishedDateFrame
                }
            }
        }
        return null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.apply {
            outState.putString(UIStateConstant.DAILY_LIMIT_VALUE_KEY, tvDailyLimitFragHome.text.toString())
            outState.putString(UIStateConstant.TOTAL_SPENT_AMOUNT_KEY, tvSpentMoneyFragHome.text.toString())
            outState.putString(UIStateConstant.WIDGET_INCOME_AMOUNT_KEY, tvWidgetIncomeNumberFragHome.text.toString())
            outState.putString(UIStateConstant.WIDGET_EXPENSE_AMOUNT_KEY, tvWidgetExpenseNumberFragHome.text.toString())
            outState.putString(UIStateConstant.WIDGET_SAVINGS_AMOUNT_KEY, tvWidgetSavingsNumberFragHome.text.toString())
            outState.putInt(UIStateConstant.LOADING_STATE_KEY, ltLoadingDailyLimitFragHome.visibility)
            outState.putInt(UIStateConstant.PROGRESS_BAR_MIN_KEY, progressBarExpenseFragHome.min)
            outState.putInt(UIStateConstant.PROGRESS_BAR_MAX_KEY, progressBarExpenseFragHome.max)
            outState.putInt(UIStateConstant.PROGRESS_BAR_PROGRESS_KEY, progressBarExpenseFragHome.progress)
        }
    }

    private fun calculateExpenseLimitOf(
        dateLimit: DateLimit,
        unfinishedDateFrame: DateFrame,
        allDateLimits: List<DateLimit>,
        allTransactions: List<Transaction>
    ): Double {
        val issueDate = LocalDate.parse(dateLimit.date, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1)
        val allExpenses = DateLimitCalculator.Util.extractExpensesFromTransactionList(allTransactions)
        val previousDayLimitExceedValue = DateLimitCalculator.Util.getPreviousDayLimitExceededValueFor(
            dateLimit,
            allDateLimits
        )

        return DateLimitCalculator()
            .setDateFrame(unfinishedDateFrame)
            .setPreviousDayLimitExceedValue(previousDayLimitExceedValue)
            .setIssueDate(issueDate)
            .setBonusExpenseValue(DateLimitCalculator.Util.calculateBonusExpenseValueOf(
                issueDate,
                allDateLimits,
                allExpenses
            ))
            .calculate()
    }

    private suspend fun getTotalExpenseForToday (dateLimit: DateLimit): Double {
        val dateLimitWithTransactions = dateLimitViewModel.getDateLimitWithTransactions(dateLimit.dateLimitId!!)
        if (dateLimitWithTransactions != null) {
            return dateLimitViewModel.getTotalExpensesForDate(dateLimitWithTransactions)
        }
        return 0.0
    }

    private suspend fun setUIData (unfinishedDateFrame: DateFrame, currentDateLimit: DateLimit) {
        val totalExpenseForCurrentDailyLimit = getTotalExpenseForToday(currentDateLimit)
        val initialBudgetWithIncome = unfinishedDateFrame.initialBudget + unfinishedDateFrame.totalIncomeOfAll
        val currencySymbol = unfinishedDateFrame.mainCurrency.getCurrencySymbol()
        binding.apply {
            lifecycleScope.launch {
                if (transactionViewModel.newExpense) {
                    delay(700) // JUST TO SIMULATE LOADING..
                    ltLoadingSpentMoneyFragHome.visibility = View.INVISIBLE
                    startCounterAnimation(1,unfinishedDateFrame.totalExpenseOfAll.toInt(),1000,currencySymbol,tvWidgetExpenseNumberFragHome)
                    startCounterAnimationWithProgressBar(
                        0,
                        totalExpenseForCurrentDailyLimit.toInt(),
                        1200,
                        currentDateLimit.expenseLimit,
                        currencySymbol,
                        binding.tvSpentMoneyFragHome,
                        binding.progressBarExpenseFragHome
                    )
                } else {
                    tvSpentMoneyFragHome.text = "${totalExpenseForCurrentDailyLimit.toInt()}$currencySymbol"
                    progressBarExpenseFragHome.apply {
                        min = 0
                        max = currentDateLimit.expenseLimit.toInt()
                        progress = totalExpenseForCurrentDailyLimit.toInt()
                    }
                }
            }

            lifecycleScope.launch {
                if (transactionViewModel.newIncome) {
                    delay(700) // JUST TO SIMULATE LOADING..
                    ltLoadingDailyLimitFragHome.visibility = View.INVISIBLE
                    startCounterAnimation(2,currentDateLimit.expenseLimit.toInt(),1300,currencySymbol,tvDailyLimitFragHome)
                    startCounterAnimation(1,initialBudgetWithIncome.toInt(),1000,currencySymbol,tvWidgetIncomeNumberFragHome)
                    startCounterAnimation(1,unfinishedDateFrame.savedMoney.toInt(),1000,currencySymbol,tvWidgetSavingsNumberFragHome)
                } else {
                    tvDailyLimitFragHome.text = "${currentDateLimit.expenseLimit.toInt()}$currencySymbol"
                    tvWidgetIncomeNumberFragHome.text = "${initialBudgetWithIncome.toInt()}$currencySymbol"
                    tvWidgetSavingsNumberFragHome.text = "${unfinishedDateFrame.savedMoney.toInt()}$currencySymbol"
                    tvWidgetExpenseNumberFragHome.text = "${unfinishedDateFrame.totalExpenseOfAll.toInt()}$currencySymbol"
                }

                transactionViewModel.newExpense = false
                transactionViewModel.newIncome = false
            }

        }

    }

    private fun setAdapterItems (dateFrameWithTransactions: DateFrameWithTransactions) {
        val transactions = dateFrameWithTransactions.transactions
        binding.apply {
            if (transactions.isNotEmpty()) {
                transactionAdapter.differ.submitList(transactions.sortTransactionsByDateDescending())
                tvNoExpenseDataFragHome.visibility = View.GONE
                cvSeeAllFragHome.visibility = View.VISIBLE
                cvSeeAllFragHome.isClickable = true
                rvLastTransactionsFragHome.visibility = View.VISIBLE
            } else {
                tvNoExpenseDataFragHome.visibility = View.VISIBLE
                cvSeeAllFragHome.visibility = View.GONE
                cvSeeAllFragHome.isClickable = false
                rvLastTransactionsFragHome.visibility = View.GONE
            }
        }
    }

    private fun onFabButtonClicked() {
        setFabVisibility()
        setFabAnimation()
        setClickable()
        isFabExpanded = !isFabExpanded
    }

    private fun setFabVisibility() {
        binding.apply {
            if (!isFabExpanded) {
                fabCreateNewSessionFragHome.visibility = View.VISIBLE
                fabAddExpenseFragHome.visibility = View.VISIBLE
                fabAddIncomeFragHome.visibility = View.VISIBLE
            } else {
                fabCreateNewSessionFragHome.visibility = View.INVISIBLE
                fabAddExpenseFragHome.visibility = View.INVISIBLE
                fabAddIncomeFragHome.visibility = View.INVISIBLE
            }
        }
    }

    private fun setFabAnimation() {
        binding.apply {
            if (!isFabExpanded) {
                fabCreateNewSessionFragHome.startAnimation(fromBottomAnim)
                fabAddExpenseFragHome.startAnimation(fromBottomAnim)
                fabAddIncomeFragHome.startAnimation(fromBottomAnim)
                fabPlusFragHome.startAnimation(rotateOpenAnim)
            } else {
                fabCreateNewSessionFragHome.startAnimation(toBottomAnim)
                fabAddExpenseFragHome.startAnimation(toBottomAnim)
                fabAddIncomeFragHome.startAnimation(toBottomAnim)
                fabPlusFragHome.startAnimation(rotateCloseAnim)
            }
        }
    }

    private fun setClickable() {
        binding.apply {
            if (!isFabExpanded) {
                fabCreateNewSessionFragHome.isClickable = true
                fabAddExpenseFragHome.isClickable = true
                fabAddIncomeFragHome.isClickable = true
            } else {
                fabCreateNewSessionFragHome.isClickable = false
                fabAddExpenseFragHome.isClickable = false
                fabAddIncomeFragHome.isClickable = false
            }
        }
    }

    private fun startCounterAnimation (startNo: Int, endNo: Int, duration: Long, currencySymbol: String, textView: TextView) {
        textView.visibility = View.VISIBLE
        val valueAnimator = ValueAnimator.ofInt(startNo, endNo)

        valueAnimator.duration = duration
        valueAnimator.addUpdateListener {
            textView.text = "${it.animatedValue}$currencySymbol"
        }
        valueAnimator.start()
    }

    private fun startCounterAnimationWithProgressBar (startNo: Int, endNo: Int, duration: Long, expenseLimit: Double, currencySymbol: String, textView: TextView, progressBar: ProgressBar) {
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
    }
}