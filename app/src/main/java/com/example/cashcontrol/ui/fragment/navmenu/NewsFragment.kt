package com.example.cashcontrol.ui.fragment.navmenu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.cashcontrol.adapter.NewsViewPagerAdapter
import com.example.cashcontrol.R
import com.example.cashcontrol.databinding.FragmentNewsBinding
import com.google.android.material.tabs.TabLayoutMediator

class NewsFragment : Fragment() {
    private lateinit var binding: FragmentNewsBinding
    private lateinit var newsViewPagerAdapter: NewsViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentNewsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        newsViewPagerAdapter = NewsViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)

        binding.viewPagerFragNews.adapter = newsViewPagerAdapter

        TabLayoutMediator(binding.tabLayoutFragNews, binding.viewPagerFragNews) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = resources.getString(R.string.text_tab_layout_news)
                    tab.setIcon(R.drawable.ic_articles)
                }

                1 -> {
                    tab.text = resources.getString(R.string.text_tab_layout_search)
                    tab.setIcon(R.drawable.ic_search)
                }

                else -> {
                    tab.text = "Tab $position"
                }
            }
        }.attach()

        return binding.root
    }
}