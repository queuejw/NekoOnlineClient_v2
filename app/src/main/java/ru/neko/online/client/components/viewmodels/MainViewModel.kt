package ru.neko.online.client.components.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.neko.online.client.components.models.CatModel
import ru.neko.online.client.components.models.UserprefsModel

class MainViewModel: ViewModel() {

    var userprefsLiveData = MutableLiveData<MutableList<UserprefsModel>>()
    var catsLiveData = MutableLiveData<MutableList<CatModel>>()

    fun setUserprefsLiveData(newData: MutableList<UserprefsModel>) {
        userprefsLiveData.value = newData
    }
    fun setCatsLiveData(newData: MutableList<CatModel>) {
        catsLiveData.value = newData
    }
}