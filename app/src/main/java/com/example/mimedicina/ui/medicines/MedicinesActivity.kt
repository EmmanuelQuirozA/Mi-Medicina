package com.example.mimedicina.ui.medicines

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.mimedicina.MiMedicinaApp
import com.example.mimedicina.R
import com.example.mimedicina.databinding.ActivityMedicinesBinding
import com.example.mimedicina.databinding.DialogAddMedicineBinding
import com.example.mimedicina.model.Medicine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class MedicinesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicinesBinding
    private val app by lazy { application as MiMedicinaApp }
    private val alarmScheduler by lazy { app.alarmScheduler }

    private val profileId: Long by lazy { intent.getLongExtra(EXTRA_PROFILE_ID, -1L) }
    private val profileName: String by lazy { intent.getStringExtra(EXTRA_PROFILE_NAME) ?: "" }

    private val viewModel: MedicinesViewModel by viewModels {
        MedicinesViewModel.Factory(app.medicinesRepository, profileId)
    }

    private val adapter: MedicinesAdapter by lazy {
        MedicinesAdapter(
            onToggleAlarm = ::toggleAlarm,
            onDelete = ::confirmDeleteMedicine
        )
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, R.string.permission_notifications_rationale, Toast.LENGTH_SHORT).show()
            }
        }

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            currentDialogBinding?.let { binding ->
                if (uri != null) {
                    selectedImageUri = uri.toString()
                    binding.medicinePhotoImageView.isVisible = true
                    binding.medicinePhotoImageView.load(uri)
                }
            }
        }

    private var currentDialogBinding: DialogAddMedicineBinding? = null
    private var selectedImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicinesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (profileId == -1L) {
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        setupToolbar()
        setupRecyclerView()
        observeMedicines()
        binding.addMedicineButton.setOnClickListener { showAddMedicineDialog() }
    }

    private fun setupToolbar() {
        binding.toolbar.title = profileName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.medicinesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.medicinesRecyclerView.adapter = adapter
        binding.medicinesRecyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun observeMedicines() {
        lifecycleScope.launch {
            viewModel.medicines.collectLatest { medicines ->
                binding.emptyStateTextView.isVisible = medicines.isEmpty()
                binding.medicinesRecyclerView.isVisible = medicines.isNotEmpty()
                adapter.submitList(medicines)
            }
        }
    }

    private fun toggleAlarm(medicine: Medicine, enabled: Boolean) {
        lifecycleScope.launch {
            if (enabled) {
                val adjusted = adjustNextReminder(medicine.copy(alarmEnabled = true))
                viewModel.updateMedicine(adjusted)
                ensureNotificationPermission()
                alarmScheduler.schedule(adjusted)
            } else {
                val updated = medicine.copy(alarmEnabled = false)
                viewModel.updateMedicine(updated)
                alarmScheduler.cancel(medicine.id)
            }
        }
    }

    private fun showAddMedicineDialog() {
        val dialogBinding = DialogAddMedicineBinding.inflate(layoutInflater)
        currentDialogBinding = dialogBinding
        selectedImageUri = null
        dialogBinding.medicinePhotoImageView.isVisible = false
        dialogBinding.startDateErrorTextView.isVisible = false
        dialogBinding.startDateButton.tag = null

        val calendar = Calendar.getInstance()
        dialogBinding.startDateButton.setOnClickListener {
            showDateTimePicker(calendar) { updatedCalendar ->
                dialogBinding.startDateButton.text = formatDateTime(updatedCalendar.timeInMillis)
                dialogBinding.startDateButton.tag = updatedCalendar.timeInMillis
                dialogBinding.startDateErrorTextView.isVisible = false
            }
        }

        dialogBinding.addPhotoButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.add_medicine)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positive.setOnClickListener {
                dialogBinding.medicineNameInput.error = null
                dialogBinding.medicinePresentationInput.error = null
                dialogBinding.medicineFrequencyInput.error = null
                dialogBinding.startDateErrorTextView.isVisible = false

                val name = dialogBinding.medicineNameInput.editText?.text?.toString()?.trim().orEmpty()
                val presentation = dialogBinding.medicinePresentationInput.editText?.text?.toString()?.trim().orEmpty()
                val comments = dialogBinding.medicineCommentsInput.editText?.text?.toString()?.trim().orEmpty()
                val frequencyText = dialogBinding.medicineFrequencyInput.editText?.text?.toString()?.trim().orEmpty()
                val startTime = dialogBinding.startDateButton.tag as? Long
                val alarmEnabled = dialogBinding.enableAlarmSwitch.isChecked

                when {
                    name.isBlank() -> dialogBinding.medicineNameInput.error = getString(R.string.error_empty_field)
                    presentation.isBlank() -> dialogBinding.medicinePresentationInput.error = getString(R.string.error_empty_field)
                    frequencyText.isBlank() -> dialogBinding.medicineFrequencyInput.error = getString(R.string.error_empty_field)
                    startTime == null -> dialogBinding.startDateErrorTextView.isVisible = true
                    else -> {
                        val frequencyHours = frequencyText.toIntOrNull()
                        if (frequencyHours == null || frequencyHours <= 0) {
                            dialogBinding.medicineFrequencyInput.error = getString(R.string.error_empty_field)
                            return@setOnClickListener
                        }
                        val medicine = Medicine(
                            id = 0,
                            profileId = profileId,
                            name = name,
                            presentation = presentation,
                            comments = comments,
                            photoUri = selectedImageUri,
                            frequencyHours = frequencyHours,
                            startTimeMillis = startTime,
                            nextReminderTimeMillis = startTime,
                            alarmEnabled = alarmEnabled
                        )
                        lifecycleScope.launch {
                            try {
                                val newId = viewModel.addMedicineWithResult(medicine)
                                val savedMedicine = viewModel.getMedicine(newId) ?: return@launch
                                if (alarmEnabled) {
                                    val adjusted = adjustNextReminder(savedMedicine)
                                    viewModel.updateMedicine(adjusted)
                                    ensureNotificationPermission()
                                    alarmScheduler.schedule(adjusted)
                                }
                            } catch (error: Exception) {
                                Toast.makeText(
                                    this@MedicinesActivity,
                                    error.localizedMessage ?: getString(R.string.error_generic),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.setOnDismissListener {
            currentDialogBinding = null
            selectedImageUri = null
        }
        dialog.show()
    }

    private fun showDateTimePicker(calendar: Calendar, onDateTimeSelected: (Calendar) -> Unit) {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val is24Hour = DateFormat.is24HourFormat(this)
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        onDateTimeSelected(calendar)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    is24Hour
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun formatDateTime(timeMillis: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return formatter.format(timeMillis)
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun adjustNextReminder(medicine: Medicine): Medicine {
        val interval = TimeUnit.HOURS.toMillis(medicine.frequencyHours.toLong().coerceAtLeast(1))
        var nextTime = medicine.nextReminderTimeMillis
        val now = System.currentTimeMillis()
        while (nextTime < now) {
            nextTime += interval
        }
        return medicine.copy(nextReminderTimeMillis = nextTime)
    }

    private fun confirmDeleteMedicine(medicine: Medicine) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_medicine)
            .setMessage(getString(R.string.confirm_delete_medicine, medicine.name))
            .setPositiveButton(R.string.accept) { _, _ ->
                lifecycleScope.launch {
                    viewModel.deleteMedicine(medicine)
                    alarmScheduler.cancel(medicine.id)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    companion object {
        const val EXTRA_PROFILE_ID = "extra_profile_id"
        const val EXTRA_PROFILE_NAME = "extra_profile_name"
    }
}
