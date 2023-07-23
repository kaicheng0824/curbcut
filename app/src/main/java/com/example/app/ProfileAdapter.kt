package com.example.app

import android.content.Context
import com.example.app.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService

import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter(private val profiles: List<Profile>,private val context: Context) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.profile_item, parent, false)
        return ProfileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profiles[position]
        holder.profileNameTextView.text = profile.name

        holder.itemView.setOnClickListener {
            // Handle the item click event here
            // You can perform an action based on the clicked profile, e.g., open a new activity, show a toast, etc.
            Toast.makeText(holder.itemView.context, "Clicked: ${profile.name}", Toast.LENGTH_SHORT).show()


        }

    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileNameTextView : TextView = itemView.findViewById(R.id.profileNameTextView)
        // Add other views from the item layout here if needed
    }
}

