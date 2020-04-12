package edu.vt.cs.cs5254.dreamcatcher

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntryKind
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import java.io.File
import java.util.*

class DreamDetailViewModel : ViewModel() {
    private val dreamRepository = DreamRepository.get()
    private val dreamIdLiveData = MutableLiveData<UUID>()

    var dreamLiveData: LiveData<DreamWithEntries?> =
        Transformations.switchMap(dreamIdLiveData) { dreamId ->
            dreamRepository.getDreamWithEntries(dreamId)
        }

    fun loadDream(dreamId: UUID) {
        dreamIdLiveData.value = dreamId
    }

    fun saveDream() {
        dreamLiveData.value?.let {
            dreamRepository.updateDreamWithEntries(it)
        }
    }

    fun deferredCheckboxUpdate(isCheck: Boolean) {
        dreamLiveData.value?.let {
            it.dream.isDeferred = isCheck
            it.dreamEntries =
                it.dreamEntries - it.dreamEntries.filter { dreamEntry -> dreamEntry.kind == DreamEntryKind.REALIZED || dreamEntry.kind == DreamEntryKind.DEFERRED}
            if (isCheck) {
                it.dreamEntries = it.dreamEntries + DreamEntry(
                    dreamId = it.dream.id,
                    kind = DreamEntryKind.DEFERRED,
                    comment = "Dream Deferred"
                )
            }
        }
    }

    fun realizedCheckboxUpdate(isCheck: Boolean) {
        dreamLiveData.value?.let {
            it.dream.isRealized = isCheck
            it.dreamEntries =
                it.dreamEntries - it.dreamEntries.filter { dreamEntry -> dreamEntry.kind == DreamEntryKind.DEFERRED || dreamEntry.kind == DreamEntryKind.REALIZED}
            if (isCheck) {
                it.dreamEntries = it.dreamEntries + DreamEntry(
                    dreamId = it.dream.id,
                    kind = DreamEntryKind.REALIZED,
                    comment = "Dream Realized"
                )
            }
        }
    }

    fun updateDreamWithEntry(dreamWithEntries: DreamWithEntries) {
        dreamRepository.updateDreamWithEntries(dreamWithEntries)
    }

    fun updateDreamEntries(dreamEntries: List<DreamEntry>){
        Log.d("test", "${dreamEntries.size}")
        dreamLiveData.value?.let{
            it.dreamEntries = dreamEntries
            dreamRepository.updateDreamWithEntries(it)
        }

    }

    fun getPhotoFile(dreamWithEntries: DreamWithEntries): File {
        return dreamRepository.getPhotoFile(dreamWithEntries.dream)
    }
}
