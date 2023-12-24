package com.example.cashcontrol.ui.fragment.news

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashcontrol.adapter.NewsAdapter
import com.example.cashcontrol.data.network.NetworkResult
import com.example.cashcontrol.databinding.FragmentAllArticlesBinding
import com.example.cashcontrol.ui.viewmodel.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllArticlesFragment : Fragment() {
    private lateinit var binding: FragmentAllArticlesBinding
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private var page: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newsViewModel = ViewModelProvider(requireActivity())[NewsViewModel::class.java]
        newsAdapter = NewsAdapter()
        binding = FragmentAllArticlesBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        requestNewsData()

        binding.rvArticlesFragAllArticles.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsViewModel.newsResponseResult.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    binding.ltLoadingFragAllArticles.visibility = View.INVISIBLE
                    binding.rvArticlesFragAllArticles.visibility = View.VISIBLE
                    newsAdapter.differ.submitList(it.data!!.articles)
                }
                is NetworkResult.Loading -> {
                    binding.ltLoadingFragAllArticles.visibility = View.VISIBLE
                    binding.rvArticlesFragAllArticles.visibility = View.VISIBLE
                }
                is NetworkResult.Error -> {
                    binding.ltLoadingFragAllArticles.visibility = View.INVISIBLE
                    binding.rvArticlesFragAllArticles.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun requestNewsData() {
        newsViewModel.getNewsFromApi(page)
    }
}