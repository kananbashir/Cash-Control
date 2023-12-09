package com.example.cashcontrol.ui.fragment.drawers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
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
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showNotifyingMessage
import com.example.cashcontrol.util.constant.SettingsConstants.LANGUAGE_AZERBAIJANI
import com.example.cashcontrol.util.constant.SettingsConstants.LANGUAGE_ENGLISH
import com.example.cashcontrol.util.constant.SettingsConstants.LANGUAGE_TURKISH
import com.example.cashcontrol.util.constant.SettingsConstants.THEME_DARK
import com.example.cashcontrol.util.constant.SettingsConstants.THEME_LIGHT
import com.example.cashcontrol.util.constant.SettingsConstants.THEME_SYSTEM
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        dateFrameViewModel = ViewModelProvider(requireActivity()).get(DateFrameViewModel::class.java)
        binding = FragmentSettingsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dateFrameViewModel.allDateFrames.combine(profileViewModel.allProfiles) { _, _, ->
                    userViewModel.getOnlineUser()?.let { onlineUser ->
                        profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                            binding.tvProfileSettingProfileNameFragSettings.text = onlineProfile.profileName
                            dateFrameViewModel.getUnfinishedAndOnlineDateFrameByProfile(onlineProfile.profileId!!)?.let { unfinishedDf ->
                                binding.tvProfileSettingEndPointFragSettings.text = unfinishedDf.endPointDate
                                binding.tvProfileSettingSavingsFragSettings.text = "${unfinishedDf.savedMoney} ${unfinishedDf.mainCurrency.getCurrencyCode()}"
                                binding.tvProfileSettingMainCurrencyFragSettings.text = unfinishedDf.mainCurrency.getCurrencyCode()
                            }
                        }
                    }
                }.collect()
            }
        }

        val languagesAdapter = ArrayAdapter(requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.languages))
        binding.actvLanguageFragSettings.setAdapter(languagesAdapter)

        val themeAdapter = ArrayAdapter(requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.themes))
        binding.actvThemeFragSettings.setAdapter(themeAdapter)

        val currenciesAdapter = ArrayAdapter(requireContext(),
            R.layout.app_dropdown_item, resources.getStringArray(R.array.currencies))
        binding.actvMainCurrencyFragSettings.setAdapter(currenciesAdapter)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.themePreferenceFlow.combine(userViewModel.languagePreferenceFlow) { themePreference, languagePreference ->
                    when {
                        themePreference.equals(THEME_LIGHT, true) -> {
                            binding.actvThemeFragSettings.setText(THEME_LIGHT, false)
                            binding.tvAppSettingThemeFragSettings.text = THEME_LIGHT
                        }

                        themePreference.equals(THEME_DARK, true) -> {
                            binding.actvThemeFragSettings.setText(THEME_DARK, false)
                            binding.tvAppSettingThemeFragSettings.text = THEME_DARK
                        }

                        themePreference.equals(THEME_SYSTEM, true) -> {
                            binding.actvThemeFragSettings.setText(THEME_SYSTEM, false)
                            binding.tvAppSettingThemeFragSettings.text = THEME_SYSTEM
                        }
                    }

                    when {
                        languagePreference.equals(LANGUAGE_ENGLISH, true) -> {
                            val lang = resources.getStringArray(R.array.languages)[0]
                            binding.actvLanguageFragSettings.setText(lang, false)
                            binding.tvAppSettingLanguageFragSettings.text = lang
                        }

                        languagePreference.equals(LANGUAGE_AZERBAIJANI, true) -> {
                            val lang = resources.getStringArray(R.array.languages)[1]
                            binding.actvLanguageFragSettings.setText(lang, false)
                            binding.tvAppSettingLanguageFragSettings.text = lang
                        }

                        languagePreference.equals(LANGUAGE_TURKISH, true) -> {
                            val lang = resources.getStringArray(R.array.languages)[2]
                            binding.actvLanguageFragSettings.setText(lang, false)
                            binding.tvAppSettingLanguageFragSettings.text = lang
                        }
                    }
                }.collect()
            }
        }

        binding.apply {
            lybtLogOutFragSettings.setOnClickListener {
                val materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
                materialAlertDialogBuilder.setMessage(resources.getString(R.string.alert_message_settings_signout))
                    .setPositiveButton(resources.getString(R.string.alert_dialog_positive_yes)) { _, _, ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            repeatOnLifecycle(Lifecycle.State.STARTED) {
                                userViewModel.getOnlineUser()?.let { onlineUser ->
                                    userViewModel.setUserOffline(onlineUser)
                                    findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLoginSession())
                                }
                            }
                        }

                    }
                    .setNegativeButton(resources.getString(R.string.alert_dialog_negative_no)) { dialogInterface, _, -> dialogInterface.cancel() }
                    .show()
            }

            actvThemeFragSettings.setOnItemClickListener { parent, _, position, _ ->
                val selectedItem = parent.getItemAtPosition(position).toString()
                when {
                    selectedItem.equals(THEME_LIGHT, true) -> {
                        userViewModel.updateAppTheme(THEME_LIGHT)
                    }

                    selectedItem.equals(THEME_DARK, true) -> {
                        userViewModel.updateAppTheme(THEME_DARK)
                    }

                    selectedItem.equals(THEME_SYSTEM, true) -> {
                        userViewModel.updateAppTheme(THEME_SYSTEM)
                    }
                }
            }

            actvLanguageFragSettings.setOnItemClickListener { parent, _, position, _ ->
                when (parent.getItemAtPosition(position).toString()) {
                    resources.getStringArray(R.array.languages)[0] -> {
                        userViewModel.updateAppLanguage(LANGUAGE_ENGLISH)
                        updateLanguageTo(LANGUAGE_ENGLISH)
                        Toast.makeText(requireContext(), resources.getString(R.string.notifying_message_language_english), Toast.LENGTH_SHORT).show()
                    }
                    resources.getStringArray(R.array.languages)[1] -> {
                        userViewModel.updateAppLanguage(LANGUAGE_AZERBAIJANI)
                        updateLanguageTo(LANGUAGE_AZERBAIJANI)
                        Toast.makeText(requireContext(), resources.getString(R.string.notifying_message_language_azerbaijani), Toast.LENGTH_SHORT).show()
                    }
                    resources.getStringArray(R.array.languages)[2] -> {
                        userViewModel.updateAppLanguage(LANGUAGE_TURKISH)
                        updateLanguageTo(LANGUAGE_TURKISH)
                        Toast.makeText(requireContext(), resources.getString(R.string.notifying_message_language_turkish), Toast.LENGTH_SHORT).show()
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
                    .setPositiveButton(resources.getString(R.string.alert_dialog_positive_yes)) { _, _, ->
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

    private fun updateLanguageTo(languageCode: String) {
        when (languageCode) {
            LANGUAGE_ENGLISH -> {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            }

            LANGUAGE_AZERBAIJANI -> {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(LANGUAGE_AZERBAIJANI))
            }

            LANGUAGE_TURKISH -> {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(LANGUAGE_TURKISH))
            }
        }
    }

}