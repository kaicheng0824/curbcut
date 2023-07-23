package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.arcgismaps.ArcGISEnvironment

class ModeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode)

        val btnMeet = findViewById<ImageButton>(R.id.imageButtonMeet)
        val btnTalk = findViewById<ImageButton>(R.id.imageButtonKirbie)
        val btnRoute = findViewById<ImageButton>(R.id.imageButtonRoute)

        btnMeet.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        btnTalk.setOnClickListener {
            val intent = Intent(this, CommunityActivity::class.java)
            startActivity(intent)
        }

        btnRoute.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }






    }
}