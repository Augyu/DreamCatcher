package edu.vt.cs.cs5254.dreamcatcher

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.database.Dream
import java.security.acl.Owner
import java.util.*

private const val TAG = "DreamListFragment"

class DreamListFragment : Fragment() {
    private lateinit var dreamRecyclerView: RecyclerView
    private var adapter: DreamAdapter? = DreamAdapter(emptyList())
    //    private lateinit var viewModel: DreamListViewModel
    private val dreamListViewModel: DreamListViewModel by lazy {
        ViewModelProvider(this).get(DreamListViewModel::class.java)
    }
    private var callbacks: Callbacks? = null

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("test", "Total dreams")
    }
    interface Callbacks {
        fun onDreamSelected(dreamId: UUID)
    }

    companion object {
        fun newInstance() = DreamListFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dream_list_fragment, container, false)
        dreamRecyclerView = view.findViewById(R.id.dream_recycler_view) as RecyclerView
        dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        dreamRecyclerView.adapter = adapter
        return view
    }

    private fun updateUI(dreams: List<Dream>) {
        adapter = DreamAdapter(dreams)
        dreamRecyclerView.adapter = adapter
    }

    private inner class DreamHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var dream: Dream
        private val titleTextView: TextView = itemView.findViewById(R.id.dream_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.dream_date)
        private val realizedImageView: ImageView = itemView.findViewById(R.id.dream_realized_icon)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(dream: Dream) {
            this.dream = dream
            titleTextView.text = this.dream.description
            dateTextView.text = this.dream.dateRevealed.toString()
            realizedImageView.visibility = if (dream.isRealized) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) {
//            val fragment = DreamDetailFragment.newInstance(dream.id)
//            val fm = activity.supportFragmentManager
//            fm.beginTransaction()
//                .replace(R.id.fragment_container, fragment)
//                .commit()
            callbacks?.onDreamSelected(dream.id)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dreamListViewModel.dreamsListLiveData.observe(
            viewLifecycleOwner,
            Observer { dreams ->
                dreams?.let {
                    Log.i(TAG, "Got dreams $(dreams.size)")
                    updateUI(dreams)
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private inner class DreamAdapter(var dreams: List<Dream>) :
        RecyclerView.Adapter<DreamHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamHolder {
            val view = layoutInflater.inflate(R.layout.list_item_dream, parent, false)
            return DreamHolder(view)
        }

        override fun getItemCount() = dreams.size

        override fun onBindViewHolder(holder: DreamHolder, position: Int) {
            val dream = dreams[position]
            holder.bind(dream)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProviders.of(this).get(DreamListViewModel::class.java)
        // TODO: Use the ViewModel
//        dreamListViewModel.dreams.observe(viewLifecycleOwner, Observer {
//            Log.d("test", "observer")
//        })
    }


}

