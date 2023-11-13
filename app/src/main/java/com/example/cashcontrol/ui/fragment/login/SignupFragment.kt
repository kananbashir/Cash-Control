package com.example.cashcontrol.ui.fragment.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cashcontrol.R
import com.example.cashcontrol.data.entity.User
import com.example.cashcontrol.databinding.FragmentSignupBinding
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.MessageUtil.showErrorMessage
import com.example.cashcontrol.util.constant.UIStateConstant.PASSWORD_INPUT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.PASSWORD_REENTER_INPUT_KEY
import com.example.cashcontrol.util.constant.UIStateConstant.USERNAME_INPUT_KEY
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider (requireActivity()).get(UserViewModel::class.java)
        binding = FragmentSignupBinding.inflate(layoutInflater)

        if (savedInstanceState != null) {
            binding.apply {
                etUsernameFragSignUp.setText(savedInstanceState.getString(USERNAME_INPUT_KEY))
                etPasswordFragSignUp.setText(savedInstanceState.getString(PASSWORD_INPUT_KEY))
                etReenterPasswordFragSignUp.setText(savedInstanceState.getString(PASSWORD_REENTER_INPUT_KEY))
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.apply {
            btSignUpFragSignUp.setOnClickListener {
                Log.i("TCPQQ","Button clicked")
                val username = binding.etUsernameFragSignUp.text.toString()
                val password = binding.etPasswordFragSignUp.text.toString()
                val passwordReenter = binding.etReenterPasswordFragSignUp.text.toString()

                if (username.isNotEmpty() && password.isNotEmpty() && passwordReenter.isNotEmpty()) {
                    if (password == passwordReenter) {
                        if (password.length > 5) {
                            userViewModel.isUsernameValid(username) { isValidUsername ->
                                Log.i("TCPQQ","Username is valid or not - $isValidUsername")
                                if (isValidUsername) {
                                    userViewModel.upsertUser(User(username, password, true, mutableSetOf(), mutableSetOf()))
                                    userViewModel.updateOnlineUser()
                                } else {
                                    showErrorMessage("This username has already been taken", binding)
                                }
                            }
                        } else {
                            showErrorMessage("The length of password should be longer than 5!", binding)
                        }
                    } else {
                        showErrorMessage("Passwords are not matching!", binding)
                    }
                } else if (username.isEmpty() || password.isEmpty() || passwordReenter.isEmpty()){
                    showErrorMessage("All columns must be filled!", binding)
                }
            }

            ivPasswordToShowFragSignUp.setOnClickListener { changePasswordVisibility("show") }
            ivPasswordToHideFragSignUp.setOnClickListener { changePasswordVisibility("hide") }

            tvSignInFragSignUp.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.onlineUser.collect {
                    it?.let {
                        delay(1000)
                        findNavController().navigate(SignupFragmentDirections.actionGlobalOnBoardingStartFragment())
                    }
                }
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(USERNAME_INPUT_KEY, binding.etUsernameFragSignUp.text.toString())
        outState.putString(PASSWORD_INPUT_KEY, binding.etPasswordFragSignUp.text.toString())
        outState.putString(PASSWORD_REENTER_INPUT_KEY, binding.etReenterPasswordFragSignUp.text.toString())

    }

    private fun changePasswordVisibility (state: String) {
        when (state) {
            "show" -> {
                binding.apply {
                    etPasswordFragSignUp.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    etReenterPasswordFragSignUp.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    ivPasswordToHideFragSignUp.visibility = View.VISIBLE
                    ivPasswordToShowFragSignUp.visibility = View.INVISIBLE
                }
            }
            "hide" -> {
                binding.apply {
                    etPasswordFragSignUp.transformationMethod = PasswordTransformationMethod.getInstance()
                    etReenterPasswordFragSignUp.transformationMethod = PasswordTransformationMethod.getInstance()
                    ivPasswordToShowFragSignUp.visibility = View.VISIBLE
                    ivPasswordToHideFragSignUp.visibility = View.INVISIBLE
                }
            }
        }
    }
}