package com.example.appmensa.classiPaginaUtente.drawerMenuAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appmensa.R
import com.example.appmensa.retrofit.FeedbackResponse

class FeedbackAdapter(private val feedbacks: List<FeedbackResponse>) :
    RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar2)
        val commentTextView: TextView = view.findViewById(R.id.commentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feedback = feedbacks[position]
        holder.ratingBar.rating = feedback.valutazione.toFloat()
        holder.commentTextView.text = feedback.commento
    }

    override fun getItemCount() = feedbacks.size
}