package com.example.mimedicina.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mimedicina.data.repository.MedicinesRepository
import com.example.mimedicina.model.Medicine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: MedicinesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val profileId: Long = checkNotNull(savedStateHandle.get<Long>(ARG_PROFILE_ID))

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeMedicines(profileId).collectLatest { items ->
                _medicines.value = items
            }
        }
    }

    class Factory(
        private val repository: MedicinesRepository,
        private val profileId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                val handle = SavedStateHandle(mapOf(ARG_PROFILE_ID to profileId))
                return DashboardViewModel(repository, handle) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val ARG_PROFILE_ID = "dashboard_profile_id"
    }
}
