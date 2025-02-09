/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.camerasamples.components

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import kotlin.random.Random


private class IntentCameraImpl(
    private val context: Context,
    private val takePhoto: ManagedActivityResultLauncher<Uri, Boolean>,
    private val onCreateTakePhotoUri: (Uri) -> Unit,
    private val onFailure: (Throwable) -> Unit,
) : IntentCamera {
    override fun capturePhoto() {
        runCatching {
            val imagesFolder = File(context.externalCacheDir, "shared_images")
            runCatching {
                imagesFolder.mkdirs()
                val file = File(imagesFolder, "${Random.nextLong()}.jpg")
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            }.onSuccess {
                onCreateTakePhotoUri(it)
                takePhoto.launch(it)
            }
        }.onFailure {
            if (it is SecurityException) {
                onFailure(CameraException)
            } else onFailure(it)
        }
    }
}

@Stable
@Immutable
interface IntentCamera {

    fun capturePhoto()

}


@Composable
fun rememberIntentCamera(
    onFailure: () -> Unit = {},
    onSuccess: (Uri) -> Unit,
): IntentCamera {
    val context = LocalContext.current

    var takePhotoUri by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }
    val takePhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = {
            val uri = takePhotoUri
            if (it && uri != null && uri != Uri.EMPTY) {
                onSuccess(uri)
            } else onFailure()
            takePhotoUri = null
        }
    )


    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->

    }

    return remember(takePhoto) {
        derivedStateOf {
            IntentCameraImpl(
                context = context,
                takePhoto = takePhoto,
                onCreateTakePhotoUri = {
                    takePhotoUri = it
                },
                onFailure = {
                    onFailure()

                    when (it) {
                        is CameraException -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )
        }
    }.value
}

private object CameraException : Throwable("No Camera permission") {
    @Suppress("unused")
    private fun readResolve(): Any = CameraException
}