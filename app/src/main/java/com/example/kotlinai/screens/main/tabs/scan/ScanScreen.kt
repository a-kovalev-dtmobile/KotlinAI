package com.example.kotlinai.screens.main.tabs.scan

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage

@Composable
fun ScanScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var scannedText by remember { mutableStateOf<String?>(null) }
    var scannedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var scanningEnabled by remember { mutableStateOf(true) }

    // hold the camera so UI can control zoom
    var camera by remember { mutableStateOf<Camera?>(null) }
    // linear zoom in range [0f..1f]
    var zoom by remember { mutableStateOf(0f) }

    val launcher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        hasPermission = granted
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()

                            // Preview use case
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            // ImageAnalysis for ML Kit
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            // Configure ML Kit barcode scanner (QR codes included)
                            val options = BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                .build()
                            val scanner = BarcodeScanning.getClient(options)

                            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                if (scanningEnabled) {
                                    processImageProxy(scanner, imageProxy, previewView) { value, bitmap ->
                                        // disable further scanning while results screen is open
                                        scanningEnabled = false
                                        // update Compose state on detection
                                        scannedText = value
                                        scannedBitmap = bitmap
                                    }
                                } else {
                                    // not scanning, just close the frame
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            cameraProvider.unbindAll()
                            val cam = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                            // expose camera to Compose for UI controls
                            camera = cam
                            // try to initialize zoom value by observing zoomState
                            try {
                                cam.cameraInfo.zoomState.observe(lifecycleOwner) { state ->
                                    zoom = state.linearZoom
                                }
                            } catch (t: Throwable) {
                                // ignore observation failures
                            }
                        } catch (e: Exception) {
                            Log.e("ScanScreen", "Failed to bind camera use cases", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Results overlay when a QR is detected (delegated)
            scannedText?.let { resultText ->
                ResultsScreen(resultText = resultText, bitmap = scannedBitmap) {
                    // close results and re-enable scanning
                    scannedText = null
                    scannedBitmap = null
                    scanningEnabled = true
                }
            }

            // Zoom slider overlay at bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (camera != null) {
                    Slider(
                        value = zoom,
                        onValueChange = { value ->
                            zoom = value
                            try {
                                camera?.cameraControl?.setLinearZoom(value)
                            } catch (t: Throwable) {
                                // ignore
                            }
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )
                    Text(text = "Zoom: ${"%.0f".format(zoom * 100)}%")
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission required")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant permission")
                }
            }
        }
    }
}

private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    previewView: PreviewView,
    onBarcodeDetected: (String, android.graphics.Bitmap?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrEmpty()) {
                        // try to capture a bitmap from the PreviewView (may be null)
                        val bitmap: android.graphics.Bitmap? = try {
                            previewView.bitmap
                        } catch (t: Throwable) {
                            null
                        }

                        onBarcodeDetected(rawValue, bitmap)
                        break
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ScanScreen", "Barcode processing failure", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
