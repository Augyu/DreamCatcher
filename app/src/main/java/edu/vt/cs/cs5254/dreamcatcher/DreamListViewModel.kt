package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.dreamcatcher.database.Dream

class DreamListViewModel : ViewModel() {
    private val dreamRepository = DreamRepository.get()
    val dreamsListLiveData = dreamRepository.getDreams()

    fun addDream(dream: Dream) {
        dreamRepository.addDream(dream)
    }

    fun deleteAllDreams() {
        dreamRepository.deleteAllDreams()
    }
}
