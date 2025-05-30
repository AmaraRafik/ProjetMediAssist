package com.example.projetmediassist.fragments  // ou dialogs si tu l’as mis ailleurs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class EnterRppsDialogFragment(val onRppsEntered: (String) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "Numéro RPPS"

        return AlertDialog.Builder(requireContext())
            .setTitle("Entrez votre numéro RPPS")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Valider") { _, _ ->
                val rpps = input.text.toString().trim()
                if (rpps.isNotEmpty()) {
                    onRppsEntered(rpps)
                } else {
                    Toast.makeText(context, "RPPS requis", Toast.LENGTH_SHORT).show()
                }
            }
            .create()
    }
}
