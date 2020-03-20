package edu.vt.cs.cs5254.dreamcatcher.database

import androidx.room.Embedded
import androidx.room.Relation

class DreamWithEntries(
    @Embedded var dream: Dream,

    @Relation(
        parentColumn = "id",
        entityColumn = "dreamId"
    ) var dreamEntries: List<DreamEntry>
)
