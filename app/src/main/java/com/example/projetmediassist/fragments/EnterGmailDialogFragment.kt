package com.example.projetmediassist.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class EnterGmailDialogFragment(
    private val onEmailEntered: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editText = EditText(requireContext())
        editText.hint = "Entrez votre adresse Gmail"

        return AlertDialog.Builder(requireContext())
            .setTitle("Synchronisation Google Calendar")
            .setMessage("Vous n'êtes pas connecté avec Google. Fournissez une adresse Gmail.")
            .setView(editText)
            .setPositiveButton("Valider") { _, _ ->
                val email = editText.text.toString().trim()
                if (email.endsWith("@gmail.com")) {
                    onEmailEntered(email)
                }
            }
            .setNegativeButton("Annuler", null)
            .create()
    }
}
