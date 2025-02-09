package com.t8rin.camerasamples

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.t8rin.camerasamples.components.Camera2ApiContent
import com.t8rin.camerasamples.components.IntentCameraContent
import com.t8rin.camerasamples.components.utils.shareBitmap
import com.t8rin.camerasamples.ui.CameraSamplesTheme
import com.t8rin.camerasamples.viewModel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalFoundationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CameraSamplesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val scope = rememberCoroutineScope()
                    val pager = rememberPagerState { 2 }
                    var capture by remember { mutableStateOf(false) }
                    Column {
                        TopAppBar(
                            title = {
                                Text("Camera Samples")
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            windowInsets = WindowInsets.statusBars.union(
                                WindowInsets.displayCutout
                            ),
                            actions = {
                                AnimatedContent(pager.settledPage == 0) { isApi ->
                                    Icon(
                                        painter = painterResource(
                                            if (isApi) {
                                                R.drawable.baseline_api_24
                                            } else {
                                                R.drawable.baseline_smartphone_24
                                            }
                                        ),
                                        contentDescription = null
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                        )

                        HorizontalPager(
                            state = pager,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            beyondViewportPageCount = 2
                        ) {
                            Box(
                                modifier = Modifier.clipToBounds()
                            ) {
                                when (it) {
                                    0 -> {
                                        Camera2ApiContent(
                                            capture = capture && pager.currentPage == 0,
                                            onCaptured = {
                                                viewModel.onBitmapObtained(it)
                                                capture = false
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    1 -> {
                                        IntentCameraContent(
                                            capture = capture && pager.currentPage == 1,
                                            onCaptured = {
                                                viewModel.onBitmapObtained(it)
                                                capture = false
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .windowInsetsPadding(
                                    WindowInsets.navigationBars.union(
                                        WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                                    )
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        pager.animateScrollToPage(
                                            (pager.currentPage - 1).absoluteValue % 2
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            FloatingActionButton(
                                onClick = {
                                    capture = true
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_camera_24),
                                    contentDescription = null
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        pager.animateScrollToPage(
                                            (pager.currentPage + 1) % 2
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    viewModel.bitmap?.let { bitmap ->
                        AlertDialog(
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_camera_24),
                                    contentDescription = null
                                )
                            },
                            title = {
                                Text(
                                    text = "Captured Image"
                                )
                            },
                            confirmButton = {
                                var isLoading by remember {
                                    mutableStateOf(false)
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        isLoading = true
                                        scope.launch {
                                            shareBitmap(
                                                bitmap = bitmap,
                                                onComplete = {
                                                    isLoading = false
                                                }
                                            )
                                        }
                                    }
                                ) {
                                    AnimatedContent(isLoading) {
                                        if (it) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 3.dp,
                                                color = LocalContentColor.current
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.Share,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                                Button(
                                    onClick = {
                                        viewModel.onBitmapObtained(null)
                                    }
                                ) {
                                    Text("Close")
                                }
                            },
                            onDismissRequest = {
                                viewModel.onBitmapObtained(null)
                            },
                            text = {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}