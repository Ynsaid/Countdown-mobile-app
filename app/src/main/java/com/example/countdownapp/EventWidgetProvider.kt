package com.example.countdownapp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class EventWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val sharedPreferences = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
            val eventName = sharedPreferences.getString("widget_event_$appWidgetId", "No Event")
            val eventDate = sharedPreferences.getString("widget_date_$appWidgetId", "N/A")
            println("Updating widget $appWidgetId with event: $eventName, date: $eventDate")
            val views = RemoteViews(context.packageName, R.layout.widget_layput).apply {
                setTextViewText(R.id.widget_event_name, eventName)
                setTextViewText(R.id.widget_event_date, eventDate)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}