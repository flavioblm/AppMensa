package com.example.appmensa.classiPaginaUtente.drawerMenuAdapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appmensa.R
import com.example.appmensa.retrofit.Messaggio

class ChatAdapter(private val messages: List<Messaggio>) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageText: TextView = view.findViewById(R.id.textViewMessage)
        val params = messageText.layoutParams as FrameLayout.LayoutParams

        fun bind(message: Messaggio) {
            messageText.text = message.testo

            if (message.id_utente != null) {
                // Messaggio dell'utente
                params.gravity = Gravity.END
                messageText.setBackgroundResource(R.color.snowdark)
            } else {
                // Messaggio del gestore
                params.gravity = Gravity.START
                messageText.setBackgroundResource(R.color.snowstorm)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size
}