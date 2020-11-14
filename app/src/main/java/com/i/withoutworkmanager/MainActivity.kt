package com.i.withoutworkmanager

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var myService: BlurService? = null
    var isBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Glide.with(this).asBitmap()
            .load(BitmapFactory.decodeResource(this.resources, R.drawable.koi_fish)).into(slika)
        zamegliSliko.setOnClickListener {
            applyBlur(seekBar.progress, this, myConnection)
            it.isEnabled = false
            progressBar.visibility = View.VISIBLE }
    }

    private fun observeState(){
        myService?.finished?.observe(this, Observer { finished ->
            if(finished){
                progressBar.visibility = View.INVISIBLE
                zamegliSliko.isEnabled = true
                if(!myService?.blurredImageUri.isNullOrEmpty()){
                    Glide.with(this).asBitmap()
                        .load(myService?.blurredImageUri).into(slika)
                }
            }
        })
    }

    private val myConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) { val binder = service as BlurService.MyLocalBinder
            myService = binder.service
            isBound = true
            observeState() }
        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }
}