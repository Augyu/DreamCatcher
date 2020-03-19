package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.dreamcatcher.database.Dream

class DreamListViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    val dreams = mutableListOf<Dream>()

    init {
        for (i in 0 until 100) {
            val dream = Dream()
            dream.description = "Dream #$i"
            dream.isRealized = i % 2 == 0
            dreams += dream
        }
    }
}
