package com.example.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)
        val backButton = findViewById<View>(R.id.profileBackButton) as Button
        backButton.setOnClickListener { finish() }

        val profileRecyclerView = findViewById<RecyclerView>(R.id.profilesRecyclerView);
        val profiles = createDummyProfiles()

        // Set up the RecyclerView with the adapter
        val profileAdapter = ProfileAdapter(profiles,this)

        profileRecyclerView.layoutManager = (LinearLayoutManager(this));
        profileRecyclerView.adapter = profileAdapter
    }


    fun createDummyProfiles(): List<Profile> {
        val dummyProfiles = mutableListOf<Profile>()

        // Add some dummy profiles to the list
        dummyProfiles.add(Profile("Ken Le", 30))
        dummyProfiles.add(Profile("Jane Smith", 25))
        dummyProfiles.add(Profile("Bob Johnson", 35))
        dummyProfiles.add(Profile("Alice Williams", 28))
        dummyProfiles.add(Profile("fefe Johnson", 35))
        dummyProfiles.add(Profile("fefefe Williams", 28))
        // Add more profiles as needed

        return dummyProfiles
    }
}