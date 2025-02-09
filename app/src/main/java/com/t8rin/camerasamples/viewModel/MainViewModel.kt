package com.t8rin.camerasamples.viewModel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private var cachedBitmap: Bitmap? by mutableStateOf<Bitmap?>(null)

    fun onBitmapObtained(bitmap: Bitmap?) {
        cachedBitmap = bitmap
    }

    val bitmap: Bitmap?
        get() = cachedBitmap
}