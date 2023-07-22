package com.example.app


import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class Community : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.community)
        val backButton = findViewById<View>(R.id.backButton) as Button
        backButton.setOnClickListener { finish() }
    }
}