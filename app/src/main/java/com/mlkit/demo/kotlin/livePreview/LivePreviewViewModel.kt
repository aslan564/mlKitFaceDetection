package com.mlkit.demo.kotlin.livePreview

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlkit.demo.kotlin.network.Status
import com.mlkit.demo.kotlin.repository.VisionLabsRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

private const val TAG = "LivePreviewViewModel"

class LivePreviewViewModel : ViewModel() {
    private val repository = VisionLabsRepository()
    private var _controlLoginState = MutableLiveData<String>()
    var controlLoginState: LiveData<String> = _controlLoginState

    fun getUserData(body: MultipartBody.Part, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.checkUserLiveOrNot(body) {
                when (it.status) {
                    Status.SUCCESS -> {
                        _controlLoginState.value = "muveffeqiyyetle login oldun "
                        Log.d(TAG, "getUserData: ${it.data}")
                        onComplete()
                    }
                    Status.ERROR -> {
                        _controlLoginState.value = "xeta bas verdi \n ${it.message}"
                        Log.d(TAG, "getUserData: ${it.message}")
                    }
                    Status.LOADING -> {
                        _controlLoginState.value = "yukleme davam edir... "
                        Log.d(TAG, "getUserData: loading")
                    }
                }
            }
        }
    }
}