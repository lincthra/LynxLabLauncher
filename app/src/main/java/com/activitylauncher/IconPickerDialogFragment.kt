package com.activitylauncher

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog // For creating a simple dialog
import android.util.Log

class IconPickerDialogFragment : DialogFragment() {

    private var listener: ConfigActivity.IconSelectionListener? = null
    private val TAG = "IconPickerDialog"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ConfigActivity.IconSelectionListener) {
            listener = context
        } else {
            // Log the error for easier debugging if something goes wrong in the environment
            Log.e(TAG, "$context must implement ConfigActivity.IconSelectionListener")
            // As per common practice, throw an exception if the listener isn't implemented
            // This was not explicitly requested but is good practice.
            // The subtask mentions "ensure the listener is correctly assigned",
            // and this check helps ensure that.
            throw RuntimeException("$context must implement ConfigActivity.IconSelectionListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // This is a placeholder for the actual dialog creation.
        // A real implementation would inflate a custom layout with icons.
        // For now, let's create a simple dialog with a few dummy icon options.
        val dummyIcons = arrayOf("icon_path_1.png", "icon_path_2.png", "icon_path_3.png")

        return AlertDialog.Builder(requireActivity())
            .setTitle("Select Icon")
            .setItems(dummyIcons) { _, which ->
                val selectedIconPath = dummyIcons[which]
                Log.d(TAG, "Dummy icon selected: $selectedIconPath")
                // Call the listener's method to pass back the selected icon path
                listener?.onIconSelected(selectedIconPath)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null // Clean up the listener to avoid potential memory leaks
    }

    // This method could be called by the dialog's UI when an icon is actually picked.
    // For this example, the selection is done directly in onCreateDialog's setISingleChoiceItems.
    // public fun exampleNotifyIconSelected(iconPath: String) {
    //     listener?.onIconSelected(iconPath)
    // }
}
