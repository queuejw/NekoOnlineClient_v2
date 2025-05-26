package ru.neko.online.client.fragment.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neko.online.client.R
import ru.neko.online.client.components.Cat

class HomeFragment : Fragment(R.layout.home_fragment) {

    private var recyclerView: RecyclerView? = null
    private var mAdapter: CatAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.cat_list)
        recyclerView?.let {
            mAdapter = CatAdapter(it.context)
            it.adapter = mAdapter
            it.layoutManager = GridLayoutManager(it.context, 3)
        }
    }
}

private class CatAdapter(val context: Context) : RecyclerView.Adapter<CatHolder?>() {

    private var mCats: MutableList<Cat>
    private val size = context.resources.getDimensionPixelSize(R.dimen.neko_display_size)

    init {
        mCats = ArrayList<Cat>()
    }

    fun setCats(cats: MutableList<Cat>) {
        mCats = cats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatHolder {
        return CatHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.cat_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CatHolder, position: Int) {
        holder.imageView.setImageBitmap(mCats[position].createBitmap(size, size))
        holder.textView.text = mCats[position].name
    }

    override fun getItemCount(): Int {
        return mCats.size
    }
}

private class CatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById<ImageView>(R.id.cat_icon)
    val textView: MaterialTextView = itemView.findViewById<MaterialTextView>(R.id.cat_title)
}