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
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithDateLimits
import com.example.cashcontrol.data.db.entity.relation.DateFrameWithTransactions
import com.example.cashcontrol.databinding.FragmentHomeBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.UIStateConstant.DAILY_LIMIT_VALUE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.LOADING_STATE_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.TOTAL_SPENT_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.WIDGET_EXPENSE_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.WIDGET_INCOME_AMOUNT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.WIDGET_SAVINGS_AMOUNT_KEY
import com.example.cashcontrol.util.extension.getCurrencySymbol
import com.example.cashcontrol.util.extension.sortTransactionsByDateDescending
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
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
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        transactionViewModel = ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)
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
//                tvDailyLimitFragHome.text = savedInstanceState.getString(DAILY_LIMIT_VALUE_KEY)
//                tvSpentMoneyFragHome.text = savedInstanceState.getString(TOTAL_SPENT_AMOUNT_KEY)
//                tvWidgetIncomeNumberFragHome.text = savedInstanceState.getString(WIDGET_INCOME_AMOUNT_KEY)
//                tvWidgetExpenseNumberFragHome.text = savedInstanceState.getString(WIDGET_EXPENSE_AMOUNT_KEY)
//                tvWidgetSavingsNumberFragHome.text = savedInstanceState.getString(WIDGET_SAVINGS_AMOUNT_KEY)
//                ltLoadingDailyLimitFragHome.visibility = savedInstanceState.getInt(LOADING_STATE_KEY)
//                ltLoadingSpentMoneyFragHome.visibility = savedInstanceState.getInt(LOADING_STATE_KEY)
//                if (savedInstanceState.getInt(LOADING_STATE_KEY) == View.GONE) {
//                    tvDailyLimitFragHome.visibility = View.VISIBLE
//                    tvSpentMoneyFragHome.visibility = View.VISIBLE
//                }
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
                    dateFrameViewModel.getDateFrameWithTransactions(unfinishedDateFrame.dateFrameId!!)
                        ?.let { dateFrameWithTransactions ->
                            setAdapterItems(dateFrameWithTransactions)
                        }

                    if (savedInstanceState == null) {
                        binding.tvStartPointDateFragHome.text = unfinishedDateFrame.startPointDate
                        binding.tvEndPointDateFragHome.text = unfinishedDateFrame.endPointDate
                        binding.tvCurrentDateFragHome.text = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US))
                        val currentDateLimit = dateLimitViewModel.getCurrentDateLimitByDateFrame(unfinishedDateFrame.dateFrameId!!)

                        when {
                                currentDateLimit == null -> {
                                createDateLimits()

                                transactionViewModel.newExpense = true
                                transactionViewModel.newIncome = true

                                dateLimitViewModel.getCurrentDateLimitByDateFrame(unfinishedDateFrame.dateFrameId!!)?.let { currentDl ->
                                    dateFrameViewModel.getDateFrameWithDateLimits(unfinishedDateFrame.dateFrameId!!)?.let { dateFrameWithDateLimits ->
                                        dateLimitViewModel.setExpenseLimit(calculateExpenseLimit(unfinishedDateFrame, dateFrameWithDateLimits), currentDl)
                                        setUIData(
                                            unfinishedDateFrame,
                                            dateLimitViewModel.getCurrentDateLimitByDateFrame(unfinishedDateFrame.dateFrameId!!)!!
                                        )
                                    }
                                }
                            }

                            currentDateLimit.expenseLimit == 0.0 -> {
                                dateFrameViewModel.getDateFrameWithDateLimits(unfinishedDateFrame.dateFrameId!!)
                                    ?.let { dateFrameWithDateLimits ->
                                        dateLimitViewModel.setExpenseLimit(
                                            calculateExpenseLimit(
                                                unfinishedDateFrame,
                                                dateFrameWithDateLimits
                                            ), currentDateLimit
                                        )
                                        transactionViewModel.newExpense = true
                                        transactionViewModel.newIncome = true
                                        setUIData(unfinishedDateFrame, currentDateLimit)
                                    }
                            }

                            else -> {
                                setUIData(unfinishedDateFrame, currentDateLimit)
                            }
                        }
                    } else {
                        binding.apply {
                            tvDailyLimitFragHome.text = savedInstanceState.getString(DAILY_LIMIT_VALUE_KEY)
                            tvSpentMoneyFragHome.text = savedInstanceState.getString(TOTAL_SPENT_AMOUNT_KEY)
                            tvWidgetIncomeNumberFragHome.text = savedInstanceState.getString(WIDGET_INCOME_AMOUNT_KEY)
                            tvWidgetExpenseNumberFragHome.text = savedInstanceState.getString(WIDGET_EXPENSE_AMOUNT_KEY)
                            tvWidgetSavingsNumberFragHome.text = savedInstanceState.getString(WIDGET_SAVINGS_AMOUNT_KEY)
                            ltLoadingDailyLimitFragHome.visibility = savedInstanceState.getInt(LOADING_STATE_KEY)
                            ltLoadingSpentMoneyFragHome.visibility = savedInstanceState.getInt(LOADING_STATE_KEY)
                            if (savedInstanceState.getInt(LOADING_STATE_KEY) == View.GONE) {
                                tvDailyLimitFragHome.visibility = View.VISIBLE
                                tvSpentMoneyFragHome.visibility = View.VISIBLE
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

            fabCreateNewSessionFragHome.setOnClickListener {
                onAddButtonClicked()
                findNavController().navigate(HomeFragmentDirections.actionGlobalOnboardingSession2())
            }

            cvSeeAllFragHome.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.actionGlobalTransactionsDetailFragment())
            }
        }

        return binding.root
    }

    private suspend fun createDateLimits() {
        getUnfinishedAndOnlineDateFrame()?.let { unfinishedDateFrame ->
            dateFrameViewModel.getDateFrameWithDateLimits(unfinishedDateFrame.dateFrameId!!)?.let { dateFrameWithDateLimits ->
                val lastDate = dateFrameWithDateLimits.dateLimits.last().date
                val parsedLastDate = LocalDate.parse(
                    lastDate,
                    DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US)
                )
                val daysPassedFromLastDate = ChronoUnit.DAYS.between(
                    parsedLastDate,
                    LocalDate.now().plusDays(1)
                ).absoluteValue

                val dateLimitList: MutableList<DateLimit> = mutableListOf()

                for (i in 0 until daysPassedFromLastDate) {
                    val updatedDate = parsedLastDate.plusDays(i)

                    val dateLimit = DateLimit(
                        updatedDate.format(DateTimeFormatter.ofPattern(DATE_LIMIT_DATE_PATTERN, Locale.US)),
                        unfinishedDateFrame.dateFrameId!!,
                        0.0,
                        0.0
                    )
                    dateLimitList.add(dateLimit)
                }
                dateLimitViewModel.upsertAllDateLimits(*dateLimitList.toTypedArray())
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
            outState.putString(DAILY_LIMIT_VALUE_KEY, tvDailyLimitFragHome.text.toString())
            outState.putString(TOTAL_SPENT_AMOUNT_KEY, tvSpentMoneyFragHome.text.toString())
            outState.putString(WIDGET_INCOME_AMOUNT_KEY, tvWidgetIncomeNumberFragHome.text.toString())
            outState.putString(WIDGET_EXPENSE_AMOUNT_KEY, tvWidgetExpenseNumberFragHome.text.toString())
            outState.putString(WIDGET_SAVINGS_AMOUNT_KEY, tvWidgetSavingsNumberFragHome.text.toString())
            outState.putInt(LOADING_STATE_KEY, ltLoadingDailyLimitFragHome.visibility)
        }
    }

    private fun calculateExpenseLimit(unfinishedDateFrame: DateFrame, dateFrameWithDateLimits: DateFrameWithDateLimits): Double {
        return DateLimitCalculator(
            unfinishedDateFrame.totalBudget,
            unfinishedDateFrame.remainingBudget,
            dateFrameViewModel.getPreviousDayLimitExceedValue(dateFrameWithDateLimits),
            unfinishedDateFrame.startPointDate,
            unfinishedDateFrame.endPointDate
        ).calculate()
    }

    private suspend fun getTotalExpenseForToday (dateLimitId: Int): Double {
        val dateLimitWithTransactions = dateLimitViewModel.getDateLimitWithTransactions(dateLimitId)
        if (dateLimitWithTransactions != null) {
            val allExpenses = dateLimitViewModel.getExpensesForCurrentDate(dateLimitWithTransactions)
            var totalExpense = 0.0
            for (expenses in allExpenses) {
                totalExpense += expenses.transactionAmount
            }
            return totalExpense
        }
        return 0.0
    }

    private suspend fun setUIData (unfinishedDateFrame: DateFrame, currentDateLimit: DateLimit) {
        val totalExpenseForCurrentDailyLimit = getTotalExpenseForToday(currentDateLimit.dateLimitId!!)
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
                        1000,
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
                    startCounterAnimation(2,currentDateLimit.expenseLimit.toInt(),2000,currencySymbol,tvDailyLimitFragHome)
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
            binding.fabCreateNewSessionFragHome.visibility = View.GONE
            binding.fabAddExpenseFragHome.visibility = View.GONE
            binding.fabAddIncomeFragHome.visibility = View.GONE
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