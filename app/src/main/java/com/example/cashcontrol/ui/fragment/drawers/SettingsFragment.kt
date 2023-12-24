package com.example.cashcontrol.ui.fragment.drawers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.databinding.FragmentSettingsBinding
import com.example.cashcontrol.ui.fragment.bottomsheet.UpdateEndPointDateBottomSheetFragment
import com.example.cashcontrol.ui.fragment.bottomsheet.UpdateProfileNameBottomSheetFragment
import com.example.cashcontrol.ui.fragment.bottomsheet.UpdateSavingAmountBottomSheetFragment
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.CustomArrayAdapter
import com.example.cashcontrol.util.MessageUtil.showNotifyingMessage
import com.example.cashcontrol.util.constant.DateConstant.DATE_LIMIT_DATE_PATTERN
import com.example.cashcontrol.util.constant.SettingsConstants.LANGUAGE_AZERBAIJANI
import com.example.cashcontrol.util.constant.SettingsConstants.LANGUAGE_ENGLISH
import com.example.cashcontrol.util.constant.SettingsConstants.LANGUAGE_TURKISH
import com.example.cashcontrol.util.constant.SettingsConstants.THEME_DARK
import com.example.cashcontrol.util.constant.SettingsConstants.THEME_LIGHT
import com.example.cashcontrol.util.constant.SettingsConstants.THEME_SYSTEM
import com.example.cashcontrol.util.constant.UIStateConstant
import com.example.cashcontrol.util.extension.convertDateFromIso8601To
import com.example.cashcontrol.util.extension.getCurrencyCode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private var selectedTheme: Int = 0
    private var selectedLanguage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        dateFrameViewModel = ViewModelProvider(requireActivity())[DateFrameViewModel::class.java]
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        binding = FragmentSettingsBinding.inflate(layoutInflater)

        val languagesAdapter = CustomArrayAdapter(requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.languages))
        binding.actvLanguageFragSettings.setAdapter(languagesAdapter)

        val themeAdapter = CustomArrayAdapter(requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.themes))
        binding.actvThemeFragSettings.setAdapter(themeAdapter)

        val currenciesAdapter = CustomArrayAdapter(requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies))
        binding.actvMainCurrencyFragSettings.setAdapter(currenciesAdapter)

        savedInstanceState?.let {
            binding.apply {
                val theme = resources.getStringArray(R.array.themes)[it.getInt(UIStateConstant.SELECTED_THEME_KEY)]
                val language = resources.getStringArray(R.array.languages)[it.getInt(UIStateConstant.SELECTED_LANGUAGE_KEY)]
                actvThemeFragSettings.hint = theme
                tvAppSettingThemeFragSettings.text = theme
                actvLanguageFragSettings.hint = language
                tvAppSettingLanguageFragSettings.text = language
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dateFrameViewModel.allDateFrames.combine(profileViewModel.allProfiles) { _, _ ->
                    userViewModel.getOnlineUser()?.let { onlineUser ->
                        profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                            binding.tvProfileSettingProfileNameFragSettings.text = onlineProfile.profileName
                            dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile.profileId!!)?.let { unfinishedDf ->
                                binding.tvProfileSettingEndPointFragSettings.text = unfinishedDf.endPointDate.convertDateFromIso8601To(DATE_LIMIT_DATE_PATTERN)
                                binding.tvProfileSettingSavingsFragSettings.text = "${unfinishedDf.savedMoney} ${unfinishedDf.mainCurrency.getCurrencyCode()}"
                                binding.tvProfileSettingMainCurrencyFragSettings.text = unfinishedDf.mainCurrency.getCurrencyCode()
                            }
                        }
                    }
                }.collect()
            }
        }

        if (savedInstanceState == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    userViewModel.themePreferenceFlow.combine(userViewModel.languagePreferenceFlow) { themePreference, languagePreference ->
                        when {
                            themePreference.equals(THEME_LIGHT, true) -> {
                                val theme = resources.getStringArray(R.array.themes)[0]
                                binding.actvThemeFragSettings.hint = theme
                                binding.tvAppSettingThemeFragSettings.text = THEME_LIGHT
                            }

                            themePreference.equals(THEME_DARK, true) -> {
                                val theme = resources.getStringArray(R.array.themes)[1]
                                binding.actvThemeFragSettings.hint = theme
                                binding.tvAppSettingThemeFragSettings.text = THEME_DARK
                            }

                            themePreference.equals(THEME_SYSTEM, true) -> {
                                val theme = resources.getStringArray(R.array.themes)[2]
                                binding.actvThemeFragSettings.hint = theme
                                binding.tvAppSettingThemeFragSettings.text = THEME_SYSTEM
                            }
                        }

                        when {
                            languagePreference.equals(LANGUAGE_ENGLISH, true) -> {
                                val lang = resources.getStringArray(R.array.languages)[0]
                                binding.actvLanguageFragSettings.hint = lang
                                binding.tvAppSettingLanguageFragSettings.text = lang
                            }

                            languagePreference.equals(LANGUAGE_AZERBAIJANI, true) -> {
                                val lang = resources.getStringArray(R.array.languages)[1]
                                binding.actvLanguageFragSettings.hint = lang
                                binding.tvAppSettingLanguageFragSettings.text = lang
                            }

                            languagePreference.equals(LANGUAGE_TURKISH, true) -> {
                                val lang = resources.getStringArray(R.array.languages)[2]
                                binding.actvLanguageFragSettings.hint = lang
                                binding.tvAppSettingLanguageFragSettings.text = lang
                            }
                        }
                    }.collect()
                }
            }
        }

        binding.apply {
            lybtLogOutFragSettings.setOnClickListener {
                val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
                materialAlertDialogBuilder.setMessage(resources.getString(R.string.alert_message_settings_signout))
                    .setPositiveButton(resources.getString(R.string.alert_dialog_positive_yes)) { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            repeatOnLifecycle(Lifecycle.State.STARTED) {
                                userViewModel.getOnlineUser()?.let { onlineUser ->
                                    userViewModel.setUserOffline(onlineUser)
                                    transactionViewModel.newExpense = true
                                    transactionViewModel.newIncome = true
                                    findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLoginSession())
                                }
                            }
                        }

                    }
                    .setNegativeButton(resources.getString(R.string.alert_dialog_negative_no)) { dialogInterface, _ -> dialogInterface.cancel() }
                    .show()
            }

            actvThemeFragSettings.setOnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                when {
                    selectedItem.equals(THEME_LIGHT, true) -> {
                        selectedTheme = 0
                        userViewModel.updateAppTheme(THEME_LIGHT)
                    }

                    selectedItem.equals(THEME_DARK, true) -> {
                        selectedTheme = 1
                        userViewModel.updateAppTheme(THEME_DARK)
                    }

                    selectedItem.equals(THEME_SYSTEM, true) -> {
                        selectedTheme = 2
                        userViewModel.updateAppTheme(THEME_SYSTEM)
                    }
                }
            }

            actvLanguageFragSettings.setOnItemClickListener { parent, _, position, _ ->
                when (parent.getItemAtPosition(position).toString()) {
                    resources.getStringArray(R.array.languages)[0] -> {
                        selectedLanguage = 0
                        updateLanguageTo(LANGUAGE_ENGLISH)
                    }
                    resources.getStringArray(R.array.languages)[1] -> {
                        updateLanguageTo(LANGUAGE_AZERBAIJANI)
                        selectedLanguage = 1
                    }
                    resources.getStringArray(R.array.languages)[2] -> {
                        updateLanguageTo(LANGUAGE_TURKISH)
                        selectedLanguage = 2
                    }
                }
            }

            btProfileNameChangeFragSettings.setOnClickListener {
                UpdateProfileNameBottomSheetFragment().show(childFragmentManager, "updateProfileNameTag")
            }

            btEndPointChangeFragSettings.setOnClickListener {
                UpdateEndPointDateBottomSheetFragment().show(childFragmentManager, "updateEndPointDateTag")
            }

            btSavingsChangeFragSettings.setOnClickListener {
                UpdateSavingAmountBottomSheetFragment().show(childFragmentManager, "updateSavingAmountTag")
            }

            btClearCategoryHistoryFragSettings.setOnClickListener {
                val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
                materialAlertDialogBuilder.setMessage(resources.getString(R.string.alert_message_settings_clear_categories_and_sources))
                    .setPositiveButton(resources.getString(R.string.alert_dialog_positive_yes)) { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            repeatOnLifecycle(Lifecycle.State.STARTED) {
                                userViewModel.clearAllCategoriesAndSourcesForUser()
                                showNotifyingMessage(resources.getString(R.string.notifying_message_settings_clear_categories_and_sources), binding)
                            }
                        }
                    }
                    .setNegativeButton(resources.getString(R.string.alert_dialog_negative_no)) { dialogInterface, _ -> dialogInterface.cancel() }
                    .show()
            }

            ivReturnBackFragSettings.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.apply {
            outState.putInt(UIStateConstant.SELECTED_THEME_KEY, selectedTheme)
            outState.putInt(UIStateConstant.SELECTED_LANGUAGE_KEY, selectedLanguage)
        }

    }

    private fun updateLanguageTo(languageCode: String) {
        when (languageCode) {
            LANGUAGE_ENGLISH -> {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                userViewModel.updateAppLanguage(LANGUAGE_ENGLISH)
            }

            LANGUAGE_AZERBAIJANI -> {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(LANGUAGE_AZERBAIJANI))
                userViewModel.updateAppLanguage(LANGUAGE_AZERBAIJANI)
            }

            LANGUAGE_TURKISH -> {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(LANGUAGE_TURKISH))
                userViewModel.updateAppLanguage(LANGUAGE_TURKISH)
            }
        }
    }

}