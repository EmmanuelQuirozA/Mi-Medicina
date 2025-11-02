package com.example.mimedicina.model

enum class ReminderAction {
    TAKEN,
    DISMISSED;

    companion object {
        fun fromStorageValue(value: String?): ReminderAction? = when (value) {
            TAKEN.name -> TAKEN
            DISMISSED.name -> DISMISSED
            else -> null
        }
    }

    fun toStorageValue(): String = name
}
