package edu.vt.cs.cs5254.dreamcatcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class DreamActivity : AppCompatActivity(),
    DreamListFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = DreamListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onDreamSelected(dreamId: UUID) {
        val fragment = DreamDetailFragment.newInstance(dreamId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
