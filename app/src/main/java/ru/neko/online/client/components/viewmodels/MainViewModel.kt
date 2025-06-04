package ru.neko.online.client.components.viewmodels

import android.graphics.Bitmap
import androidx.collection.SparseArrayCompat
import androidx.collection.contains
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.neko.online.client.components.models.CatModel
import ru.neko.online.client.components.models.UserprefsModel
import kotlin.lazy

class MainViewModel: ViewModel() {

    val userprefsLiveData: MutableLiveData<MutableList<UserprefsModel>> by lazy {
        MutableLiveData<MutableList<UserprefsModel>>()
    }
    val catsLiveData: MutableLiveData<MutableList<CatModel>> by lazy {
        MutableLiveData<MutableList<CatModel>>()
    }
    private val catIconCache: SparseArrayCompat<Bitmap?> by lazy {
        SparseArrayCompat<Bitmap?>()
    }

    fun setUserprefsLiveData(newData: MutableList<UserprefsModel>) {
        userprefsLiveData.value = newData
    }
    fun setCatsLiveData(newData: MutableList<CatModel>) {
        catsLiveData.value = newData
    }

    fun addIconToCache(id: Int, bitmap: Bitmap?) {
        if(!catIconCache.containsKey(id)) {
            catIconCache.append(id, bitmap)
        }
    }

    fun removeIconFromCache(id: Int) {
        catIconCache.remove(id)
    }

    fun getIconFromCache(id: Int): Bitmap? {
        return catIconCache[id]
    }
}