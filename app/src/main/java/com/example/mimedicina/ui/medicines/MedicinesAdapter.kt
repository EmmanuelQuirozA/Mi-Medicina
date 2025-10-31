package com.example.mimedicina.ui.medicines

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mimedicina.databinding.ItemMedicineBinding
import com.example.mimedicina.model.Medicine

class MedicinesAdapter(private val medicines: List<Medicine>) : RecyclerView.Adapter<MedicinesAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMedicineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.binding.medicineNameTextView.text = medicine.name
        holder.binding.medicinePresentationTextView.text = medicine.presentation
        holder.binding.medicineCommentsTextView.text = medicine.comments
        holder.binding.medicineFrequencyTextView.text = "Cada ${medicine.frequency} horas"
        holder.binding.medicineStartDateTextView.text = "Inicia: ${medicine.startDate}"
        holder.binding.medicineAlarmSwitch.isChecked = medicine.alarmEnabled
    }

    override fun getItemCount() = medicines.size
}