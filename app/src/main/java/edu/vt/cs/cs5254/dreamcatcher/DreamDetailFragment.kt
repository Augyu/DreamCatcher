package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import kotlinx.android.synthetic.main.dream_detail_fragment.*
import java.util.*

private const val ARG_DREAM_ID = "dream_id"
private const val TAG = "DreamFragment"

class DreamDetailFragment : Fragment() {
    private lateinit var dream: Dream
    private lateinit var dreamEntries: List<DreamEntry>
    private lateinit var titleField: EditText
    private lateinit var entryButton: Button
    private lateinit var realizedCheckBox: CheckBox
    private val dreamDetailViewModel: DreamDetailViewModel by lazy {
        ViewModelProviders.of(this).get(DreamDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dream = Dream()
        dreamEntries = listOf()
        val dreamId: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
        Log.d(TAG, "args bundle dream ID: $dreamId")
        dreamDetailViewModel.loadDream(dreamId)
    }

    companion object {
        fun newInstance(dreamId: UUID): DreamDetailFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DREAM_ID, dreamId)
            }
            return DreamDetailFragment().apply {
                arguments = args
            }
        }
    }

    private lateinit var viewModel: DreamDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dream_detail_fragment, container, false)
        titleField = view.findViewById(R.id.dream_title)
        entryButton = view.findViewById(R.id.dream_entry_1_button)
        realizedCheckBox = view.findViewById(R.id.dream_realized_icon)
        entryButton.apply {
            text = dream.dateRevealed.toString()
            isEnabled = false
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dreamDetailViewModel.dreamLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { dreamEntries ->
                dreamEntries?.let {
                    this.dreamEntries = dreamEntries.dreamEntries
                    dreamEntries.dreamEntries.forEach { Log.d("test", "ui ${it.id}") }
                    updateUI()
                }
            }
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DreamDetailViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dream.description = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        realizedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                dream.isRealized = isChecked
            }
        }
    }
    override fun onStop(){
        super.onStop()
        dreamDetailViewModel.saveDream()
    }
    private fun updateUI(){
        titleField.setText(dream.description)
        dream_entry_0_button.text = dream.dateRevealed.toString()
        realizedCheckBox.isChecked = dream.isRealized
    }
}
