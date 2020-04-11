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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntryKind
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import java.text.DateFormat
import java.util.*

private const val ARG_DREAM_ID = "dream_id"
private const val TAG = "DreamFragment"

class DreamDetailFragment : Fragment() {
    private lateinit var dream: Dream
    private lateinit var dreamEntries: List<DreamEntry>
    private lateinit var dreamWithEntries: DreamWithEntries
    private lateinit var titleField: EditText
    private lateinit var realizedCheckBox: CheckBox
    private lateinit var deferredCheckbox: CheckBox
    private lateinit var viewModel: DreamDetailViewModel

    private var adapter: DreamEntryAdapter? = DreamEntryAdapter(emptyList())
    private lateinit var dreamEntryRecyclerView: RecyclerView

    private var callbacks: Callbacks? = null
    private val df = DateFormat.getDateInstance(DateFormat.MEDIUM)
    interface Callbacks {
        fun onDreamSelected(dreamId: UUID)
    }

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
        realizedCheckBox = view.findViewById(R.id.dream_realized)
        deferredCheckbox = view.findViewById(R.id.dream_deferred)
        dreamEntryRecyclerView = view.findViewById(R.id.dream_entry_recycle_view) as RecyclerView
        dreamEntryRecyclerView.layoutManager = LinearLayoutManager(context)
        dreamEntryRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dreamDetailViewModel.dreamLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { dreamWithEntries ->
                dreamWithEntries?.let {
                    this.dreamWithEntries = it
                    updateUI()
                    updateButtonUI()
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
                dreamWithEntries.dream.description = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        realizedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.realizedCheckboxUpdate(isChecked)
                viewModel.updateDreamWithEntry(dreamWithEntries)
            }
        }

        deferredCheckbox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.deferredCheckboxUpdate(isChecked)
                viewModel.updateDreamWithEntry(dreamWithEntries)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        dreamDetailViewModel.saveDream()
    }

    inner class DreamEntryHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var dreamEntry: DreamEntry
        private val buttonView: Button = itemView.findViewById(R.id.dream_entry_button)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(dreamEntry: DreamEntry) {
            this.dreamEntry = dreamEntry
            when (dreamEntry.kind){
                DreamEntryKind.REVEALED -> {
                    buttonView.apply{
                        setText(dreamEntry.comment)
                        setBackgroundColor(Color.YELLOW)
                    }
                }
                DreamEntryKind.COMMENT -> {
                    buttonView.apply{
                        setText(dreamEntry.comment+'('+df.format(dreamEntry.dateCreated)+')')
                        setBackgroundColor(Color.LTGRAY)
                    }
                }
                DreamEntryKind.REALIZED -> {
                    buttonView.apply{
                        setText(dreamEntry.comment)
                        setBackgroundColor(Color.GREEN)
                    }
                }
                DreamEntryKind.DEFERRED -> {
                    buttonView.apply{
                        setText(dreamEntry.comment)
                        setBackgroundColor(Color.RED)
                        setTextColor(Color.WHITE)
                    }
                }
            }
        }

        override fun onClick(v: View) {
            callbacks?.onDreamSelected(dream.id)
        }
    }

    private inner class DreamEntryAdapter(var dreamEntries: List<DreamEntry>) :
        RecyclerView.Adapter<DreamEntryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamEntryHolder {
            val view = layoutInflater.inflate(R.layout.list_item_dream_entry, parent, false)
            return DreamEntryHolder(view)
        }

        override fun getItemCount() = dreamEntries.size

        override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
            val dreamEntry = dreamEntries[position]
            holder.bind(dreamEntry)
        }

    }

    private fun updateButtonUI() {
        adapter = DreamEntryAdapter(dreamWithEntries.dreamEntries)
        dreamEntryRecyclerView.adapter = adapter
    }

    private fun updateUI() {
        titleField.setText(dreamWithEntries.dream.description)
        realizedCheckBox.apply {
            isChecked = dreamWithEntries.dream.isRealized
            isEnabled = !dreamWithEntries.dream.isDeferred
        }
        deferredCheckbox.apply {
            isChecked = dreamWithEntries.dream.isDeferred
            isEnabled = !dreamWithEntries.dream.isRealized
        }
//        dream_entry_4_button.apply {
//            visibility = View.GONE
//        }

        val df = DateFormat.getDateInstance(DateFormat.MEDIUM)

//        dreamWithEntries.dreamEntries.forEachIndexed { i, entry ->
//            when (entry.kind) {
//                DreamEntryKind.REVEALED -> {
//                    dream_entry_0_button.apply {
//                        text = entry.comment
//                        visibility = View.VISIBLE
//                        setBackgroundColor(Color.YELLOW)
//                    }
//                }
//                DreamEntryKind.COMMENT -> {
//                    detailButtons[i].apply {
//                        text = entry.comment + "(" + df.format(entry.dateCreated) + ")"
//                        visibility = View.VISIBLE
//                        setBackgroundColor(Color.LTGRAY)
//                    }
//                }
//                DreamEntryKind.REALIZED -> {
//                    dream_entry_4_button.apply {
//                        text = entry.comment
//                        visibility = View.VISIBLE
//                        setBackgroundColor(Color.GREEN)
//                    }
//                }
//                DreamEntryKind.DEFERRED -> {
//                    dream_entry_4_button.apply {
//                        text = entry.comment
//                        visibility = View.VISIBLE
//                        setBackgroundColor(Color.RED)
//                    }
//                }
//            }
//        }
    }
}
