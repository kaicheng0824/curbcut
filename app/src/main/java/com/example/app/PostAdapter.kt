package com.example.app


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView


class PostAdapter(private val posts: List<Post>,private val context: Context) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val profile = posts[position]
        holder.postNameTextView.text = profile.submission

        holder.itemView.setOnClickListener {
            // Handle the item click event here
            // You can perform an action based on the clicked profile, e.g., open a new activity, show a toast, etc.
            Toast.makeText(holder.itemView.context, "Clicked: ${profile.submission}", Toast.LENGTH_SHORT).show()
            showPopup(profile.url)
        }

    }

    fun showPopup(postUrl : String) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate( R.layout.popup_window, null );
        popupView.findViewById<TextView>(R.id.PopupTextDisplay).text = postUrl

        val popupWindow = PopupWindow(
            popupView,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        val btnDismiss = popupView.findViewById(R.id.closePopup) as Button
        btnDismiss.setOnClickListener { popupWindow.dismiss() }
        popupWindow.showAsDropDown(popupView, 0, 0)
    }




    override fun getItemCount(): Int {
        return posts.size
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postNameTextView : TextView = itemView.findViewById(R.id.postNameTextView)
        // Add other views from the item layout here if needed
    }
}

