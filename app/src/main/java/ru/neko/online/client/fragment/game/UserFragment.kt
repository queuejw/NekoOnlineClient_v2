package ru.neko.online.client.fragment.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neko.online.client.R
import ru.neko.online.client.components.AccountPrefs
import ru.neko.online.client.components.models.UserprefsModel

class UserFragment : Fragment(R.layout.user_fragment) {

    private var recyclerView: RecyclerView? = null
    private var mAdapter: UserprefsAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.userprefs_recyclerview)
        context?.let {
            val data = createData(it)
            mAdapter = UserprefsAdapter(data, it)
            val lm = LinearLayoutManager(it)
            recyclerView?.apply {
                adapter = mAdapter
                layoutManager = lm
            }
        }
    }

    private fun createData(context: Context): MutableList<UserprefsModel> {
        val data = ArrayList<UserprefsModel>()
        var accountPrefs: AccountPrefs? = AccountPrefs(context)
        data.add(UserprefsModel("NCoins", accountPrefs!!.userDataNCoins, R.drawable.ic_error))
        data.add(UserprefsModel("Еда", accountPrefs.userDataFood, R.drawable.ic_foodbowl_filled))
        data.add(UserprefsModel("Вода", accountPrefs.userDataWater, R.drawable.ic_water_filled))
        data.add(UserprefsModel("Игрушки", accountPrefs.userDataToys, R.drawable.ic_toy_mouse))
        return data
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
        data = newData
        notifyDataSetChanged()
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