package com.example.projetmediassist.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Utilisez AlertDialog de androidx.appcompat.app
import androidx.fragment.app.DialogFragment
import com.example.projetmediassist.R
import com.google.android.material.button.MaterialButton // NOUVEAU import pour MaterialButton
import com.google.android.material.textfield.TextInputEditText // NOUVEAU import pour TextInputEditText

class EnterRppsDialogFragment(val onRppsEntered: (String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Gonfler le layout personnalisé
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_enter_rpps, null)

        // Récupérer les vues du layout
        val inputRpps = dialogView.findViewById<TextInputEditText>(R.id.inputRpps)
        val validateButton = dialogView.findViewById<MaterialButton>(R.id.validateRppsButton)

        // Créer l'AlertDialog
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false) // Rpps toujours requis, donc non annulable
            .create()

        // Attacher le listener au bouton Valider (du layout personnalisé)
        validateButton.setOnClickListener {
            val rpps = inputRpps.text.toString().trim()
            if (rpps.isNotEmpty()) {
                onRppsEntered(rpps)
                alertDialog.dismiss() // Fermer la boîte de dialogue après validation
            } else {
                Toast.makeText(context, "Numéro RPPS requis.", Toast.LENGTH_SHORT).show()
                // Vous pouvez aussi ajouter une erreur sur le TextInputLayout:
                // inputRpps.error = "RPPS requis"
            }
        }

        return alertDialog
    }
}