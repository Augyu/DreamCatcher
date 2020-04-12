package edu.vt.cs.cs5254.dreamcatcher.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity

object KeyboardUtil {

    private const val TAG = "KeyboardUtil"

    // See https://stackoverflow.com/questions/1109022/close-hide-android-soft-keyboard

    /**
     * Use to hide soft keyboard when a dialog has focus
     */
    fun hideSoftKeyboard(context: Context, view: View) {
        Log.d(TAG, "Hiding soft keyboard $view")
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Use to hide soft keyboard when activity or fragment has focus
     */
    fun hideSoftKeyboard(activity: ComponentActivity?) {
        val view = activity?.currentFocus
        Log.d(TAG, "Hiding soft keyboard $view")
        view?.let { v ->
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            Log.d(TAG, "Hiding soft keyboard")
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}