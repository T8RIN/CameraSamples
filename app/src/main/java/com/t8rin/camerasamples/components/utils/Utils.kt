package com.t8rin.camerasamples.components.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor

internal suspend fun Context.shareBitmap(
    bitmap: Bitmap,
    onComplete: () -> Unit
) {
    val fileUri = withContext(Dispatchers.IO) {
        val cachePath = File(externalCacheDir, "shared_images").apply { mkdirs() }
        val file = File(cachePath, "shared_image.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        FileProvider.getUriForFile(this@shareBitmap, "${packageName}.provider", file)
    }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    onComplete()
    startActivity(Intent.createChooser(shareIntent, "Share Image"))
}

internal fun Uri.toBitmap(context: Context): Bitmap? {
    val inputStream = context.contentResolver.openInputStream(this) ?: return null
    val bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream.close()

    val exif = ExifInterface(context.contentResolver.openInputStream(this) ?: return bitmap)
    val rotation = when (
        exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

    return bitmap.rotateBitmap(rotation)
}

internal fun LifecycleCameraController.capturePhoto(
    context: Context,
    onCaptured: (Bitmap?) -> Unit
) {
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    takePicture(
        mainExecutor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val correctedBitmap: Bitmap = image
                    .toBitmap()
                    .rotateBitmap(image.imageInfo.rotationDegrees)

                onCaptured(correctedBitmap)
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                onCaptured(null)
            }
        }
    )
}

internal fun Bitmap.rotateBitmap(rotationDegrees: Int): Bitmap {
    if (rotationDegrees == 0) return this

    val matrix = Matrix().apply {
        postRotate(-rotationDegrees.toFloat())
        postScale(-1f, -1f)
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}