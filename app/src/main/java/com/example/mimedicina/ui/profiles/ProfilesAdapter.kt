package com.example.mimedicina.ui.profiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mimedicina.databinding.ItemProfileBinding
import com.example.mimedicina.model.Profile

class ProfilesAdapter(private val profiles: List<Profile>) : RecyclerView.Adapter<ProfilesAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProfileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.profileNameTextView.text = profiles[position].name
    }

    override fun getItemCount() = profiles.size
}