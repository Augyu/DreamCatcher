package edu.vt.cs.cs5254.dreamcatcher

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.database.DreamEntryKind
import edu.vt.cs.cs5254.dreamcatcher.database.DreamWithEntries
import edu.vt.cs.cs5254.dreamcatcher.util.CameraUtil
import java.io.File
import java.text.DateFormat
import java.util.*

private const val ARG_DREAM_ID = "dream_id"
private const val TAG = "DreamFragment"

class DreamDetailFragment : Fragment() {

    //model fields
    private lateinit var dreamWithEntries: DreamWithEntries
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    //view fields
    private lateinit var titleField: EditText
    private lateinit var realizedCheckBox: CheckBox
    private lateinit var deferredCheckbox: CheckBox
    private lateinit var viewModel: DreamDetailViewModel
    private lateinit var dreamIcon: ImageView
    private var adapter: DreamEntryAdapter? = DreamEntryAdapter(emptyList())
    private lateinit var dreamEntryRecyclerView: RecyclerView
    private lateinit var photoView: ImageView

    private var callbacks: Callbacks? = null
    private val df = DateFormat.getDateInstance(DateFormat.MEDIUM)

    private val titleWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            dreamWithEntries.dream.description = s.toString()
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    interface Callbacks {
        fun onDreamSelected(dreamId: UUID)
    }

    private val dreamDetailViewModel: DreamDetailViewModel by lazy {
        ViewModelProviders.of(this).get(DreamDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        dreamEntryRecyclerView = view.findViewById(R.id.dream_entry_recycler_view) as RecyclerView
        dreamIcon = view.findViewById(R.id.dream_icon)
        photoView = view.findViewById(R.id.dream_photo) as ImageView
        dreamEntryRecyclerView.layoutManager = LinearLayoutManager(context)
        dreamEntryRecyclerView.adapter = adapter
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback())
        itemTouchHelper.attachToRecyclerView(dreamEntryRecyclerView)
        return view
    }

    // options menu methods
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_dream, menu)
        val cameraAvailable = CameraUtil.isCameraAvailable(requireActivity())
        val menuItem = menu.findItem(R.id.take_dream_photo)
        val menuShare = menu.findItem(R.id.share_dream)
        menuItem.apply {
            Log.d(TAG, "Camera available: $cameraAvailable")
            isEnabled = cameraAvailable
            isVisible = cameraAvailable
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.take_dream_photo -> {
                val captureImageIntent =
                    CameraUtil.createCaptureImageIntent(requireActivity(), photoUri)
                startActivity(captureImageIntent)
                true
            }
            R.id.share_dream -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getDreamReport())
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        dreamWithEntries.dream.description
                    ).also { intent ->
                        val chooserIntent =
                            Intent.createChooser(intent, getString(R.string.send_report))
                        startActivity(chooserIntent)
                    }
                }
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dreamDetailViewModel.dreamLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { dreamWithEntries ->
                dreamWithEntries?.let {
                    this.dreamWithEntries = it
                    photoFile = dreamDetailViewModel.getPhotoFile(dreamWithEntries)
                    photoUri = FileProvider.getUriForFile(
                        requireActivity(),
                        "edu.vt.cs.cs5254.dreamcatcher.fileprovider",
                        photoFile
                    )
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
        titleField.removeTextChangedListener(titleWatcher)
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
            when (dreamEntry.kind) {
                DreamEntryKind.REVEALED -> {
                    buttonView.apply {
                        setText(dreamEntry.comment)
                        setBackgroundColor(Color.YELLOW)
                    }
                }
                DreamEntryKind.COMMENT -> {
                    buttonView.apply {
                        setText(dreamEntry.comment + '(' + df.format(dreamEntry.dateCreated) + ')')
                        setBackgroundColor(Color.LTGRAY)
                    }
                }
                DreamEntryKind.REALIZED -> {
                    buttonView.apply {
                        setText(dreamEntry.comment)
                        setBackgroundColor(Color.GREEN)
                    }
                }
                DreamEntryKind.DEFERRED -> {
                    buttonView.apply {
                        setText(dreamEntry.comment)
                        setBackgroundColor(Color.RED)
                        setTextColor(Color.WHITE)
                    }
                }
            }
        }

        override fun onClick(v: View) {
            callbacks?.onDreamSelected(dreamEntry.id)
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

        fun deleteItem(position: Int) {
            val dreamToDelete = dreamEntries[position]
            Log.d("test", "${dreamEntries.size}")
            if (dreamToDelete.kind == DreamEntryKind.COMMENT) {
                dreamEntries = dreamEntries - dreamToDelete
                dreamDetailViewModel.updateDreamEntries(dreamEntries)
                notifyItemRemoved(position)
            } else {
                notifyItemChanged(position)
            }
        }
    }

    inner class SwipeToDeleteCallback :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter?.deleteItem(position)
        }
    }

    private fun updateButton() {
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
        when {
            dreamWithEntries.dream.isRealized -> {
                dreamIcon.setImageResource(R.drawable.dream_realized_icon)
                dreamIcon.tag = R.drawable.dream_realized_icon
            }
            dreamWithEntries.dream.isDeferred -> {
                dreamIcon.setImageResource(R.drawable.dream_deferred_icon)
                dreamIcon.tag = R.drawable.dream_deferred_icon
            }
            else -> {
                dreamIcon.setImageResource(0)
                dreamIcon.tag = 0
            }
        }
        updatePhotoView()
        updateButton()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = CameraUtil.getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    private fun getDreamReport(): String {
        var report = "# " + dreamWithEntries.dream.description + "\n"
        dreamWithEntries.dreamEntries.forEach {
            report += it.comment + " (" + df.format(it.dateCreated) + ")\n"
        }
        return report
    }
}
