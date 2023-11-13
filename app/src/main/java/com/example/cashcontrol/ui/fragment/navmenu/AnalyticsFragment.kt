package com.example.cashcontrol.ui.fragment.navmenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cashcontrol.databinding.FragmentAnalyticsBinding
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf

class AnalyticsFragment : Fragment() {
    private lateinit var binding: FragmentAnalyticsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalyticsBinding.inflate(layoutInflater)

        val chartEntryModels = entryModelOf(entriesOf(3, 2, 2, 3, 1))
        binding.chartFragAnalytics.setModel(chartEntryModels)

        return binding.root
    }
}