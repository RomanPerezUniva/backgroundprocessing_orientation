package com.krewget.android.demoworkmanager

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.lang.Exception

class UploadWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        try {
            val data : String = inputData.getString("string") ?: "SIN MENSAJE"
//            for (i: Int in 0..600) {
//                Log.i("WORKMANAGER", "Subiendo elemento $i")
//            }
            var builder = NotificationCompat.Builder(applicationContext, "Alarm")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("ALARMA!")
                .setContentText(data)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)


            with(NotificationManagerCompat.from(applicationContext)) {
                // notificationId is a unique int for each notification that you must define
                notify(1, builder.build())
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}