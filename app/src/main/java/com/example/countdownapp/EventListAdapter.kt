package com.example.countdownapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.text.SimpleDateFormat
import java.util.*

class EventListAdapter(
    private val eventList: List<Event>,
    private val onRemoveClickListener: (Int) -> Unit,
    private val onArchiveClickListener: (Int) -> Unit
) : RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {;
        private val eventBackground: ConstraintLayout = itemView.findViewById(R.id.event_bg)
        private val eventName: TextView = itemView.findViewById(R.id.event_name)
        private val eventEmoji: TextView = itemView.findViewById(R.id.emoji_of_event)
        private val daysTxt: TextView = itemView.findViewById(R.id.days_txt)
        private val hoursTxt: TextView = itemView.findViewById(R.id.Hours_txt)
        private val minutesTxt: TextView = itemView.findViewById(R.id.Min_txt)
        private val handler = Handler(Looper.getMainLooper())
        private lateinit var targetDateStr: String
        private val countdownRunnable = object : Runnable {
            override fun run() {
                val adapterPosition = bindingAdapterPosition
                if (adapterPosition == RecyclerView.NO_POSITION || adapterPosition >= eventList.size) {
                    return
                }
                val event = eventList[adapterPosition]
                val (countdownValues, isEnded) = calculateCountdown(event.date)
                daysTxt.text = "${countdownValues[0]}"
                hoursTxt.text = "${countdownValues[1]}"
                minutesTxt.text = "${countdownValues[2]}"
                if (isEnded) {

                    onArchiveClickListener(adapterPosition)
                    handler.removeCallbacks(this)
                    return
                }
                handler.postDelayed(this, 1000)
            }
        }
        fun bind(event: Event) {
            eventName.text = event.name
            eventEmoji.text = event.emoji
            targetDateStr = event.date
            event.colorBackground?.let { eventBackground.setBackgroundColor(it) }
            event.imageBackgroundUri?.let { uriString ->
                val uri = Uri.parse(uriString)
                Glide.with(itemView.context)
                    .load(uri)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            eventBackground.background = resource
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                            eventBackground.background = placeholder
                        }
                    })
            } ?: run {
                eventBackground.background = null
                event.colorBackground?.let { eventBackground.setBackgroundColor(it) }
            }
            handler.post(countdownRunnable)
        }
        fun cleanup() {
            handler.removeCallbacks(countdownRunnable)
        }
        private fun calculateCountdown(targetDateStr: String): Pair<IntArray, Boolean> {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val targetDate = dateFormat.parse(targetDateStr) ?: return intArrayOf(0, 0, 0) to true

                val currentDate = Calendar.getInstance().time
                val diffMillis = targetDate.time - currentDate.time

                if (diffMillis <= 0) {
                    return intArrayOf(0, 0, 0) to true
                }

                val seconds = (diffMillis / 1000).toInt()
                val minutes = (seconds / 60).toInt()
                val hours = (minutes / 60).toInt()
                val days = (hours / 24).toInt()
                val remainingHours = (hours % 24).toInt()
                val remainingMinutes = (minutes % 60).toInt()

                return intArrayOf(days, remainingHours, remainingMinutes) to false
            } catch (e: Exception) {
                e.printStackTrace()
                return intArrayOf(0, 0, 0) to true
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]


        holder.itemView.findViewById<ImageView>(R.id.event_remove).setOnClickListener {
            onRemoveClickListener(position)
        }



        holder.itemView.setOnLongClickListener {
            onAddWidgetClickListener(event)
            true
        }

        holder.bind(event)
    }
    override fun getItemCount(): Int {
        return eventList.size
    }
    private var onAddWidgetClickListener: (Event) -> Unit = {}
    fun setOnAddWidgetClickListener(listener: (Event) -> Unit) {
        onAddWidgetClickListener = listener
    }
    override fun onViewRecycled(holder: EventViewHolder) {
        super.onViewRecycled(holder)
        holder.cleanup()
    }
}