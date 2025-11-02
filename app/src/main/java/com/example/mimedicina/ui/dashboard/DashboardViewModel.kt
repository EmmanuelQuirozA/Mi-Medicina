package com.example.mimedicina.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mimedicina.alarms.AlarmScheduler
import com.example.mimedicina.data.repository.MedicinesRepository
import com.example.mimedicina.data.repository.ReminderHistoryRepository
import com.example.mimedicina.model.Medicine
import com.example.mimedicina.model.ReminderAction
import com.example.mimedicina.model.ReminderHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.max

class DashboardViewModel(
    private val medicinesRepository: MedicinesRepository,
    private val reminderHistoryRepository: ReminderHistoryRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val profileId: Long = checkNotNull(savedStateHandle.get<Long>(ARG_PROFILE_ID))

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    private val _history = MutableStateFlow<List<ReminderHistory>>(emptyList())
    val history: StateFlow<List<ReminderHistory>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            medicinesRepository.observeMedicines(profileId).collectLatest { items ->
                _medicines.value = items
            }
        }
        viewModelScope.launch {
            reminderHistoryRepository.observeHistory(profileId).collectLatest { entries ->
                _history.value = entries
            }
        }
    }

    fun markReminderTaken(medicine: Medicine) {
        handleReminderAction(medicine, ReminderAction.TAKEN)
    }

    fun dismissReminder(medicine: Medicine) {
        handleReminderAction(medicine, ReminderAction.DISMISSED)
    }

    private fun handleReminderAction(medicine: Medicine, action: ReminderAction) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            reminderHistoryRepository.markReminderAction(medicine, action, now)
            val interval = TimeUnit.HOURS.toMillis(medicine.frequencyHours.toLong().coerceAtLeast(1))
            val baseTime = max(now, medicine.nextReminderTimeMillis)
            val nextReminderTime = baseTime + interval
            val updatedMedicine = medicine.copy(nextReminderTimeMillis = nextReminderTime)
            medicinesRepository.updateMedicine(updatedMedicine)
            if (medicine.alarmEnabled) {
                alarmScheduler.schedule(updatedMedicine)
            } else {
                alarmScheduler.cancel(medicine.id)
            }
        }
    }

    class Factory(
        private val medicinesRepository: MedicinesRepository,
        private val reminderHistoryRepository: ReminderHistoryRepository,
        private val alarmScheduler: AlarmScheduler,
        private val profileId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                val handle = SavedStateHandle(mapOf(ARG_PROFILE_ID to profileId))
                return DashboardViewModel(
                    medicinesRepository,
                    reminderHistoryRepository,
                    alarmScheduler,
                    handle
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val ARG_PROFILE_ID = "dashboard_profile_id"
    }
}
