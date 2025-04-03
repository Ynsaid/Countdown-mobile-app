package com.example.countdownapp

import ItemDecoration
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {


    private lateinit var eventListAdapter: EventListAdapter

    // Other views
    private lateinit var emptyList: TextView
    private lateinit var emptyListImage: ImageView
    private lateinit var archiveBtn: ImageView
    private lateinit var addBtn: Button
    private lateinit var eventList: RecyclerView


    private val eventsLists = mutableListOf<Event>()
    private val archivedEvents = mutableListOf<Event>()

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var widgetResultLauncher: ActivityResultLauncher<Intent>


    private val REQUEST_ADD_WIDGET = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        emptyList = findViewById(R.id.text_empty)
        emptyListImage = findViewById(R.id.img_empty)
        archiveBtn = findViewById(R.id.archive_btn)
        addBtn = findViewById(R.id.addEvent_btn)
        eventList = findViewById(R.id.event_list)


        loadEventsFromSharedPreferences()


        eventListAdapter = EventListAdapter(
            eventsLists,
            onRemoveClickListener = { position ->
                if (position in eventsLists.indices) {
                    eventsLists.removeAt(position)
                    eventListAdapter.notifyItemRemoved(position)
                    updateEmptyStateVisibility()
                    saveEventsToSharedPreferences()
                }
            },
            onArchiveClickListener = { position ->
                val event = eventsLists.removeAt(position)
                archivedEvents.add(event)
                eventListAdapter.notifyItemRemoved(position)
                updateEmptyStateVisibility()
                saveEventsToSharedPreferences()
            }
        )


        eventList.layoutManager = LinearLayoutManager(this)
        eventList.adapter = eventListAdapter


        val spaceHeightInPixels = resources.getDimensionPixelSize(R.dimen.mr_item)
        eventList.addItemDecoration(ItemDecoration(spaceHeightInPixels))


        updateEmptyStateVisibility()


        archiveBtn.setOnClickListener {
            val intent = Intent(this, ArchivedEvent::class.java)
            intent.putParcelableArrayListExtra("archivedEvents", ArrayList(archivedEvents))
            startActivity(intent)
        }


        addBtn.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            activityResultLauncher.launch(intent)
        }


        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val name = data?.getStringExtra("name") ?: ""
                val date = data?.getStringExtra("date") ?: ""
                val emoji = data?.getStringExtra("emoji") ?: ""
                val colorBackground = data?.getIntExtra("colorBackground", Color.WHITE)
                val imageBackgroundUri = data?.getStringExtra("imageBackgroundUri")


                val newEvent = Event(name, date, emoji, colorBackground, imageBackgroundUri)
                eventsLists.add(newEvent)
                eventListAdapter.notifyItemInserted(eventsLists.size - 1)

                updateEmptyStateVisibility()
                saveEventsToSharedPreferences()
            }
        }


        widgetResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                    ?: return@registerForActivityResult

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    EventWidgetProvider.updateAppWidget(this, AppWidgetManager.getInstance(this), appWidgetId)
                }
            }
        }


        eventListAdapter.setOnAddWidgetClickListener { event ->
            AlertDialog.Builder(this)
                .setTitle("Add Widget")
                .setMessage("Do you want to add this event as a widget to your home screen?")
                .setPositiveButton("Yes") { _, _ ->
                    val appWidgetHost = AppWidgetHost(this, 1)
                    val widgetId = appWidgetHost.allocateAppWidgetId()

                    val sharedPreferences = getSharedPreferences("WidgetPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("widget_event_$widgetId", event.name)
                    editor.putString("widget_date_$widgetId", event.date)
                    editor.apply()

                    EventWidgetProvider.updateAppWidget(this, AppWidgetManager.getInstance(this), widgetId)


                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, ComponentName(this@MainActivity, EventWidgetProvider::class.java))
                    }
                    widgetResultLauncher.launch(intent)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }


    private fun updateEmptyStateVisibility() {
        if (eventsLists.isEmpty()) {
            emptyList.visibility = View.VISIBLE
            emptyListImage.visibility = View.VISIBLE
        } else {
            emptyList.visibility = View.GONE
            emptyListImage.visibility = View.GONE
        }
    }


    private fun saveEventsToSharedPreferences() {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        editor.putString("events_list", gson.toJson(eventsLists))
        editor.putString("archived_events", gson.toJson(archivedEvents))
        editor.apply()
    }

    // Load events from SharedPreferences
    private fun loadEventsFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val gson = Gson()

        sharedPreferences.getString("events_list", null)?.let {
            val type = object : TypeToken<List<Event>>() {}.type
            eventsLists.addAll(gson.fromJson(it, type))
        }

        sharedPreferences.getString("archived_events", null)?.let {
            val type = object : TypeToken<List<Event>>() {}.type
            archivedEvents.addAll(gson.fromJson(it, type))
        }
    }
}
