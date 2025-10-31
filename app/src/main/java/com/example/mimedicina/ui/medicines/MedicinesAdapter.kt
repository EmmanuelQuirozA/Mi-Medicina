package com.example.mimedicina.ui.medicines

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mimedicina.R
import com.example.mimedicina.databinding.ItemMedicineBinding
import com.example.mimedicina.model.Medicine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicinesAdapter(
    private val onToggleAlarm: (Medicine, Boolean) -> Unit,
    private val onDelete: (Medicine) -> Unit
) : ListAdapter<Medicine, MedicinesAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMedicineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(medicine: Medicine) {
            binding.medicineNameTextView.text = medicine.name
            binding.medicinePresentationTextView.text = medicine.presentation
            binding.medicinePresentationTextView.isVisible = medicine.presentation.isNotBlank()
            binding.medicineCommentsTextView.text = medicine.comments
            binding.medicineCommentsTextView.isVisible = medicine.comments.isNotBlank()
            binding.medicineFrequencyTextView.text =
                binding.root.context.getString(R.string.frequency_hours_format, medicine.frequencyHours)
            binding.medicineStartTimeTextView.text = binding.root.context.getString(
                R.string.start_time_format,
                dateFormat.format(Date(medicine.startTimeMillis))
            )
            binding.medicineNextReminderTextView.text = if (medicine.nextReminderTimeMillis > 0) {
                binding.root.context.getString(
                    R.string.next_reminder_format,
                    dateFormat.format(Date(medicine.nextReminderTimeMillis))
                )
            } else {
                binding.root.context.getString(R.string.next_alarm_unavailable)
            }

            binding.medicineAlarmSwitch.setOnCheckedChangeListener(null)
            binding.medicineAlarmSwitch.isChecked = medicine.alarmEnabled
            binding.medicineAlarmSwitch.setOnCheckedChangeListener { _, isChecked ->
                onToggleAlarm(medicine, isChecked)
            }

            binding.deleteMedicineButton.setOnClickListener { onDelete(medicine) }

            if (medicine.photoUri.isNullOrBlank()) {
                binding.medicinePhotoImageView.isVisible = false
            } else {
                binding.medicinePhotoImageView.isVisible = true
                binding.medicinePhotoImageView.load(medicine.photoUri)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean =
            oldItem == newItem
    }

    companion object {
        private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    }
}
