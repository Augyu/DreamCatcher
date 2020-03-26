package edu.vt.cs.cs5254.dreamcatcher

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import java.text.DateFormat
import java.util.*

private const val ARG_DREAM_ID = "dream_id"
private const val TAG = "DreamFragment"

class DreamDetailFragment : Fragment() {
    private lateinit var dream: Dream
    private lateinit var dreamEntries: List<DreamEntry>
    private lateinit var titleField: EditText
    private lateinit var entryButton: Button
    private lateinit var realizedCheckBox: CheckBox
    private lateinit var deferredCheckbox: CheckBox
    private lateinit var viewModel: DreamDetailViewModel
    private lateinit var dream_1_Button: Button
    private lateinit var dream_2_Button: Button
    private lateinit var dream_3_Button: Button
    private lateinit var realizedButton: Button
    private lateinit var detailButtons: List<Button>

    private val dreamDetailViewModel: DreamDetailViewModel by lazy {
        ViewModelProviders.of(this).get(DreamDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dream = Dream()
        dreamEntries = listOf()
        val dreamId: UUID = arguments?.getSerializable(ARG_DREAM_ID) as UUID
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dream_detail_fragment, container, false)
        titleField = view.findViewById(R.id.dream_title)
        entryButton = view.findViewById(R.id.dream_entry_0_button)
        realizedCheckBox = view.findViewById(R.id.dream_realized)
        deferredCheckbox = view.findViewById(R.id.dream_deferred)
        dream_1_Button = view.findViewById(R.id.dream_entry_1_button)
        dream_2_Button = view.findViewById(R.id.dream_entry_2_button)
        dream_3_Button = view.findViewById(R.id.dream_entry_3_button)
        realizedButton = view.findViewById(R.id.dream_entry_4_button)
        detailButtons =
            listOf(entryButton, dream_1_Button, dream_2_Button, dream_3_Button, realizedButton)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dreamDetailViewModel.dreamLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { dreamWithEntries ->
                dreamWithEntries?.let {
                    this.dreamEntries = it.dreamEntries
                    this.dream = it.dream
                    updateUI()
                }
            }
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DreamDetailViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dream.description = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        realizedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                dream.isRealized = isChecked
                deferredCheckbox.isEnabled = !dream.isRealized
                detailButtons[dreamEntries.count() - 1].text = "Dream Realized"
                detailButtons[dreamEntries.count() - 1].setBackgroundColor(Color.RED)
            }
        }
        deferredCheckbox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                dream.isDeferred = isChecked
                realizedCheckBox.isEnabled = !dream.isDeferred
                detailButtons[dreamEntries.count() - 1].text = "Dream Deferred"
                detailButtons[dreamEntries.count()-1].setBackgroundColor(Color.GRAY)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        dreamDetailViewModel.saveDream()
    }

    private fun updateUI() {
        titleField.setText(dream.description)
        realizedCheckBox.apply {
            isChecked = dream.isRealized
            isEnabled = !dream.isDeferred
        }
        deferredCheckbox.apply {
            isChecked = dream.isDeferred
            isEnabled = !dream.isRealized
        }
        val df = DateFormat.getDateInstance(DateFormat.MEDIUM)

        dreamEntries.forEachIndexed { i, entry ->
            if (entry.kind.toString() == "COMMENT") {
                detailButtons[i].text = entry.comment + "(" + df.format(entry.dateCreated) + ")"
                detailButtons[i].setBackgroundColor(Color.LTGRAY)
            } else {
                if (i == 0) {
                    detailButtons[i].setBackgroundColor(Color.GREEN)
                } else {
                    if (dream.isDeferred) {
                        detailButtons[i].setBackgroundColor(Color.GRAY)
                    } else {
                        detailButtons[i].setBackgroundColor(Color.RED)
                    }
                }
                detailButtons[i].setText(entry.comment)
            }

            detailButtons[i].visibility = View.VISIBLE
        }
    }
}
