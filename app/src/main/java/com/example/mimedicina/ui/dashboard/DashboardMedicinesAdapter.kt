package com.example.mimedicina.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mimedicina.R
import com.example.mimedicina.databinding.ItemDashboardMedicineBinding
import com.example.mimedicina.model.Medicine
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class DashboardMedicinesAdapter(
    private val onMedicineDismissed: (Medicine) -> Unit
) : ListAdapter<Medicine, DashboardMedicinesAdapter.ViewHolder>(DiffCallback) {

    private var currentTimeMillis: Long = System.currentTimeMillis()

    fun updateCurrentTime(timeMillis: Long) {
        currentTimeMillis = timeMillis
        if (itemCount > 0) {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardMedicineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), currentTimeMillis, onMedicineDismissed)
    }

    class ViewHolder(
        private val binding: ItemDashboardMedicineBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine, currentTimeMillis: Long, onMedicineDismissed: (Medicine) -> Unit) {
            val context = binding.root.context
            binding.medicineNameTextView.text = medicine.name

            if (medicine.presentation.isBlank()) {
                binding.medicinePresentationTextView.isVisible = false
            } else {
                binding.medicinePresentationTextView.isVisible = true
                binding.medicinePresentationTextView.text = medicine.presentation
            }

            binding.medicineFrequencyTextView.text =
                context.getString(R.string.medicine_take_every_format, medicine.frequencyHours)

            val nextReminderText = if (medicine.nextReminderTimeMillis > 0L) {
                context.getString(
                    R.string.dashboard_next_dose_format,
                    formatDateTime(medicine.nextReminderTimeMillis)
                )
            } else {
                context.getString(
                    R.string.dashboard_next_dose_format,
                    context.getString(R.string.next_alarm_unavailable)
                )
            }
            binding.medicineNextTimeTextView.text = nextReminderText

            val status = buildStatusText(context, medicine, currentTimeMillis)
            binding.medicineStatusTextView.text = status.text
            binding.medicineStatusTextView.setTextColor(
                ContextCompat.getColor(context, status.colorRes)
            )

            binding.markDoneButton.setOnClickListener { onMedicineDismissed(medicine) }
            binding.dismissButton.setOnClickListener { onMedicineDismissed(medicine) }
        }

        private fun buildStatusText(
            context: android.content.Context,
            medicine: Medicine,
            currentTimeMillis: Long
        ): StatusInfo {
            val nextReminder = medicine.nextReminderTimeMillis
            if (!medicine.alarmEnabled || nextReminder <= 0L) {
                return StatusInfo(
                    context.getString(R.string.dashboard_no_reminder),
                    R.color.text_secondary
                )
            }

            val diff = nextReminder - currentTimeMillis
            val threshold = TimeUnit.MINUTES.toMillis(1)
            return when {
                diff > threshold -> {
                    val timeRemaining = formatDuration(context, diff)
                    StatusInfo(
                        context.getString(R.string.dashboard_due_in_format, timeRemaining),
                        R.color.success_green
                    )
                }
                diff < -threshold -> {
                    val overdueTime = formatDuration(context, abs(diff))
                    StatusInfo(
                        context.getString(R.string.dashboard_overdue_format, overdueTime),
                        R.color.danger_red
                    )
                }
                else -> StatusInfo(
                    context.getString(R.string.dashboard_due_now),
                    R.color.blue_primary
                )
            }
        }

        private fun formatDuration(context: android.content.Context, durationMillis: Long): String {
            val absoluteMillis = abs(durationMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(absoluteMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(absoluteMillis) -
                TimeUnit.HOURS.toMinutes(hours)

            val parts = mutableListOf<String>()
            if (hours > 0) {
                parts += context.resources.getQuantityString(
                    R.plurals.dashboard_hours,
                    hours.toInt(),
                    hours.toInt()
                )
            }
            if (minutes > 0 || parts.isEmpty()) {
                val minuteQuantity = minutes.toInt().coerceAtLeast(1)
                parts += context.resources.getQuantityString(
                    R.plurals.dashboard_minutes,
                    minuteQuantity,
                    minuteQuantity
                )
            }
            return parts.joinToString(separator = " ")
        }

        private fun formatDateTime(timeMillis: Long): String {
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return formatter.format(timeMillis)
        }

        private data class StatusInfo(val text: String, val colorRes: Int)
    }

    private object DiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean =
            oldItem == newItem
    }
}
