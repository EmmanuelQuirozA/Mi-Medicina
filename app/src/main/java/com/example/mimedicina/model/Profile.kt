package com.example.mimedicina.model

data class Profile(
    val name: String,
    val medicines: MutableList<Medicine> = mutableListOf()
)