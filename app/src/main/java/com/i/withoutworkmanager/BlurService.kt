package com.i.withoutworkmanager

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.work.workDataOf
import java.util.*

class BlurService: Service() {
    private val myBinder = MyLocalBinder()
    val finished = MutableLiveData(false)
    var blurredImageUri = ""
    inner class MyLocalBinder : Binder() {
        val service: BlurService
            get() = this@BlurService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val image = intent.getStringExtra("KEY_IMAGE_URI")
        val blurAmount = intent.getIntExtra("BLUR_AMOUNT", 1)
        blurredImageUri = image
        startForeground(
            999,
            createNotification("Foreground service started", applicationContext)
        )
        val resolver = applicationContext.contentResolver

        val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(image)))
        var output = picture
        for(i in 1..blurAmount){
            output = blurBitmap(output, applicationContext)
        }
        val outputUri = writeBitmapToFile(applicationContext, output)
        val outputImage = BitmapFactory.decodeStream(
            resolver.openInputStream(Uri.parse(outputUri.toString())))
        blurredImageUri = MediaStore.Images.Media.insertImage(
            resolver, outputImage, "Blurred image", Date().toString())
        finished.postValue(true)
        stopSelf()

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("service","Service destroyed.")
        stopForeground(true)
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}