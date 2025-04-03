package com.example.countdownapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArchivedEventAdapter(private val eventList: List<Event>) :
    RecyclerView.Adapter<ArchivedEventAdapter.ArchivedEventViewHolder>() {

    inner class ArchivedEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventName: TextView = itemView.findViewById(R.id.event_name)
        private val eventEmoji: TextView = itemView.findViewById(R.id.emoji_of_event)

        fun bind(event: Event) {
            eventName.text = event.name
            eventEmoji.text = event.emoji
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivedEventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return ArchivedEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArchivedEventViewHolder, position: Int) {
        holder.bind(eventList[position])
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}