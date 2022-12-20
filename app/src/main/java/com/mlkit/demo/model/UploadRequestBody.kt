package com.mlkit.demo.model

import android.os.Handler
import android.os.Looper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class UploadRequestBody(
    private val file: File,
    private val contentType: String,
    private val callback: UploadCallback
) : RequestBody() {

    interface UploadCallback {
        fun onProgressUpdate(percentage: Int)
    }

    override fun contentType() = "$contentType/jpeg".toMediaTypeOrNull()
    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val lent = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val fileInputStream = FileInputStream(file)
        var uploaded = 0L
        fileInputStream.use { fileInput ->
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (fileInput.read(buffer).also { read = it } != -1) {
                handler.post(ProgressUploaded(uploaded, lent))
                uploaded += read
                sink.write(buffer, 0, read)
            }
        }
    }

    inner class ProgressUploaded(
        private val uploaded: Long,
        private val total: Long
    ) : Runnable {
        override fun run() {
            callback.onProgressUpdate((100 * uploaded / total).toInt())
        }

    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 1048
    }
}