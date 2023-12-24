package com.example.cashcontrol.ui.fragment.navmenu

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
import com.example.cashcontrol.adapter.DateFramesAdapter
import com.example.cashcontrol.adapter.listener.DateFramesClickListener
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.databinding.FragmentTransactionsBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import com.example.cashcontrol.ui.viewmodel.UserViewModel
import com.example.cashcontrol.util.extension.sortDateFramesByDateDescending
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsFragment : Fragment(), DateFramesClickListener {
    private lateinit var binding: FragmentTransactionsBinding
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var dateFramesAdapter: DateFramesAdapter
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateFrameViewModel = ViewModelProvider(requireActivity())[DateFrameViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        dateFramesAdapter = DateFramesAdapter()
        dateFramesAdapter.setListener(this)
        binding = FragmentTransactionsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.apply {
            rvAllTotalExpensesFragTransactions.adapter = dateFramesAdapter
            rvAllTotalExpensesFragTransactions.layoutManager = LinearLayoutManager(requireContext())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.getOnlineUser()?.let { onlineUser ->
                    profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                        profileViewModel.getProfileWithDateFrames(onlineProfile.profileId!!)?.let { profileWithDateFrames ->
                            val dateFrames = profileWithDateFrames.dateFrames.sortDateFramesByDateDescending()
                            dateFramesAdapter.differ.submitList(dateFrames)
                        }
                    }
                }
            }
        }

        return  binding.root
    }

    override fun onDateFrameClicked(dateFrame: DateFrame) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.getOnlineUser()?.let { onlineUser ->
                    profileViewModel.getOnlineProfileById(onlineUser.userId!!)?.let { onlineProfile ->
                        dateFrameViewModel.getOnlineDateFrameByProfile(onlineProfile.profileId!!)?.let { onlineDateFrame ->
                            dateFrameViewModel.setDateFrameOffline(onlineDateFrame)
                            dateFrameViewModel.setDateFrameOnline(dateFrame)
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }

    }
}