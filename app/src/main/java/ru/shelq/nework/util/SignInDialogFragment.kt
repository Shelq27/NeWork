package ru.shelq.nework.util

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.shelq.nework.R

class SignInDialogFragment : DialogFragment() {
    @Override
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(getString(R.string.sign_in_dialog))
                .setPositiveButton(
                    R.string.sign_in
                ) { _, _ ->
                    findNavController().navigate(R.id.signInFragment)
                }
                .setNegativeButton(getString(R.string.dialog_cancel)
                ) { _, _ ->
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}