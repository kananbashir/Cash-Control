package com.example.cashcontrol.ui.fragment.drawers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashcontrol.R
import com.example.cashcontrol.adapter.ProfilesAdapter
import com.example.cashcontrol.data.db.entity.Profile
import com.example.cashcontrol.databinding.FragmentProfileBinding
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var profilesAdapter: ProfilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        profilesAdapter = ProfilesAdapter()
        binding = FragmentProfileBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

//        binding.apply {
//
//            btCreateNewProfileFragProfile.setOnClickListener {
//                profileViewModel.onlineProfile.value?.let {
//                    profileViewModel.setProfileOffline(it)
//                    findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToOnBoardingProfileFragment())
//                }
//            }
//
//            rvProfilesFragProfile.apply {
//                adapter = profilesAdapter
//                layoutManager = LinearLayoutManager(requireContext())
//            }
//
//            viewLifecycleOwner.lifecycleScope.launch {
//                repeatOnLifecycle(Lifecycle.State.STARTED) {
//                    profileViewModel.allProfiles.collect {
//                        if (it.isNotEmpty()) {
//                            profilesAdapter.differ.submitList(it)
//                        }
//                    }
//                }
//            }
//
//        }

        return binding.root
    }
}