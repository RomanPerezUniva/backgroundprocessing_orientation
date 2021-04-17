package com.krewget.android.demoworkmanager

import DatePickerFragment
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.print.PrintHelper.ORIENTATION_PORTRAIT
import androidx.work.*
import java.sql.Time
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var button : Button
    private lateinit var datePicked : Date
    private lateinit var timePicked : Time

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarmas"
            val descriptionText = "ALARMAS"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Alarm", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.orientacion).setOnClickListener{
            if(resources.configuration.orientation==Configuration.ORIENTATION_PORTRAIT)  {
                findViewById<TextView>(R.id.text).text="Portrait"

            }  else{
                findViewById<TextView>(R.id.text).text="Landscape"
            }
        }


        createNotificationChannel()

        button = findViewById<Button>(R.id.b1);

        val messageText: TextView = findViewById(R.id.messageTV)

        val editTextDate: TextView = findViewById(R.id.editTextDate)

        editTextDate.keyListener = null

        editTextDate.setOnClickListener {

            val newFragment = DatePickerFragment.newInstance { _, year, month, day ->
                // +1 because January is zero
                datePicked = Date(year, month, day)
                val selectedDate = day.toString() + " / " + (month + 1) + " / " + year
                editTextDate.text = selectedDate
            }


            newFragment.show(supportFragmentManager, "datePicker")
        }

        val editTextTime: TextView = findViewById(R.id.editTextTime)

        editTextTime.keyListener = null

        editTextTime.setOnClickListener {

            val newFragment = TimePickerFragment.newInstance { view, hourOfDay, minute ->
                // +1 because January is zero
                timePicked = Time(hourOfDay, minute, 0)
                val selectedTime = "$hourOfDay : $minute"
                editTextTime.text = selectedTime
            }


            newFragment.show(supportFragmentManager, "timePicker")
        }


        findViewById<Button>(R.id.b1).setOnClickListener {
            if (!editTextDate.text.isNullOrBlank() || !editTextTime.text.isNullOrBlank()) {
                Toast.makeText(this, "Creando Alarma", Toast.LENGTH_SHORT).show()
                setOneTimeRequest(editTextDate.text.toString(), editTextTime.text.toString(), messageText.text.toString())
            } else Toast.makeText(this, "Porfavor agrega fecha y hora", Toast.LENGTH_SHORT).show()
        }

    }


    private fun setOneTimeRequest(date: String, time: String, message: String?) {

        val now = Date()

        val dater = date.split(" / ")
        val dated = Date(dater[2].toInt() - 1900 , dater[1].toInt()-1, dater[0].toInt())

        dated.hours = time.split(" : ")[0].toInt()
        dated.minutes = time.split(" : ")[1].toInt()

        val timeLeft: Long = dated.time - now.time

        button.text = timeLeft.toString()

        val wm = WorkManager.getInstance(applicationContext)

//        val constraints = Constraints.Builder()
//            .setRequiresCharging(true)
//            .build()

        val data: Data = Data.Builder()
            .putString("string", message ?: "")
            .build()


        val uploadReq = PeriodicWorkRequest.Builder(UploadWork::class.java, timeLeft, TimeUnit.MILLISECONDS)
//            .setConstraints(constraints)
            .setInputData(data)
            .build()

        button.isEnabled = false

        wm.enqueue(uploadReq)
        wm.getWorkInfoByIdLiveData(uploadReq.id).observe(this, Observer {
            button.text = it.state.name
            button.isEnabled = true
        })
    }
}