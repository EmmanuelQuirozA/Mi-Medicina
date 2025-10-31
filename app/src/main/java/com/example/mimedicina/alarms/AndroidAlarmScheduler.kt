package com.example.mimedicina.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.mimedicina.model.Medicine

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(medicine: Medicine) {
        val triggerAtMillis = medicine.nextReminderTimeMillis
        if (triggerAtMillis <= System.currentTimeMillis()) {
            return
        }
        val pendingIntent = buildPendingIntent(medicine)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    override fun cancel(medicineId: Long) {
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = MedicineAlarmReceiver.ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun buildPendingIntent(medicine: Medicine): PendingIntent {
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = MedicineAlarmReceiver.ACTION_REMINDER
            putExtra(MedicineAlarmReceiver.EXTRA_MEDICINE_ID, medicine.id)
            putExtra(MedicineAlarmReceiver.EXTRA_MEDICINE_NAME, medicine.name)
            putExtra(MedicineAlarmReceiver.EXTRA_PROFILE_ID, medicine.profileId)
            putExtra(MedicineAlarmReceiver.EXTRA_FREQUENCY_HOURS, medicine.frequencyHours)
        }
        return PendingIntent.getBroadcast(
            context,
            medicine.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
