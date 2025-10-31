package com.example.mimedicina.alarms

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mimedicina.MiMedicinaApp
import com.example.mimedicina.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MedicineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER) return
        val medicineId = intent.getLongExtra(EXTRA_MEDICINE_ID, -1L)
        if (medicineId == -1L) return
        val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: return
        val app = context.applicationContext as MiMedicinaApp

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationChannels.ensureReminderChannel(context)
            val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notification_title, medicineName))
                .setContentText(context.getString(R.string.notification_body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .build()

            NotificationManagerCompat.from(context).notify(medicineId.toInt(), notification)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val repository = app.medicinesRepository
            val medicine = repository.getMedicine(medicineId)
            if (medicine != null && medicine.alarmEnabled) {
                val nextReminderTime = System.currentTimeMillis() +
                    TimeUnit.HOURS.toMillis(medicine.frequencyHours.toLong())
                val updatedMedicine = medicine.copy(nextReminderTimeMillis = nextReminderTime)
                repository.updateMedicine(updatedMedicine)
                app.alarmScheduler.schedule(updatedMedicine)
            } else {
                app.alarmScheduler.cancel(medicineId)
            }
        }
    }

    companion object {
        const val ACTION_REMINDER = "com.example.mimedicina.ACTION_REMINDER"
        const val EXTRA_MEDICINE_ID = "extra_medicine_id"
        const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
        const val EXTRA_PROFILE_ID = "extra_profile_id"
        const val EXTRA_FREQUENCY_HOURS = "extra_frequency_hours"
    }
}
