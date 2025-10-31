package com.example.mimedicina.ui.medicines

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mimedicina.R
import com.example.mimedicina.databinding.ItemMedicineBinding
import com.example.mimedicina.model.Medicine
import java.util.Locale

class MedicinesAdapter(
    private val onToggleAlarm: (Medicine, Boolean) -> Unit,
    private val onDelete: (Medicine) -> Unit,
    private val onEdit: (Medicine) -> Unit
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
                binding.root.context.getString(R.string.medicine_take_every_format, medicine.frequencyHours)

            val initials = medicine.name.trim().takeIf { it.isNotEmpty() }
                ?.substring(0, 1)?.uppercase(Locale.getDefault()) ?: "?"
            binding.medicineInitialsTextView.text = initials

            if (medicine.photoUri.isNullOrBlank()) {
                binding.medicinePhotoImageView.isVisible = false
                binding.medicineInitialsTextView.isVisible = true
            } else {
                binding.medicinePhotoImageView.isVisible = true
                binding.medicinePhotoImageView.load(medicine.photoUri)
                binding.medicineInitialsTextView.isVisible = false
            }

            val context = binding.root.context
            val alarmBackground = if (medicine.alarmEnabled) {
                R.drawable.bg_alarm_button_active
            } else {
                R.drawable.bg_alarm_button_inactive
            }
            val alarmTint = if (medicine.alarmEnabled) {
                R.color.success_green
            } else {
                R.color.icon_inactive
            }
            binding.medicineAlarmButton.setBackgroundResource(alarmBackground)
            ImageViewCompat.setImageTintList(
                binding.medicineAlarmButton,
                ColorStateList.valueOf(ContextCompat.getColor(context, alarmTint))
            )
            binding.medicineAlarmButton.contentDescription = context.getString(
                if (medicine.alarmEnabled) R.string.disable_alarm else R.string.enable_alarm
            )
            binding.medicineAlarmButton.setOnClickListener {
                onToggleAlarm(medicine, !medicine.alarmEnabled)
            }

            binding.deleteMedicineButton.setOnClickListener { onDelete(medicine) }
            binding.root.setOnClickListener { onEdit(medicine) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean =
            oldItem == newItem
    }
}
