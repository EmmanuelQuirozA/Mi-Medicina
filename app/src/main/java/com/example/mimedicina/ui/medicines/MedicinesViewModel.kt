package com.example.mimedicina.ui.medicines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mimedicina.data.repository.MedicinesRepository
import com.example.mimedicina.model.Medicine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedicinesViewModel(
    private val repository: MedicinesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val profileId: Long = checkNotNull(savedStateHandle.get<Long>(ARG_PROFILE_ID))

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeMedicines(profileId).collect { items ->
                _medicines.value = items
            }
        }
    }

    suspend fun addMedicineWithResult(medicine: Medicine): Long {
        return repository.addMedicine(medicine)
    }

    fun updateMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repository.updateMedicine(medicine)
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repository.deleteMedicine(medicine)
        }
    }

    suspend fun getMedicine(id: Long): Medicine? = repository.getMedicine(id)

    class Factory(
        private val repository: MedicinesRepository,
        private val profileId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MedicinesViewModel::class.java)) {
                val handle = SavedStateHandle(mapOf(ARG_PROFILE_ID to profileId))
                return MedicinesViewModel(repository, handle) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val ARG_PROFILE_ID = "profile_id"
    }
}
