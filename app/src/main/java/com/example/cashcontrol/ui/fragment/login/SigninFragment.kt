package com.example.cashcontrol.ui.fragment.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.databinding.FragmentSigninBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.constant.UIStateConstant.PASSWORD_INPUT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.USERNAME_INPUT_KEY
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SigninFragment : Fragment() {
    private lateinit var binding: FragmentSigninBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var dateFrameViewModel: DateFrameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider (requireActivity()).get(UserViewModel::class.java)
        profileViewModel = ViewModelProvider (requireActivity()).get(ProfileViewModel::class.java)
        dateFrameViewModel = ViewModelProvider (requireActivity()).get(DateFrameViewModel::class.java)

        binding = FragmentSigninBinding.inflate(layoutInflater)

        if (savedInstanceState != null) {
            binding.apply {
                etUsernameFragSignIn.setText(savedInstanceState.getString(USERNAME_INPUT_KEY))
                etPasswordFragSignIn.setText(savedInstanceState.getString(PASSWORD_INPUT_KEY))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.apply {
            btSignInFragSignIn.setOnClickListener {
                val username = binding.etUsernameFragSignIn.text.toString()
                val password = binding.etPasswordFragSignIn.text.toString()


//                if (username.isNotEmpty() && password.isNotEmpty()) {
//                    viewLifecycleOwner.lifecycleScope.launch {
//                        repeatOnLifecycle(Lifecycle.State.STARTED) {
//                            userViewModel.allUsers.zip(profileViewModel.allProfiles) { allUsers, allProfiles ->
//                                if (userViewModel.isUsernameAndPasswordAvailable(username, password, allUsers)) {
//                                    if (userViewModel.userWithProfiles.first().profiles.isNotEmpty()) {
//                                        profileViewModel.onlineProfile.zip(dateFrameViewModel.unfinishedDateFrame) { onlineProfile, unfinishedDf ->
//                                            if (onlineProfile == null) {
//                                                findNavController().navigate(SigninFragmentDirections.actionGlobalOnBoardingProfileFragment())
//                                            } else {
//                                                if (unfinishedDf == null) {
//                                                    findNavController().navigate(SigninFragmentDirections.actionGlobalOnboardingSession())
//                                                } else {
//                                                    findNavController().navigate(SigninFragmentDirections.actionGlobalMainSession())
//                                                }
//                                            }
//                                        }
//                                    } else {
//                                        findNavController().navigate(SigninFragmentDirections.actionGlobalOnBoardingStartFragment())
//                                    }
//                                } else {
//                                    showErrorMessage("Username or password wrong!")
//                                }
//                            }.collect()
//                        }
//                    }
//                } else if (username.isEmpty() || password.isEmpty()) {
//                    showErrorMessage("All columns must be filled")
//                }
            }

            ivPasswordToHideFragSignIn.setOnClickListener { changePasswordVisibility("hide") }
            ivPasswordToShowFragSignIn.setOnClickListener { changePasswordVisibility("show") }

            tvSignUpFragSignIn.setOnClickListener {
                findNavController().navigate(SigninFragmentDirections.actionSigninFragmentToSignupFragment())
            }
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(USERNAME_INPUT_KEY, binding.etUsernameFragSignIn.text.toString())
        outState.putString(PASSWORD_INPUT_KEY, binding.etPasswordFragSignIn.text.toString())

    }

    private fun changePasswordVisibility (state: String) {
        when (state) {
            "show" -> {
                binding.apply {
                    etPasswordFragSignIn.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    ivPasswordToHideFragSignIn.visibility = View.VISIBLE
                    ivPasswordToShowFragSignIn.visibility = View.INVISIBLE
                }
            }
            "hide" -> {
                binding.apply {
                    etPasswordFragSignIn.transformationMethod = PasswordTransformationMethod.getInstance()
                    ivPasswordToShowFragSignIn.visibility = View.VISIBLE
                    ivPasswordToHideFragSignIn.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun showErrorMessage (message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_SHORT)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
            .setBackgroundTint(resources.getColor(R.color.bittersweet_red, null))
            .show()
    }
}