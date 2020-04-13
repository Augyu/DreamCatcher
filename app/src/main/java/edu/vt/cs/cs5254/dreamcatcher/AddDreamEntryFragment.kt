package edu.vt.cs.cs5254.dreamcatcher

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import java.util.*

class AddDreamEntryFragment : DialogFragment() {
    private lateinit var commentText: EditText
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_comment, null)
        commentText = dialogView.findViewById(R.id.comment_text)

        return AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle(R.string.add_commite_title)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                targetFragment?.let { fragment ->
                    Log.d("test", "add dream frag")
                    (fragment as Callbacks).onCommentCreated(commentText.text.toString(), Date())
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
            }
            .create()
    }

    companion object {
        fun newInstance() = AddDreamEntryFragment()
    }

    interface Callbacks {
        fun onCommentCreated(comment: String, createDate: Date)
    }
}