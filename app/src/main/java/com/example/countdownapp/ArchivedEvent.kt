package com.example.countdownapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ArchivedEvent : AppCompatActivity() {

    private lateinit var eventList: RecyclerView
    private lateinit var adapter: ArchivedEventAdapter
    private val events = mutableListOf<Event>()
    private lateinit var  emptyTasks: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_archived_event)

        eventList = findViewById(R.id.event_list_archieved)
        eventList.layoutManager = LinearLayoutManager(this)
        adapter = ArchivedEventAdapter(events)
        eventList.adapter = adapter
        emptyTasks = findViewById(R.id.empty_tasks)

        val archivedEvents = intent.getParcelableArrayListExtra<Event>("archivedEvents")


        if (archivedEvents != null) {
            events.clear()
            events.addAll(archivedEvents)
            adapter.notifyDataSetChanged()
            if (events.isEmpty()) {
                emptyTasks.visibility = View.VISIBLE
            } else {
                emptyTasks.visibility = View.GONE
            }
        }
    }
}