package com.t8rin.camerasamples.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.t8rin.camerasamples.components.utils.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun IntentCameraContent(
    capture: Boolean,
    onCaptured: (Bitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val intentCamera = rememberIntentCamera(
        onFailure = {
            onCaptured(null)
        },
        onSuccess = { uri ->
            scope.launch {
                withContext(Dispatchers.IO) {
                    onCaptured(uri.toBitmap(context))
                }
            }
        }
    )

    LaunchedEffect(capture) {
        if (capture) {
            intentCamera.capturePhoto()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text("Click capture to open system camera")
    }
}

