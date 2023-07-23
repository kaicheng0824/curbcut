//package com.example.app
//
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.os.Handler
//import android.widget.VideoView
//import androidx.appcompat.app.AppCompatActivity
//
//class SplashActivity : AppCompatActivity() {
//
//    private val loadingTimeMillis: Long = 5000 // 5 seconds
//    private lateinit var videoView: VideoView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_loading)
//
//        videoView = findViewById(R.id.videoView2)
//
//        // Set the video URI and start playing
//        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.your_video_file}")
//        videoView.setVideoURI(videoUri)
//        videoView.start()
//
//        // Simulate loading time with a delay before starting MainActivity
//        Handler().postDelayed({
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }, loadingTimeMillis)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // Release the VideoView resources when the activity is destroyed
//        videoView.stopPlayback()
//    }
//}