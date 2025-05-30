package com.example.projetmediassist.utils

import android.content.Context
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object CalendarUtils {

    fun getCredential(context: Context, accountName: String): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(
            context, listOf(CalendarScopes.CALENDAR)
        ).apply {
            selectedAccountName = accountName
        }
    }

    suspend fun addEvent(
        context: Context,
        accountName: String,
        title: String,
        description: String,
        startMillis: Long,
        endMillis: Long = startMillis + 60 * 60 * 1000
    ): String? = suspendCoroutine { cont ->
        try {
            val credential = getCredential(context, accountName)

            val service = Calendar.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("MediAssist").build()

            val event = Event()
                .setSummary(title)
                .setDescription(description)
                .setStart(
                    EventDateTime().setDateTime(DateTime(startMillis)).setTimeZone("Europe/Paris")
                )
                .setEnd(
                    EventDateTime().setDateTime(DateTime(endMillis)).setTimeZone("Europe/Paris")
                )

            val createdEvent = service.events().insert("primary", event).execute()
            Log.d("CALENDAR", "✅ Événement ajouté : ${createdEvent.htmlLink}")
            cont.resume(createdEvent.id) // ← on retourne l'ID ici

        } catch (e: Exception) {
            Log.e("CALENDAR", "❌ Erreur lors de l'ajout à Google Calendar", e)
            cont.resume(null)
        }
    }

}
