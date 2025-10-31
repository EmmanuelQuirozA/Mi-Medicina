package com.example.mimedicina.ui.profiles

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mimedicina.MiMedicinaApp
import com.example.mimedicina.R
import com.example.mimedicina.databinding.ActivityProfilesBinding
import com.example.mimedicina.model.Profile
import com.example.mimedicina.ui.dashboard.DashboardActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilesBinding
    private val app by lazy { application as MiMedicinaApp }

    private val viewModel: ProfilesViewModel by viewModels {
        ProfilesViewModel.Factory(app.profilesRepository)
    }

    private val adapter: ProfilesAdapter by lazy {
        ProfilesAdapter(
            onProfileSelected = ::openDashboard,
            onProfileLongPressed = ::confirmDeleteProfile
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupListeners()
        observeProfiles()
    }

    private fun setupRecyclerView() {
        binding.profilesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.profilesRecyclerView.adapter = adapter
        binding.profilesRecyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun setupListeners() {
        binding.addProfileButton.setOnClickListener {
            showAddProfileDialog()
        }
    }

    private fun observeProfiles() {
        lifecycleScope.launch {
            viewModel.profiles.collectLatest { profiles ->
                adapter.submitList(profiles)
            }
        }
        lifecycleScope.launch {
            viewModel.error.collectLatest { message ->
                message?.let {
                    Toast.makeText(this@ProfilesActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.consumeError()
                }
            }
        }
    }

    private fun showAddProfileDialog() {
        val input = EditText(this).apply {
            hint = getString(R.string.profile_name_hint)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.add_profile)
            .setView(input)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false
            input.doAfterTextChanged { text ->
                positiveButton.isEnabled = !text.isNullOrBlank()
                input.error = if (text.isNullOrBlank()) {
                    getString(R.string.error_empty_field)
                } else {
                    null
                }
            }
            positiveButton.setOnClickListener {
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.addProfile(name)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun openDashboard(profile: Profile) {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            putExtra(DashboardActivity.EXTRA_PROFILE_ID, profile.id)
            putExtra(DashboardActivity.EXTRA_PROFILE_NAME, profile.name)
        }
        startActivity(intent)
    }

    private fun confirmDeleteProfile(profile: Profile) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_profile)
            .setMessage(getString(R.string.confirm_delete_profile, profile.name))
            .setPositiveButton(R.string.accept) { _, _ ->
                lifecycleScope.launch {
                    val medicines = app.medicinesRepository.getMedicinesForProfile(profile.id)
                    medicines.forEach { medicine ->
                        app.alarmScheduler.cancel(medicine.id)
                    }
                    app.profilesRepository.removeProfile(profile)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
