package com.example.lynxlauncher

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class IconPickerDialogFragment : DialogFragment() {

    private var listener: ConfigActivity.IconSelectionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure the host activity implements the callback interface
        if (context is ConfigActivity.IconSelectionListener) {
            listener = context
        } else {
            // Fallback for targetFragment, though less common for Activity communication
            if (targetFragment is ConfigActivity.IconSelectionListener) {
                listener = targetFragment as ConfigActivity.IconSelectionListener
            } else {
                throw RuntimeException("$context must implement IconSelectionListener or set target fragment")
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_icon_picker, null)

        val recyclerViewIcons = view.findViewById<RecyclerView>(R.id.recyclerViewIcons)
        
        // Use the availableIcons list from ConfigActivity's companion object
        val icons = ConfigActivity.availableIcons
        
        val adapter = IconPickerAdapter(icons) { iconResId, iconIdentifier ->
            listener?.onIconSelected(iconResId, iconIdentifier)
            dismiss() // Dismiss the dialog after selection
        }

        recyclerViewIcons.adapter = adapter
        // Adjust span count as needed, e.g., 4 icons per row
        recyclerViewIcons.layoutManager = GridLayoutManager(context, 4) 

        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            // No explicit positive/negative buttons needed if selection dismisses dialog
            // .setTitle(R.string.dialog_title_select_icon) // Title is in the custom layout
            .create()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null // Avoid memory leaks
    }
}
