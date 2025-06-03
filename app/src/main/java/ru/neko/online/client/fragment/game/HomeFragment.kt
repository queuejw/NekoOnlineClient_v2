package ru.neko.online.client.fragment.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neko.online.client.R
import ru.neko.online.client.components.Cat
import ru.neko.online.client.components.models.CatModel
import ru.neko.online.client.components.viewmodels.MainViewModel
import kotlin.getValue

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
            mAdapter?.setNewCats(getNormalCatList(it, requireContext()))
        }
        recyclerView?.let {
            it.adapter = mAdapter
            it.layoutManager = GridLayoutManager(it.context, 3)
        }
    }

    private fun getNormalCatList(data: MutableList<CatModel>, context: Context): MutableList<Cat> {
        val newList = ArrayList<Cat>()
        data.forEach {
            newList.add(Cat(context, it.seed))
        }
        return newList
    }
}

private class CatAdapter(val context: Context, var cats: MutableList<Cat>) : RecyclerView.Adapter<CatHolder?>() {

    private val size = context.resources.getDimensionPixelSize(R.dimen.neko_display_size)

    fun setNewCats(newCats: MutableList<Cat>) {
        cats = newCats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatHolder {
        return CatHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.cat_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CatHolder, position: Int) {
        holder.imageView.setImageBitmap(cats[position].createBitmap(size, size))
        holder.textView.text = cats[position].name
    }

    override fun getItemCount(): Int {
        return cats.size
    }
}

private class CatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById<ImageView>(R.id.cat_icon)
    val textView: MaterialTextView = itemView.findViewById<MaterialTextView>(R.id.cat_title)
}