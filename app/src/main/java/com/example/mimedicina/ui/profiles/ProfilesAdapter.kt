package com.example.mimedicina.ui.profiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mimedicina.databinding.ItemProfileBinding
import com.example.mimedicina.model.Profile

class ProfilesAdapter(
    private val onProfileSelected: (Profile) -> Unit,
    private val onProfileLongPressed: (Profile) -> Unit
) : ListAdapter<Profile, ProfilesAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = getItem(position)
        holder.bind(profile)
    }

    inner class ViewHolder(private val binding: ItemProfileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(profile: Profile) {
            binding.profileNameTextView.text = profile.name
            binding.root.setOnClickListener { onProfileSelected(profile) }
            binding.root.setOnLongClickListener {
                onProfileLongPressed(profile)
                true
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Profile>() {
        override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean =
            oldItem == newItem
    }
}
