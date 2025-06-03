package ru.neko.online.client.fragment.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neko.online.client.R
import ru.neko.online.client.components.AccountPrefs
import ru.neko.online.client.components.models.UserprefsModel
import ru.neko.online.client.components.viewmodels.MainViewModel

class UserFragment : Fragment(R.layout.user_fragment) {

    private var recyclerView: RecyclerView? = null
    private var mAdapter: UserprefsAdapter? = null

    private val viewModel: MainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.userprefs_recyclerview)
        context?.let {
            val userprefsData = viewModel.userprefsLiveData
            userprefsData.value?.let { data ->
                mAdapter = UserprefsAdapter(data, it)
            }
            userprefsData.observe(viewLifecycleOwner) {
                mAdapter?.updateData(it)
            }
            val lm = LinearLayoutManager(it)
            recyclerView?.apply {
                adapter = mAdapter
                layoutManager = lm
            }
        }
    }
}

class UserprefsAdapter(var data: MutableList<UserprefsModel>, val context: Context): RecyclerView.Adapter<UserprefsHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserprefsHolder {
        return UserprefsHolder(LayoutInflater.from(parent.context).inflate(R.layout.userpref_holder, parent, false))
    }

    fun updateData(newData: MutableList<UserprefsModel>) {
        val diffCallback = UserprefsDiff(data, newData)
        val result = DiffUtil.calculateDiff(diffCallback)
        data = newData
        result.dispatchUpdatesTo(this)
    }

    override fun onBindViewHolder(
        holder: UserprefsHolder,
        position: Int
    ) {
        val item = data[position]
        holder.apply {
            textView.text = "${item.type} : ${item.amount}"
            imageView.setImageDrawable(ContextCompat.getDrawable(context, item.iconId))
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class UserprefsHolder(view: View): RecyclerView.ViewHolder(view) {
    val imageView: ImageView = view.findViewById<ImageView>(android.R.id.icon)
    val textView: MaterialTextView = view.findViewById<MaterialTextView>(android.R.id.text1)
}

class UserprefsDiff(val old: MutableList<UserprefsModel>, val new: MutableList<UserprefsModel>): DiffUtil.Callback() {

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
        return old[oldItemPosition].amount == new[newItemPosition].amount
    }
}