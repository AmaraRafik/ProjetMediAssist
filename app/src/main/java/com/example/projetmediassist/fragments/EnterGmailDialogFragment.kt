package com.example.projetmediassist.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.projetmediassist.R

class EnterGmailDialogFragment(
    private val onEmailEntered: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editText = EditText(requireContext())
        editText.hint = getString(R.string.entergmail_dialog_hint)

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.entergmail_dialog_title))
            .setMessage(getString(R.string.entergmail_dialog_message))
            .setView(editText)
            .setPositiveButton(getString(R.string.entergmail_dialog_ok)) { _, _ ->
                val email = editText.text.toString().trim()
                if (email.endsWith("@gmail.com")) {
                    onEmailEntered(email)
                }
            }
            .setNegativeButton(getString(R.string.entergmail_dialog_cancel), null)
            .create()
    }
}

