package com.example.cashcontrol.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.databinding.ActivityMainBinding
import com.example.cashcontrol.ui.fragment.navmenu.HomeFragmentDirections
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.constant.SettingsConstants.THEME_DARK
import com.example.cashcontrol.util.constant.SettingsConstants.THEME_LIGHT
import com.example.cashcontrol.util.constant.SettingsConstants.THEME_SYSTEM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private var loadingCounter: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                setTheme()
            }
        }
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider (this)[UserViewModel::class.java]
        profileViewModel = ViewModelProvider (this)[ProfileViewModel::class.java]
        dateFrameViewModel = ViewModelProvider (this)[DateFrameViewModel::class.java]

        installSplashScreen().setKeepOnScreenCondition { loadingCounter >= 2 }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getUserInformation()
            }
        }

        binding.apply {
            ivProfilesActivityMain.setOnClickListener {
                navController.navigate(HomeFragmentDirections.actionGlobalProfileFragment())
            }

            ivSettingsActivityMain.setOnClickListener {
                navController.navigate(HomeFragmentDirections.actionGlobalSettingsFragment())
            }
        }
    }
    private suspend fun getUserInformation() {
        val onlineUser = userViewModel.getOnlineUser()
        if (onlineUser != null) {
            val onlineProfile = profileViewModel.getOnlineProfileById(onlineUser.userId!!)
            if (onlineProfile != null) {
                val unfinishedDateFrame = dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile.profileId!!)
                if (unfinishedDateFrame != null) {
                    setNavigationGraph(R.id.main_session)
                } else {
                    setNavigationGraph(R.id.onboarding_session)
                }
            } else {
                setNavigationGraph(R.id.onBoardingStartFragment)
            }
        } else {
            setNavigationGraph(R.id.login_session)
        }
        loadingCounter++
    }

    private suspend fun setTheme() {
        withContext(Dispatchers.Main) {
            userViewModel.themePreferenceFlow.collect {
                when (it) {
                    THEME_LIGHT -> { AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO) }
                    THEME_DARK -> { AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES) }
                    THEME_SYSTEM -> { AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM) }
                }
                loadingCounter++
            }
        }
    }

    private fun setNavigationGraph (startDestinationId: Int) {
        val navHostFragmentTop = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragmentTop.navController
        navController = findNavController(R.id.fragmentContainerView)

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(startDestinationId)
        navController.graph = navGraph
        binding.bottomNavigationView.setupWithNavController(navController)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                userViewModel.allUsers.combine(profileViewModel.allProfiles) { _, _ ->
                    userViewModel.getOnlineUser()?.let { onlineUser ->
                        userViewModel.updateCachedCategories(onlineUser)
                        binding.tvHelloUserActivityMain.text = resources.getString(R.string.placeholder_text_hello, onlineUser.username)
                        profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                            binding.tvProfileNameActivityMain.text = onlineProfile.profileName
                        }
                    }
                }.collect()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}