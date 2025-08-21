package com.example.kotlinai.screens.main.tabs.scan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResultsScreen(
    resultText: String,
    bitmap: android.graphics.Bitmap?,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // header with back arrow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Results", modifier = Modifier.padding(start = 8.dp))
        }
        // helper to open a URL robustly from any Context
        fun openUrl(ctx: android.content.Context, raw: String) {
            var url = raw.trim()
            if (!android.util.Patterns.WEB_URL.matcher(url).matches() && !url.contains("://")) {
                url = "http://$url"
            }
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(url)
            }

            try {
                // try start directly
                ctx.startActivity(intent)
            } catch (e: Exception) {
                // fallback: add NEW_TASK and try again
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    ctx.startActivity(intent)
                } catch (_: Exception) {
                    // ignore - nothing more we can do locally
                }
            }
        }

        bitmap?.let { bmp ->
            // compute current date/time once when the results screen is shown
            val now = System.currentTimeMillis()
            // human-friendly datetime, e.g. "Aug 21, 2025, 2:05:12 PM"
            val dateTimeText = SimpleDateFormat("MMM d, yyyy, h:mm:ss a", Locale.getDefault()).format(Date(now))

            // rounded gray container that holds the image, date/time and the QR text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        val imageShape = RoundedCornerShape(6.dp)

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .aspectRatio(1f)
                                .clip(imageShape)
                                .border(1.dp, Color.Black, imageShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "QR Screenshot",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // show human-friendly date/time on a single line
                        Text(text = dateTimeText)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // QR decoded text inside the same container (clickable link)
                        Text(
                            text = resultText,
                            modifier = Modifier.clickable {
                                openUrl(ctx, resultText)
                            },
                            style = TextStyle(
                                color = UiColor.Blue,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // actions: Copy + Share on one row, Open Website on its own row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = {
                        val clipboard = ctx.getSystemService(android.content.ClipboardManager::class.java)
                        val clip = android.content.ClipData.newPlainText("QR", resultText)
                        clipboard?.setPrimaryClip(clip)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("📋")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val shareIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, resultText)
                            type = "text/plain"
                        }
                        ctx.startActivity(android.content.Intent.createChooser(shareIntent, "Share QR result"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = "Share")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = { openUrl(ctx, resultText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("🔗")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Website")
                }
        }

    // Close is handled by the header back button
    }
}
