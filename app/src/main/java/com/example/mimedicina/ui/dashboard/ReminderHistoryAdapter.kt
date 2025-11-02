package com.example.mimedicina.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mimedicina.databinding.ItemReminderHistoryBinding
import com.example.mimedicina.model.ReminderAction
import com.example.mimedicina.model.ReminderHistory
import java.text.SimpleDateFormat
import java.util.Locale

class ReminderHistoryAdapter :
    ListAdapter<ReminderHistory, ReminderHistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemReminderHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(history: ReminderHistory) {
            val context = binding.root.context
            binding.historyMedicineNameTextView.text = history.medicineName
            binding.historyScheduledTextView.text = context.getString(
                com.example.mimedicina.R.string.dashboard_history_triggered_format,
                formatDateTime(history.scheduledTimeMillis)
            )
            val actionTime = history.actionTimeMillis
            binding.historyActionTimeTextView.text = if (actionTime != null) {
                context.getString(
                    com.example.mimedicina.R.string.dashboard_history_action_format,
                    formatDateTime(actionTime)
                )
            } else {
                context.getString(com.example.mimedicina.R.string.dashboard_history_action_pending)
            }
            val actionLabelRes = when (history.action) {
                ReminderAction.TAKEN -> com.example.mimedicina.R.string.dashboard_history_action_taken
                ReminderAction.DISMISSED -> com.example.mimedicina.R.string.dashboard_history_action_dismissed
                null -> com.example.mimedicina.R.string.dashboard_history_action_unknown
            }
            binding.historyActionTextView.setText(actionLabelRes)
        }

        private fun formatDateTime(timeMillis: Long): String {
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return formatter.format(timeMillis)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ReminderHistory>() {
        override fun areItemsTheSame(oldItem: ReminderHistory, newItem: ReminderHistory): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ReminderHistory, newItem: ReminderHistory): Boolean =
            oldItem == newItem
    }
}
