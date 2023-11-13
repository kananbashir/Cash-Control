package com.example.cashcontrol.adapter

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cashcontrol.R
import com.example.cashcontrol.data.entity.Profile
import com.example.cashcontrol.databinding.ItemLayoutProfilesBinding

class ProfilesAdapter: RecyclerView.Adapter<ProfilesAdapter.ProfilesViewHolder>() {

    val differ = AsyncListDiffer(this, getDifferCallback())

    inner class ProfilesViewHolder (val binding: ItemLayoutProfilesBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilesViewHolder {
        return ProfilesViewHolder(
            ItemLayoutProfilesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ProfilesViewHolder, position: Int) {
        val currentItem = differ.currentList[position]

        holder.binding.apply {
            if (currentItem.isOnline) {
                val isDarkMode = root.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                if (isDarkMode) {
                    root.setBackgroundColor(root.resources.getColor(R.color.white, null))
                    tvProfileNameItemLayoutProfiles.setTextColor(root.resources.getColor(R.color.midnight_green, null))
                    ivDeleteProfileItemLayoutProfiles.drawable.setTint(root.resources.getColor(R.color.midnight_green, null))
                } else {
                    root.setBackgroundColor(root.resources.getColor(R.color.midnight_green, null))
                    tvProfileNameItemLayoutProfiles.setTextColor(root.resources.getColor(R.color.white, null))
                    ivDeleteProfileItemLayoutProfiles.drawable.setTint(root.resources.getColor(R.color.white, null))
                }
            }

            tvProfileNameItemLayoutProfiles.text = currentItem.profileName
        }
    }

    private fun getDifferCallback (): DiffUtil.ItemCallback<Profile> {
        val differCallback = object : DiffUtil.ItemCallback<Profile>() {
            override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean {
                return oldItem.profileId == newItem.profileId
            }

            override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean {
                return oldItem == newItem
            }
        }

        return differCallback
    }
}