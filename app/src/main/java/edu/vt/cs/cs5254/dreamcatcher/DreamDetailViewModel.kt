package edu.vt.cs.cs5254.dreamcatcher

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import java.util.UUID

class DreamDetailViewModel : ViewModel() {
    private val dreamRepository = DreamRepository.get()
    private val dreamIdLiveData = MutableLiveData<UUID>()

    var dreamLiveData: LiveData<DreamWithEntries?> = Transformations.switchMap(dreamIdLiveData) { dreamId ->
        dreamRepository.getDreamWithEntries(dreamId)
    }

    fun loadDream(dreamId: UUID) {
        dreamIdLiveData.value = dreamId
    }

//    fun saveDream(dreamWithEntries: DreamWithEntries){
//        dreamRepository.updateDreamWithEntries(dreamWithEntries)
//    }

    fun saveDream(){
        dreamLiveData.value?.let {
            dreamRepository.updateDreamWithEntries(it)
            it.dreamEntries.forEach {
                Log.d("test", "${it.id}")
            }
        }

    }
}
