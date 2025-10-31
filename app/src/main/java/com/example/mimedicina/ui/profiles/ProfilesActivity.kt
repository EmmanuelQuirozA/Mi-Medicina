package com.example.mimedicina.ui.profiles

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mimedicina.R
import com.example.mimedicina.databinding.ActivityProfilesBinding
import com.example.mimedicina.model.Profile

class ProfilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilesBinding
    private val profiles = mutableListOf<Profile>()
    private lateinit var adapter: ProfilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ProfilesAdapter(profiles)
        binding.profilesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.profilesRecyclerView.adapter = adapter

        binding.addProfileButton.setOnClickListener {
            showAddProfileDialog()
        }
    }

    private fun showAddProfileDialog() {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Add Profile")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val name = editText.text.toString()
                if (name.isNotEmpty()) {
                    profiles.add(Profile(name))
                    adapter.notifyItemInserted(profiles.size - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}