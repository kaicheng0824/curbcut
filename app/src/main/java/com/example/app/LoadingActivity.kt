package com.example.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ProgressBar
import com.example.app.R.id.progressBar

class LoadingActivity : AppCompatActivity() {

    private val loadingTimeMillis: Long = 2000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        // Simulate loading time with a delay before starting MainActivity
        Handler().postDelayed({
            val intent = Intent(this, ModeActivity::class.java)
            startActivity(intent)
            finish()
        }, loadingTimeMillis)
    }
}

