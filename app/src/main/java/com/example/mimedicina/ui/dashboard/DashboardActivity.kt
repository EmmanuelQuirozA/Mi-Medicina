package com.example.mimedicina.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mimedicina.MiMedicinaApp
import com.example.mimedicina.databinding.ActivityDashboardBinding
import com.example.mimedicina.model.Medicine
import com.example.mimedicina.ui.medicines.MedicinesActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val app by lazy { application as MiMedicinaApp }

    private val profileId: Long by lazy { intent.getLongExtra(EXTRA_PROFILE_ID, -1L) }
    private val profileName: String by lazy { intent.getStringExtra(EXTRA_PROFILE_NAME) ?: "" }

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(app.medicinesRepository, profileId)
    }

    private val adapter: DashboardMedicinesAdapter by lazy {
        DashboardMedicinesAdapter(::onMedicineDismissed)
    }

    private val hiddenMedicineIds = mutableSetOf<Long>()
    private var allMedicines: List<Medicine> = emptyList()
    private var clockJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (profileId == -1L) {
            finish()
            return
        }

        if (savedInstanceState != null) {
            val hiddenIds = savedInstanceState.getLongArray(STATE_HIDDEN_MEDICINES)
            hiddenIds?.let { array ->
                hiddenMedicineIds.addAll(array.toList())
            }
        }

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeMedicines()
    }

    override fun onStart() {
        super.onStart()
        startClockUpdates()
    }

    override fun onStop() {
        super.onStop()
        clockJob?.cancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLongArray(STATE_HIDDEN_MEDICINES, hiddenMedicineIds.toLongArray())
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbarTitleTextView.text = profileName
        binding.toolbarBackTextView.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.medicinesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.medicinesRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.manageMedicinesButton.setOnClickListener {
            val intent = Intent(this, MedicinesActivity::class.java).apply {
                putExtra(MedicinesActivity.EXTRA_PROFILE_ID, profileId)
                putExtra(MedicinesActivity.EXTRA_PROFILE_NAME, profileName)
            }
            startActivity(intent)
        }
    }

    private fun observeMedicines() {
        lifecycleScope.launch {
            viewModel.medicines.collectLatest { medicines ->
                allMedicines = medicines
                updateVisibleMedicines()
            }
        }
    }

    private fun onMedicineDismissed(medicine: Medicine) {
        hiddenMedicineIds.add(medicine.id)
        updateVisibleMedicines()
    }

    private fun updateVisibleMedicines() {
        val visibleMedicines = allMedicines.filterNot { hiddenMedicineIds.contains(it.id) }
        binding.emptyStateTextView.isVisible = visibleMedicines.isEmpty()
        binding.medicinesRecyclerView.isVisible = visibleMedicines.isNotEmpty()
        adapter.submitList(visibleMedicines)
    }

    private fun startClockUpdates() {
        clockJob?.cancel()
        clockJob = lifecycleScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                binding.currentTimeTextView.text = formatTime(now)
                adapter.updateCurrentTime(now)
                delay(CLOCK_UPDATE_INTERVAL_MILLIS)
            }
        }
    }

    private fun formatTime(timeMillis: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(timeMillis)
    }

    companion object {
        const val EXTRA_PROFILE_ID = "dashboard_profile_id"
        const val EXTRA_PROFILE_NAME = "dashboard_profile_name"

        private const val STATE_HIDDEN_MEDICINES = "state_hidden_medicines"
        private const val CLOCK_UPDATE_INTERVAL_MILLIS = 1000L
    }
}
