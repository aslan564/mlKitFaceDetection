package com.mlkit.demo.kotlin.repository

import com.mlkit.demo.kotlin.network.NetworkResult
import com.mlkit.demo.kotlin.network.UserService.visionImageService
import com.mlkit.demo.model.VisionResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONObject

class VisionLabsRepository {
    private val serviceAccount = visionImageService

    suspend fun checkUserLiveOrNot(
        image: MultipartBody.Part,
        onComplete: (NetworkResult<String>) -> Unit
    ) {
        try {
            onComplete(NetworkResult.loading())
            val response = serviceAccount.uploadImage(image)
            if (response.isSuccessful) {
                response.body()?.let {
                    onComplete(NetworkResult.success(it))
                } ?: onComplete(NetworkResult.error("xeta bas verdi "))
            } else {
                catchServerError(response.errorBody()) {
                    onComplete(it)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(NetworkResult.error(e.localizedMessage))
        }
    }
}

fun <T> catchServerError(error: ResponseBody?, onCatchError: (NetworkResult<T>) -> Unit) {
    try {
        val jObjError = error?.let {
            JSONObject(it.string())
        }
        jObjError?.let {
            val messageServer = jObjError.getString("error")
            val errorServer = jObjError.getString("message")
            onCatchError(
                NetworkResult.error(
                    messageServer.ifEmpty { errorServer }
                )
            )
        }
    } catch (e: Exception) {
        onCatchError(NetworkResult.error(e.message ?: "an error occurred "))

    }
}