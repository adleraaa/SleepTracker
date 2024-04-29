
package com.mistershorr.loginandregistration

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mistershorr.loginandregistration.databinding.ActivitySleepDetailBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter


class SleepDetailActivity : AppCompatActivity() {

    companion object {
        val TAG = "SleepDetailActivity"
        val EXTRA_SLEEP = "sleepytime"
    }

    private lateinit var binding: ActivitySleepDetailBinding
    lateinit var bedTime: LocalDateTime
    lateinit var wakeTime: LocalDateTime
    lateinit var sleepDateTime: LocalDateTime



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var sleep = intent.getParcelableExtra<Sleep>(EXTRA_SLEEP)
        if(sleep != null){
            bedTime = LocalDateTime.ofEpochSecond(sleep.bedMillis/1000, 0,
                ZoneId.systemDefault().rules.getOffset(Instant.now()))
            wakeTime = LocalDateTime.ofEpochSecond(sleep.wakeMillis/1000, 0,
                ZoneId.systemDefault().rules.getOffset(Instant.now()))
            sleepDateTime = LocalDateTime.ofEpochSecond(sleep.sleepDateMillis/1000, 0,
                ZoneId.systemDefault().rules.getOffset(Instant.now()))
        }
        // these are default values that should be set when creating a new entry
        // however, if editing an existing entry, those values should be used instead
        else {
            sleep = Sleep()
            bedTime = LocalDateTime.now()
            sleepDateTime = LocalDateTime.now()
            wakeTime = bedTime.plusHours(8)
        }

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        binding.buttonSleepDetailBedTime.text = timeFormatter.format(bedTime)
        binding.editTextTextMultiLineSleepDetailNotes.setText(sleep.notes)
        binding.buttonSleepDetailWakeTime.text = timeFormatter.format(wakeTime)
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE MMM dd, yyyy")
        binding.buttonSleepDetailDate.text = dateFormatter.format(sleepDateTime)
        binding.ratingBarSleepDetailQuality.rating = sleep.quality / 2.0f

        binding.buttonSleepDetailCancel.setOnClickListener{
            this.finish()
        }

        binding.buttonSleepDetailSave.setOnClickListener {
            sleep.bedMillis = bedTime.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now())) * 1000
            sleep.wakeMillis = wakeTime.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now())) * 1000
            sleep.sleepDateMillis = sleepDateTime.toEpochSecond(ZoneId.systemDefault().rules.getOffset(Instant.now())) * 1000
            if (sleep == null) {
                sleep.ownerId = Backendless.UserService.CurrentUser().userId
            }
            sleep.notes = binding.editTextTextMultiLineSleepDetailNotes.text.toString()

            // read the number of stars and multiply * 2 and set the quality
            sleep.quality = (binding.ratingBarSleepDetailQuality.rating * 2).toInt()
            Backendless.Data.of(Sleep::class.java)
                .save(sleep, object : AsyncCallback<Sleep?> {
                    override fun handleResponse(response: Sleep?) {
                        Log.d(TAG, "handleResponse: saved")
                        finish()
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Log.d(TAG, "handleFault: ${fault.message}")
                    }
                })


        }

        binding.buttonSleepDetailBedTime.setOnClickListener {
            setTime(bedTime, timeFormatter, binding.buttonSleepDetailBedTime)
        }

        binding.buttonSleepDetailWakeTime.setOnClickListener {
            setTime(wakeTime, timeFormatter, binding.buttonSleepDetailWakeTime)
        }

        binding.buttonSleepDetailDate.setOnClickListener {
            val selection = bedTime.toEpochSecond(UTC)
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(selection*1000) // requires milliseconds
                .setTitleText("Select a Date")
                .build()

            Log.d(TAG, "onCreate: after build: ${LocalDateTime.ofEpochSecond(datePicker.selection?: 0L, 0, UTC)}")
            datePicker.addOnPositiveButtonClickListener { millis ->
                val selectedLocalDate = Instant.ofEpochMilli(millis).atOffset(UTC).toLocalDateTime()
                Toast.makeText(this, "Date is: ${dateFormatter.format(selectedLocalDate)}", Toast.LENGTH_SHORT).show()

                // make sure that waking up the next day if waketime < bedtime is preserved
                var wakeDate = selectedLocalDate

                if(wakeTime.dayOfMonth != bedTime.dayOfMonth) {
                    wakeDate = wakeDate.plusDays(1)
                }

                bedTime = LocalDateTime.of(
                    selectedLocalDate.year,
                    selectedLocalDate.month,
                    selectedLocalDate.dayOfMonth,
                    bedTime.hour,
                    bedTime.minute
                )

                wakeTime = LocalDateTime.of(
                    wakeDate.year,
                    wakeDate.month,
                    wakeDate.dayOfMonth,
                    wakeTime.hour,
                    wakeTime.minute
                )
                binding.buttonSleepDetailDate.text = dateFormatter.format(bedTime)
            }
            datePicker.show(supportFragmentManager, "datepicker")
        }

    }

    fun setTime(time: LocalDateTime, timeFormatter: DateTimeFormatter, button: Button) {
        val timePickerDialog = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(time.hour)
            .setMinute(time.minute)
            .build()

        timePickerDialog.show(supportFragmentManager, "bedtime")
        timePickerDialog.addOnPositiveButtonClickListener {
            var selectedTime = LocalDateTime.of(time.year, time.month, time.dayOfMonth, timePickerDialog.hour, timePickerDialog.minute)
            button.text = timeFormatter.format(selectedTime)
            when(button.id) {
                binding.buttonSleepDetailBedTime.id -> {
                    bedTime = selectedTime
                    if(wakeTime.toEpochSecond(UTC) < selectedTime.toEpochSecond(UTC)) {
                        wakeTime = wakeTime.plusDays(1)
                    }
                }
                binding.buttonSleepDetailWakeTime.id -> {
                    if(selectedTime.toEpochSecond(UTC) < bedTime.toEpochSecond(UTC)) {
                        selectedTime = selectedTime.plusDays(1)
                    }
                    wakeTime = selectedTime
                }
            }
        }
    }
}