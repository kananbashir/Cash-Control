package com.example.cashcontrol.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cashcontrol.ui.fragment.news.AllArticlesFragment
import com.example.cashcontrol.ui.fragment.news.ArticleSearchFragment

class NewsViewPagerAdapter(fragmentManager: FragmentManager, lifeCycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifeCycle) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllArticlesFragment()
            1 -> ArticleSearchFragment()
            else -> Fragment()
        }
    }
}