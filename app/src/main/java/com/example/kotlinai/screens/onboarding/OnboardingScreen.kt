package com.example.kotlinai.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlin.math.abs
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

private data class Slide(val imageRes: Int, val title: String, val subtitle: String)

private const val SNAP_FRACTION = 0.35f

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    val slides = listOf(
        Slide(
            imageRes = com.example.kotlinai.R.drawable.on_boarding_1,
            title = stringResource(id = com.example.kotlinai.R.string.onboard_title_1),
            subtitle = stringResource(id = com.example.kotlinai.R.string.onboard_subtitle_1)
        ),
        Slide(
            imageRes = com.example.kotlinai.R.drawable.on_boarding_2,
            title = stringResource(id = com.example.kotlinai.R.string.onboard_title_2),
            subtitle = stringResource(id = com.example.kotlinai.R.string.onboard_subtitle_2)
        ),
        Slide(
            imageRes = com.example.kotlinai.R.drawable.on_boarding_3,
            title = stringResource(id = com.example.kotlinai.R.string.onboard_title_3),
            subtitle = stringResource(id = com.example.kotlinai.R.string.onboard_subtitle_3)
        )
    )

    val listState = rememberLazyListState()
    // Snap earlier based on scroll offset fraction of viewport (snap when scrolled past SNAP_FRACTION)
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
            if (!scrolling) {
                // Guard: layout may not be ready yet (viewport width == 0) or there may be no visible items.
                val layoutInfo = listState.layoutInfo
                val viewportWidth = layoutInfo.viewportSize.width
                if (viewportWidth == 0 || layoutInfo.visibleItemsInfo.isEmpty()) {
                    // skip this pass until layout is ready
                    return@collect
                }

                val currentIndex = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset

                val threshold = (viewportWidth * SNAP_FRACTION).toInt()
                val tentative = if (offset > threshold) currentIndex + 1 else currentIndex
                val target = tentative.coerceIn(0, slides.lastIndex)

                // Snap if target differs OR there's a partial offset (to settle position)
                if (target != currentIndex || offset != 0) {
                    // Try animated snap first
                    listState.animateScrollToItem(target)

                    // If animation didn't settle (rare), force exact position
                    val afterIndex = listState.firstVisibleItemIndex
                    val afterOffset = listState.firstVisibleItemScrollOffset
                    if (afterIndex != target || afterOffset != 0) {
                        listState.scrollToItem(target)
                    }
                }
            }
        }
    }

    // coroutineScope removed — button now always calls onContinue()
    val context = LocalContext.current

    // Permission launcher that will call onContinue() when camera permission is granted
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            onContinue()
        }
        // if denied, stay on onboarding (could show rationale/snackbar)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Make the carousel occupy the available space above the bottom controls
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val pageWidth = this.maxWidth
                val pageHeight = this.maxHeight

                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(slides) { slide ->
                        Column(
                            modifier = Modifier
                                .width(pageWidth)
                                .fillMaxHeight()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            // Fixed top spacer so image sits at same vertical position across slides
                            Spacer(modifier = Modifier.height(pageHeight * 0.12f))

                            // Smaller, fixed-height image so it doesn't vary between slides
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = slide.imageRes),
                                contentDescription = slide.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(pageHeight * 0.38f),
                                contentScale = ContentScale.Fit
                            )

                            // much larger gap between image and title (tripled)
                            Spacer(modifier = Modifier.height(96.dp))

                            Text(
                                text = slide.title,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = slide.subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.width(pageWidth * 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Indicators
            val current = listState.firstVisibleItemIndex.coerceIn(0, slides.lastIndex)
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                slides.forEachIndexed { index, _ ->
                    val color = if (index == current) MaterialTheme.colorScheme.primary else Color.Gray
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isLast = current == slides.lastIndex
            val buttonTextRes = if (isLast) com.example.kotlinai.R.string.onboard_allow_camera else com.example.kotlinai.R.string.onboard_next

            Button(
                onClick = {
                    val hasCamera = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasCamera) {
                        onContinue()
                    } else {
                        cameraLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(id = buttonTextRes),
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
    
}
