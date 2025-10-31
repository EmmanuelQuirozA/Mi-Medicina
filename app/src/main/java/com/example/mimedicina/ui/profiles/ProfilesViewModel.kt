package com.example.mimedicina.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mimedicina.data.repository.ProfilesRepository
import com.example.mimedicina.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfilesViewModel(
    private val repository: ProfilesRepository
) : ViewModel() {

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfiles().collect { items ->
                _profiles.value = items
            }
        }
    }

    fun addProfile(name: String) {
        viewModelScope.launch {
            val result = repository.addProfile(name.trim())
            result.exceptionOrNull()?.let {
                _error.value = it.message
            }
        }
    }

    fun consumeError() {
        _error.value = null
    }

    class Factory(
        private val repository: ProfilesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfilesViewModel::class.java)) {
                return ProfilesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
