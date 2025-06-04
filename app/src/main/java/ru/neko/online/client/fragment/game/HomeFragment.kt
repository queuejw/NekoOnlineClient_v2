package ru.neko.online.client.fragment.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neko.online.client.R
import ru.neko.online.client.components.Cat
import ru.neko.online.client.components.models.CatModel
import ru.neko.online.client.components.viewmodels.MainViewModel

class HomeFragment : Fragment(R.layout.home_fragment) {

    private var recyclerView: RecyclerView? = null
    private var mAdapter: CatAdapter? = null

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.cat_list)
        val catsData = viewModel.catsLiveData
        catsData.value?.let { data ->
            mAdapter = CatAdapter(requireContext(), getNormalCatList(data, requireContext()))
        }
        catsData.observe(viewLifecycleOwner) {
            context?.let { context ->
                mAdapter?.setNewCats(getNormalCatList(it, context))
            }
        }
        recyclerView?.let {
            it.adapter = mAdapter
            it.layoutManager = GridLayoutManager(it.context, 3)
        }
    }

    private fun getNormalCatList(data: MutableList<CatModel>, context: Context): MutableList<Cat> {
        val newList = ArrayList<Cat>()
        data.forEach {
            newList.add(Cat(context, it.seed, it.name, it.id))
        }
        return newList
    }

    internal inner class CatAdapter(val context: Context, var cats: MutableList<Cat>) :
        RecyclerView.Adapter<CatHolder?>() {

        fun setNewCats(newCats: MutableList<Cat>) {
            val callback = CatsDiff(cats, newCats)
            val result = DiffUtil.calculateDiff(callback)
            cats = newCats
            result.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatHolder {
            return CatHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.cat_view, parent, false)
            )
        }

        override fun onBindViewHolder(holder: CatHolder, position: Int) {
            holder.apply {
                imageView.setImageBitmap(viewModel.getIconFromCache(cats[position].id!!.toInt()))
                textView.text = cats[position].name
            }
        }

        override fun getItemCount(): Int {
            return cats.size
        }
    }
}

internal class CatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: AppCompatImageView = itemView.findViewById<AppCompatImageView>(R.id.cat_icon)
    val textView: MaterialTextView = itemView.findViewById<MaterialTextView>(R.id.cat_title)
}

class CatsDiff(val old: MutableList<Cat>, val new: MutableList<Cat>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return old[oldItemPosition].id == new[newItemPosition].id
    }
}