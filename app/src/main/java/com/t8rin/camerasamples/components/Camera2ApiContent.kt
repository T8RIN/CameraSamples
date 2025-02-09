package com.t8rin.camerasamples.components

import android.graphics.Bitmap
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.t8rin.camerasamples.components.utils.capturePhoto

@Composable
internal fun Camera2ApiContent(
    capture: Boolean,
    onCaptured: (Bitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
        }
    }

    LaunchedEffect(capture, cameraController) {
        if (capture) {
            cameraController.capturePhoto(
                context = context,
                onCaptured = onCaptured
            )
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_START
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                controller = cameraController
            }
        },
        onRelease = {
            cameraController.unbind()
        }
    )
}