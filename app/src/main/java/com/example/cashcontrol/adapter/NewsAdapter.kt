package com.example.cashcontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.cashcontrol.data.model.Article
import com.example.cashcontrol.databinding.ItemLayoutArticleBinding

class NewsAdapter: RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    val differ = AsyncListDiffer(this, getDifferCallback())

    inner class NewsViewHolder(val binding: ItemLayoutArticleBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder(ItemLayoutArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = differ.currentList[position]

        holder.binding.apply {
            tvTitleItemLayoutArticle.text = currentItem.title
            tvAuthorItemLayoutArticle.text = currentItem.author
            tvPublishDateItemLayoutArticle.text = currentItem.publishedAt
            currentItem.urlToImage?.let {
                ivNoPhotoItemLayoutArticle.visibility = View.INVISIBLE
                ivPhotoItemLayoutArticle.load(currentItem.urlToImage) {
                    crossfade(600)
                }
            }
        }
    }

    private fun getDifferCallback (): DiffUtil.ItemCallback<Article> {
        val differCallback = object: DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }
        }

        return differCallback
    }
}