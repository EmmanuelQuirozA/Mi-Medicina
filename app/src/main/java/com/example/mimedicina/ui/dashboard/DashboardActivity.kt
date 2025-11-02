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
import com.example.mimedicina.ui.common.applyEdgeToEdge
import com.example.mimedicina.ui.medicines.MedicinesActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val app by lazy { application as MiMedicinaApp }

    private val profileId: Long by lazy { intent.getLongExtra(EXTRA_PROFILE_ID, -1L) }
    private val profileName: String by lazy { intent.getStringExtra(EXTRA_PROFILE_NAME) ?: "" }

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(
            app.medicinesRepository,
            app.reminderHistoryRepository,
            app.alarmScheduler,
            profileId
        )
    }

    private val adapter: DashboardMedicinesAdapter by lazy {
        DashboardMedicinesAdapter(
            onMarkDone = ::onMedicineMarkedDone,
            onDismiss = ::onMedicineDismissed
        )
    }

    private val historyAdapter: ReminderHistoryAdapter by lazy {
        ReminderHistoryAdapter()
    }

    private val hiddenMedicineReminders = mutableMapOf<Long, Long>()
    private var allMedicines: List<Medicine> = emptyList()
    private var clockJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge(binding.root)

        if (profileId == -1L) {
            finish()
            return
        }

        if (savedInstanceState != null) {
            val hiddenIds = savedInstanceState.getLongArray(STATE_HIDDEN_MEDICINE_IDS)
            val hiddenTimes = savedInstanceState.getLongArray(STATE_HIDDEN_MEDICINE_TIMES)
            if (hiddenIds != null && hiddenTimes != null && hiddenIds.size == hiddenTimes.size) {
                hiddenMedicineReminders.clear()
                hiddenIds.indices.forEach { index ->
                    hiddenMedicineReminders[hiddenIds[index]] = hiddenTimes[index]
                }
            }
        }

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeMedicines()
        observeHistory()
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
        val size = hiddenMedicineReminders.size
        val ids = LongArray(size)
        val times = LongArray(size)
        var index = 0
        hiddenMedicineReminders.forEach { (id, reminderTime) ->
            ids[index] = id
            times[index] = reminderTime
            index++
        }
        outState.putLongArray(STATE_HIDDEN_MEDICINE_IDS, ids)
        outState.putLongArray(STATE_HIDDEN_MEDICINE_TIMES, times)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbarTitleTextView.text = profileName
        binding.toolbarBackTextView.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.medicinesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.medicinesRecyclerView.adapter = adapter
        binding.medicinesRecyclerView.isNestedScrollingEnabled = false

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter
        binding.historyRecyclerView.isNestedScrollingEnabled = false
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

    private fun observeHistory() {
        lifecycleScope.launch {
            viewModel.history.collectLatest { history ->
                binding.historyEmptyStateTextView.isVisible = history.isEmpty()
                binding.historyRecyclerView.isVisible = history.isNotEmpty()
                historyAdapter.submitList(history)
            }
        }
    }

    private fun onMedicineMarkedDone(medicine: Medicine) {
        hiddenMedicineReminders[medicine.id] = medicine.nextReminderTimeMillis
        viewModel.markReminderTaken(medicine)
        updateVisibleMedicines()
    }

    private fun onMedicineDismissed(medicine: Medicine) {
        hiddenMedicineReminders[medicine.id] = medicine.nextReminderTimeMillis
        viewModel.dismissReminder(medicine)
        updateVisibleMedicines()
    }

    private fun updateVisibleMedicines() {
        val medicinesById = allMedicines.associateBy { it.id }
        val iterator = hiddenMedicineReminders.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val medicine = medicinesById[entry.key]
            if (medicine == null || medicine.nextReminderTimeMillis != entry.value) {
                iterator.remove()
            }
        }
        val visibleMedicines = allMedicines.filterNot { medicine ->
            hiddenMedicineReminders[medicine.id] == medicine.nextReminderTimeMillis
        }
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

        private const val STATE_HIDDEN_MEDICINE_IDS = "state_hidden_medicine_ids"
        private const val STATE_HIDDEN_MEDICINE_TIMES = "state_hidden_medicine_times"
        private const val CLOCK_UPDATE_INTERVAL_MILLIS = 1000L
    }
}
