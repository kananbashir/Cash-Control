package com.example.cashcontrol.ui.fragment.news

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cashcontrol.adapter.NewsAdapter
import com.example.cashcontrol.data.network.NetworkResult
import com.example.cashcontrol.databinding.FragmentArticleSearchBinding
import com.example.cashcontrol.ui.viewmodel.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleSearchFragment : Fragment() {
    private lateinit var binding: FragmentArticleSearchBinding
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsViewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentArticleSearchBinding.inflate(layoutInflater)
        newsAdapter = NewsAdapter()
        newsViewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding.rvSearchResultsFragArticleSearch.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.svSearchFragArticleSearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                requestNewsWithQuery(query!!)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsViewModel.newsSearchResponseResult.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    binding.ltLoadingFragArticleSearch.visibility = View.INVISIBLE
                    binding.rvSearchResultsFragArticleSearch.visibility = View.VISIBLE
                    newsAdapter.differ.submitList(it.data!!.articles)
                }
                is NetworkResult.Loading -> {
                    binding.ltLoadingFragArticleSearch.visibility = View.VISIBLE
                    binding.rvSearchResultsFragArticleSearch.visibility = View.INVISIBLE
                }
                is NetworkResult.Error -> {
                    binding.ltLoadingFragArticleSearch.visibility = View.VISIBLE
                    binding.rvSearchResultsFragArticleSearch.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun requestNewsWithQuery (query: String) {
        newsViewModel.searchInNews(query, 1)
    }
}