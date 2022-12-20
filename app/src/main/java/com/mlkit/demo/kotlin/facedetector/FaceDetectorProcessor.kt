package com.mlkit.demo.kotlin.facedetector

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.mlkit.demo.GraphicOverlay
import com.mlkit.demo.kotlin.VisionProcessorBase
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.util.Locale

/** Face Detector Demo.  */
private const val TAG = "FaceDetectorProcessor"

class FaceDetectorProcessor(
    context: Context,
    detectorOptions: FaceDetectorOptions?,
    private val faceControl: FaceControl

) :
    VisionProcessorBase<List<Face>>(context) {

    interface FaceControl {
        fun getFaceParameters(face: Face, image: Bitmap?)
    }


    private val detector: FaceDetector

    init {
        val options = detectorOptions
            ?: FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking()
                .build()

        detector = FaceDetection.getClient(options)

        Log.v(MANUAL_TESTING_LOG, "Face detector options: $options")
    }

    override fun stop() {
        super.stop()
        detector.close()
    }

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun onSuccess(results: List<Face>, graphicOverlay: GraphicOverlay, image: Bitmap?) {
        for (face in results) {
            graphicOverlay.add(FaceGraphic(graphicOverlay, face))
            faceControl.getFaceParameters(face, image)
            logExtrasForTesting(face)
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG_FACE, "Face detection failed $e")
    }

    companion object {
        private const val TAG_FACE = "FaceDetectorProcessor"
        private fun logExtrasForTesting(face: Face?) {
            if (face != null) {

                // All landmarks
                val landMarkTypes = intArrayOf(
                    FaceLandmark.MOUTH_BOTTOM,
                    FaceLandmark.MOUTH_RIGHT,
                    FaceLandmark.MOUTH_LEFT,
                    FaceLandmark.RIGHT_EYE,
                    FaceLandmark.LEFT_EYE,
                    FaceLandmark.RIGHT_EAR,
                    FaceLandmark.LEFT_EAR,
                    FaceLandmark.RIGHT_CHEEK,
                    FaceLandmark.LEFT_CHEEK,
                    FaceLandmark.NOSE_BASE
                )
                val landMarkTypesStrings = arrayOf(
                    "MOUTH_BOTTOM",
                    "MOUTH_RIGHT",
                    "MOUTH_LEFT",
                    "RIGHT_EYE",
                    "LEFT_EYE",
                    "RIGHT_EAR",
                    "LEFT_EAR",
                    "RIGHT_CHEEK",
                    "LEFT_CHEEK",
                    "NOSE_BASE"
                )
                for (i in landMarkTypes.indices) {
                    val landmark = face.getLandmark(landMarkTypes[i])
                    val contour = face.getContour(landMarkTypes[i])
                    val widthFace = face.boundingBox.width()
                    val heightFace = face.boundingBox.height()
                    Log.d("aslanfacedehsy", "logExtrasForTesting: $widthFace $heightFace")
                    if (contour != null) {
                        Log.d(
                            "aslanfacedehsy",
                            "logExtrasForTesting: ${contour.points} $heightFace"
                        )
                        //PointF(132.0, 215.0),
                    }

                    if (landmark == null) {
                        Log.v(
                            MANUAL_TESTING_LOG,
                            "No landmark of type: " + landMarkTypesStrings[i] + " has been detected"
                        )
                    } else {
                        val landmarkPosition = landmark.position
                        val landmarkPositionStr =
                            String.format(
                                Locale.US,
                                "x: %f , y: %f",
                                landmarkPosition.x,
                                landmarkPosition.y
                            )
                        Log.v(
                            MANUAL_TESTING_LOG,
                            "Position for face landmark: " +
                                    landMarkTypesStrings[i] +
                                    " is :" +
                                    landmarkPositionStr
                        )
                    }
                }
                Log.v(
                    MANUAL_TESTING_LOG,
                    "face left eye open probability: " + face.leftEyeOpenProbability
                )
                Log.v(
                    MANUAL_TESTING_LOG,
                    "face right eye open probability: " + face.rightEyeOpenProbability
                )
                Log.v(
                    MANUAL_TESTING_LOG,
                    "face smiling probability: " + face.smilingProbability
                )
                Log.v(
                    MANUAL_TESTING_LOG,
                    "face tracking id: " + face.trackingId
                )

                /*if (face.boundingBox.centerX()) {
                }*/

                Log.v(
                    MANUAL_TESTING_LOG_TWO,
                    "3 face boundingBox centerX: " + face.boundingBox.centerX()
                )

                Log.v(
                    MANUAL_TESTING_LOG_TWO,
                    "4 face boundingBox centerY: " + face.boundingBox.centerY()
                )
            }
        }
    }
}
