package com.example.projetmediassist.utils

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes // Crucial import

object AuthUtils {

    /**
     * Récupère le dernier compte Google connecté qui a accordé la permission Calendar.
     * Renvoie null si aucun compte n'est connecté ou si la permission n'a pas été accordée.
     */
    fun getLastSignedInAccountWithCalendarScope(context: Context): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        // Vérifie si le compte existe et si la permission Calendar a été accordée
        if (account != null && GoogleSignIn.hasPermissions(account, Scope(CalendarScopes.CALENDAR))) {
            return account
        }
        return null
    }

    /**
     * Récupère l'email du dernier compte Google connecté ayant la permission Calendar.
     */
    fun getGoogleAccountEmailWithCalendarScope(context: Context): String? {
        return getLastSignedInAccountWithCalendarScope(context)?.email
    }

    /**
     * Vérifie si l'utilisateur est connecté avec Google et a accordé la permission Calendar.
     */
    fun isGoogleSignedInAndCalendarScopeGranted(context: Context): Boolean {
        return getLastSignedInAccountWithCalendarScope(context) != null
    }
}