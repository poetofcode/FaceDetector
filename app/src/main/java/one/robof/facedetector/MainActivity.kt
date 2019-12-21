package one.robof.facedetector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import java.io.File
import java.io.IOException


private const val Tag = "FaceDetector"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chooseImageButton.setOnClickListener {
            Utils.openGallery(this)
        }

        resultText.text = ""
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == Utils.RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK
                && null != data
            ) {
                val selectedImage = data.data
                val realPath = Utils.getRealPathFromURI(selectedImage, this)

                Log.d(Tag, "Real Path: $realPath")

                if (Utils.getFileExtension(realPath) == "gif") {
                    Log.e(Tag, "GIF is not support")
                    return
                }

                val imgFile = File(realPath)
                if (imgFile.exists()) {
                    val myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath())
                    imageView.setImageBitmap(myBitmap)

                    detectFace(Uri.fromFile(imgFile), this)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun detectFace(uri: Uri, ctx: Activity) {
        resultText.text = "Обработка..."

        val image: FirebaseVisionImage
        try {
            image = FirebaseVisionImage.fromFilePath(ctx, uri)

            val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()

            val detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options)

            detector.detectInImage(image)
                .addOnSuccessListener { faces ->
                    var res = "Найдено лиц: ${faces.size}\n"

                    // Task completed successfully
                    // ...
                    Log.d(Tag, "DETECTION SUCCESS")
                    Log.d(Tag, "Face count: ${faces.size}")

                    for ((idx, face) in faces.withIndex()) {
                        res += "${idx + 1}:\n"

                        val bounds = face.boundingBox
                        val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                        val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

                        // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                        // nose available):
                        val leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
                        leftEar?.let {
                            val leftEarPos = leftEar.position
                        }

                        // If contour detection was enabled:
                        val leftEyeContour = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).points
                        val upperLipBottomContour = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).points

                        // If classification was enabled:
                        if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            val smileProb = face.smilingProbability
                            Log.d(Tag, "smile prob: $smileProb")
                            if (smileProb >= 0.5) {
                                res += "- улыбается\n"
                            } else {
                                res += "- серьёзное\n"
                            }
                        }

                        if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            val leftEyeOpenProb = face.leftEyeOpenProbability
                            Log.d(Tag, "left open prob: $leftEyeOpenProb")
                            if (leftEyeOpenProb >= 0.5) {
                                res += "- левый глаз открыт\n"
                            } else {
                                res += "- левый глаз закрыт\n"
                            }
                        }

                        if (face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            val rightEyeOpenProb = face.rightEyeOpenProbability
                            Log.d(Tag, "right open prob: $rightEyeOpenProb")
                            if (rightEyeOpenProb >= 0.5) {
                                res += "- правый глаз открыт\n"
                            } else {
                                res += "- правый глаз закрыт\n"
                            }
                        }

                        // If face tracking was enabled:
                        if (face.trackingId != FirebaseVisionFace.INVALID_ID) {
                            val id = face.trackingId
                        }
                    }

                    resultText.text = res
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                    e.printStackTrace()
                    resultText.text = "Не распознано: $e"
                }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
