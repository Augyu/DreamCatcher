package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs.cs5254.dreamcatcher.database.Dream

private const val TAG = "DreamListFragment"

class DreamListFragment : Fragment() {
    private lateinit var dreamRecyclerView: RecyclerView
    private var adapter: DreamAdapter? = null
    private lateinit var viewModel: DreamListViewModel
    private val dreamListViewModel: DreamListViewModel by lazy {
        ViewModelProvider(this).get(DreamListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Total dreams ${dreamListViewModel.dreams.size}")
    }

    companion object {
        fun newInstance() = DreamListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dream_list_fragment, container, false)
        dreamRecyclerView = view.findViewById(R.id.dream_recycler_view) as RecyclerView
        dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        updateUI()
        return view
    }

    private fun updateUI() {
        val dreams = dreamListViewModel.dreams
        adapter = DreamAdapter(dreams)
        dreamRecyclerView.adapter = adapter
    }

    private inner class DreamHolder(view: View) : RecyclerView.ViewHolder(view) ,View.OnClickListener{
        private lateinit var dream: Dream
        val titleTextView: TextView = itemView.findViewById(R.id.dream_title)
        val dateTextView: TextView = itemView.findViewById(R.id.dream_date)
        init {
            itemView.setOnClickListener(this)
        }
        fun bind(dream: Dream) {
            this.dream = dream
            titleTextView.text = this.dream.description
            dateTextView.text = this.dream.dateRevealed.toString()
        }

        override fun onClick(v:View){
            Toast.makeText(context, "${dream.description} pressed!", Toast.LENGTH_SHORT).show()
        }
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
        viewModel = ViewModelProviders.of(this).get(DreamListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
