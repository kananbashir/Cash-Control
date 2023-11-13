package com.example.cashcontrol.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.databinding.ActivityMainBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.DateLimitViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var dateLimitViewModel: DateLimitViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private var isLoading: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider (this).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider (this).get(ProfileViewModel::class.java)
        dateFrameViewModel = ViewModelProvider (this).get(DateFrameViewModel::class.java)
        dateLimitViewModel = ViewModelProvider (this).get(DateLimitViewModel::class.java)
        transactionViewModel = ViewModelProvider (this).get(TransactionViewModel::class.java)

        installSplashScreen().setKeepOnScreenCondition { isLoading }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel.checkOnlineUser {
            if (it != null) {
                if (userViewModel.userWithProfiles.isNotEmpty()) {
                    profileViewModel.checkOnlineProfile(userViewModel.userWithProfiles.first()) { onlineProfile ->
                        if (onlineProfile != null) {
                            if (profileViewModel.profileWithDateFrames.isNotEmpty()) {
                                dateFrameViewModel.checkUnfinishedDateFrame(profileViewModel.profileWithDateFrames.first()) { unfinishedDf ->
                                    if (unfinishedDf != null) {
                                        setNavigationGraph(R.id.main_session)
                                    } else {
                                        setNavigationGraph(R.id.onboarding_session)
                                    }
                                }
                            } else {
                                setNavigationGraph(R.id.onboarding_session)
                            }
                        } else {
                            setNavigationGraph(R.id.onBoardingProfileFragment)
                        }
                    }
                } else {
                    setNavigationGraph(R.id.onBoardingProfileFragment)
                }
            } else {
                setNavigationGraph(R.id.login_session)
            }
        }

        binding.apply {
            ivProfilesActivityMain.setOnClickListener {
                val navController = findNavController(R.id.fragmentContainerView)
                navController.navigateUp()
                navController.navigate(R.id.profileFragment)
            }
        }
    }

    private fun setNavigationGraph (startDestinationId: Int) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(startDestinationId)
        navController.graph = navGraph
        binding.bottomNavigationView.setupWithNavController(navController)

        isLoading = false

        val topDestinations = setOf(
            R.id.homeFragment,
            R.id.analyticsFragment,
            R.id.expenseFragment,
            R.id.newsFragment,
//                R.id.currencyConverterFragment // Unfortunately, there is no free currency converter API..
        )

        navController.addOnDestinationChangedListener { _,destination,_ ->
            if (destination.id in topDestinations) {
                binding.bottomNavigationView.visibility = View.VISIBLE
                binding.ivSettingsActivityMain.visibility = View.VISIBLE
                binding.ivProfilesActivityMain.visibility = View.VISIBLE
                binding.tvHelloUserActivityMain.visibility = View.VISIBLE
                binding.tvProfileNameActivityMain.visibility = View.VISIBLE
            } else {
                binding.bottomNavigationView.visibility = View.GONE
                binding.ivSettingsActivityMain.visibility = View.GONE
                binding.ivProfilesActivityMain.visibility = View.GONE
                binding.tvHelloUserActivityMain.visibility = View.GONE
                binding.tvProfileNameActivityMain.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}