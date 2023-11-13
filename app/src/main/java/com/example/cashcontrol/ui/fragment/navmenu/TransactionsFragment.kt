package com.example.cashcontrol.ui.fragment.navmenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashcontrol.R
import com.example.cashcontrol.adapter.DateFramesAdapter
import com.example.cashcontrol.adapter.listener.DateFramesClickListener
import com.example.cashcontrol.data.db.entity.DateFrame
import com.example.cashcontrol.databinding.FragmentTransactionsBinding
import com.example.cashcontrol.ui.viewmodel.DateFrameViewModel
import com.example.cashcontrol.ui.viewmodel.ProfileViewModel
import com.example.cashcontrol.ui.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsFragment : Fragment(), DateFramesClickListener {
    private lateinit var binding: FragmentTransactionsBinding
    private lateinit var dateFrameViewModel: DateFrameViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var dateFramesAdapter: DateFramesAdapter
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateFrameViewModel = ViewModelProvider(requireActivity()).get(DateFrameViewModel::class.java)
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        transactionViewModel = ViewModelProvider(requireActivity()).get(TransactionViewModel::class.java)
        dateFramesAdapter = DateFramesAdapter()
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

//        viewLifecycleOwner.lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                transactionViewModel.sortedAllTransactions.observe(viewLifecycleOwner) {
//                    if (profileViewModel.profileWithDateFrames.isNotEmpty()) {
//                        dateFramesAdapter.differ.submitList(profileViewModel.profileWithDateFrames.first().dateFrames)
//                    }
//                }
//            }
//        }

        return  binding.root
    }

    override fun onDateFrameClicked(dateFrame: DateFrame) {

    }
}