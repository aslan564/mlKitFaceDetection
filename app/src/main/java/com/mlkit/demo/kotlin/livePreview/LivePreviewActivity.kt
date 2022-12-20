/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mlkit.demo.kotlin.livePreview

import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.annotation.KeepName
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.face.Face
import com.mlkit.demo.CameraSource
import com.mlkit.demo.CameraSourcePreview
import com.mlkit.demo.GraphicOverlay
import com.mlkit.demo.R
import com.mlkit.demo.databinding.ActivityVisionLivePreviewBinding
import com.mlkit.demo.kotlin.facedetector.FaceDetectorProcessor
import com.mlkit.demo.kotlin.facemeshdetector.FaceMeshDetectorProcessor
import com.mlkit.demo.model.UploadRequestBody
import com.mlkit.demo.preference.PreferenceUtils
import com.mlkit.demo.preference.SettingsActivity
import com.mlkit.demo.preference.SettingsActivity.LaunchSource
import okhttp3.MultipartBody
import java.io.*
import java.util.*


/** Live preview demo for ML Kit APIs. */
@KeepName
class LivePreviewActivity :
    AppCompatActivity(), FaceDetectorProcessor.FaceControl, UploadRequestBody.UploadCallback,
    OnItemSelectedListener,
    CompoundButton.OnCheckedChangeListener {

    private val binding: ActivityVisionLivePreviewBinding by lazy {
        ActivityVisionLivePreviewBinding.inflate(
            layoutInflater
        )
    }
    private val mViewModel by viewModels<LivePreviewViewModel>()
    private var takePhotoState = false

    private var rect = Rect()
    private lateinit var ivContainer: ImageView

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var selectedModel = FACE_DETECTION

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(binding.root)
        preview = binding.previewView

        val settingsCanWrite = hasWriteSettingsPermission(this)
        graphicOverlay = findViewById(R.id.graphic_overlay)
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null")
        }

        if (!settingsCanWrite) {
            changeWriteSettingsPermission(this)
        } else {
            changeScreenBrightness(this)
        }

        ivContainer = findViewById(R.id.iv_container)
        ivContainer.getLocalVisibleRect(rect)


        val options: MutableList<String> = ArrayList()
        options.add(FACE_DETECTION)
        options.add(FACE_MESH_DETECTION)

        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter(this, R.layout.spinner_style, options)

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // attaching data adapter to spinner
        binding.spinner.adapter = dataAdapter
        binding.spinner.onItemSelectedListener = this

        binding.facingSwitch.setOnCheckedChangeListener(this)

        binding.settingsButton.setOnClickListener {
            val intent = Intent(applicationContext, SettingsActivity::class.java)
            intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, LaunchSource.LIVE_PREVIEW)
            startActivity(intent)
        }

        createCameraSource(selectedModel)

        mViewModel.controlLoginState.observe(this) {
            binding.rootLayout?.snackBar(it)
        }
    }


    @Synchronized
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent?.getItemAtPosition(pos).toString()
        preview?.stop()
        createCameraSource(selectedModel)
        startCameraSource()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Do nothing.
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Log.d(TAG, "Set facing")
        if (cameraSource != null) {
            cameraSource?.setFacing(CameraSource.CAMERA_FACING_FRONT)
        }
        preview?.stop()
        startCameraSource()
    }

    private fun createCameraSource(model: String) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(this, graphicOverlay)
        }
        try {
            when (model) {

                FACE_DETECTION -> {
                    Log.i(TAG, "Using Face Detector Processor")
                    val faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(this)
                    cameraSource?.setMachineLearningFrameProcessor(
                        FaceDetectorProcessor(this, faceDetectorOptions, this)
                    )
                }
                FACE_MESH_DETECTION -> {
                    cameraSource!!.setMachineLearningFrameProcessor(FaceMeshDetectorProcessor(this));
                }
                else -> Log.e(TAG, "Unknown model: $model")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor: $model", e)
            Toast.makeText(
                applicationContext,
                "Can not create image processor: " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null")
                }
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        createCameraSource(selectedModel)
        startCameraSource()
    }

    /** Stops the camera. */
    override fun onPause() {
        super.onPause()
        preview?.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource?.release()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getFaceParameters(face: Face, image: Bitmap?) {
        val heightDetectedFace = face.boundingBox.height()
        if (heightDetectedFace > 190) {
            binding.tvInformation?.text = "cihazi uzaqlasdirin "
        } else if (heightDetectedFace < 170) {
            binding.tvInformation?.text = "cihazi yaxinlasdirin "
        } else {
            binding.tvInformation?.text = "cihazi sabit saxlayin. "
            if (face.boundingBox.centerY() < 290) {
                binding.tvInformation?.text = "Cihazi yuxari qaldirin  "
            } else if (face.boundingBox.centerY() > 300) {
                binding.tvInformation?.text = "Cihazi asagi salin  "
            } else if (face.boundingBox.centerX() < 190) {
                binding.tvInformation?.text = "Cihazi saga aparin  "
            } else if (face.boundingBox.centerX() > 200) {
                binding.tvInformation?.text = "Cihazi sola aparin  "
            } else {
                binding.tvInformation?.text = "sabit qalin  "
                takePhoto(image)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun takePhoto(image: Bitmap?) {
        if (takePhotoState) {
            return
        } else {
            image?.let {
                val file = convertBitmapToFile(image)
                uploadImage(file)
                //uploadImage(file)
                Log.d("lkqjshdkja", "takePhoto: ${file}")
                Log.d("lkqjshdkja", "allocationByteCount: ${image.allocationByteCount}")
                takePhotoState = true
            }
        }
    }

    private fun uploadImage(photoUri: Uri?) {
        if (photoUri == null) {
            binding.rootLayout?.snackBar("zehmet olmasa bir sekil secin ")
            return
        }
        Log.d(TAG_NEW, "faces detected: 2 ")
        val parcelFileDescriptor = contentResolver.openFileDescriptor(photoUri, "r", null) ?: return
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(photoUri))
        //BURADAN ASAGI
        val fileOutputStream = FileOutputStream(file)
        inputStream.copyTo(fileOutputStream)
        val uploadRequestBody = UploadRequestBody(file, "image", this)
        mViewModel.getUserData(
            MultipartBody.Part.createFormData(
                "image",
                file.name,
                uploadRequestBody
            )
        ) {
            preview?.stop()
        }
    }

    private fun convertBitmapToFile(bitmap: Bitmap): Uri {
        //create a file to write bitmap data
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "image", null)
        return Uri.parse(path)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun changeWriteSettingsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        context.startActivity(intent)
    }

    // Check whether this app has android write settings permission.
    @RequiresApi(Build.VERSION_CODES.M)
    private fun hasWriteSettingsPermission(context: Context): Boolean {
        var ret = true
        // Get the result from below code.
        ret = Settings.System.canWrite(context)
        return ret
    }

    private fun changeScreenBrightness(
        context: Context,
    ) {   // Change the screen brightness change mode to manual.
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        // Apply the screen brightness value to the system, this will change
        // the value in Settings ---> Display ---> Brightness level.
        // It will also change the screen brightness for the device.
        Settings.System.putInt(
            context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 255
        )
    }


    companion object {
        private const val FACE_DETECTION = "Face Detection"
        private const val FACE_MESH_DETECTION = "Face Mesh Detection (Beta)";
        private const val TAG = "CameraXApp"
        private const val TAG_NEW = "CameraXAdsafadfsdfppnew"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onProgressUpdate(percentage: Int) {

    }

}

fun View.snackBar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).also { snackbar ->
        snackbar.setAction("ok") {
            snackbar.dismiss()
        }
    }.show()
}

fun ContentResolver.getFileName(uri: Uri): String {
    var name = ""
    val cursor = query(uri, null, null, null, null)
    cursor?.use {
        it.moveToFirst()
        val index = if (it.getColumnIndex(OpenableColumns.DISPLAY_NAME) >= 0) {
            it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        } else it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
        name = cursor.getString(index)
    }
    return name
}
