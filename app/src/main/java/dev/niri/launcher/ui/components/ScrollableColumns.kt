package dev.niri.launcher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import dev.niri.launcher.model.AppColumn
import dev.niri.launcher.model.AppInfo
import dev.niri.launcher.ui.theme.*

/**
 * The heart of niri on Android: a horizontally scrollable strip of columns.
 * Each column is a vertical list of app tiles.
 * Focused column gets the gradient focus ring.
 */
@Composable
fun ScrollableColumns(
    columns: List<AppColumn>,
    focusedColumnIndex: Int,
    onColumnFocused: (Int) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val columnWidth = (screenWidth * 0.38f) // Each column ~38% of screen width
    val gap = 12.dp
    val horizontalPadding = 16.dp

    // Scroll offset animation
    val scrollOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Track which column is focused based on scroll position
    LaunchedEffect(columns.size) {
        if (columns.isNotEmpty() && focusedColumnIndex >= columns.size) {
            onColumnFocused(columns.lastIndex.coerceAtLeast(0))
        }
    }

    // Calculate target scroll to center the focused column
    val targetScroll by remember(focusedColumnIndex, columns.size) {
        derivedStateOf {
            val colWidthPx = columnWidth.value + gap.value
            val screenCenter = config.screenWidthDp / 2f
            val colCenter = focusedColumnIndex * colWidthPx + (colWidthPx / 2f)
            (colCenter - screenCenter).coerceAtLeast(0f)
        }
    }

    LaunchedEffect(targetScroll) {
        scrollOffset.animateTo(
            targetValue = targetScroll,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Horizontal scrollable area
        Row(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(columns.size) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        // Manual horizontal scroll with drag
                        // (In production, use fling too)
                    }
                }
                .padding(horizontal = horizontalPadding),
        ) {
            columns.forEachIndexed { index, column ->
                val isFocused = index == focusedColumnIndex
                val focusProgress by animateFloatAsState(
                    targetValue = if (isFocused) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "focus_progress",
                )

                Column(
                    modifier = Modifier
                        .width(columnWidth)
                        .fillMaxHeight()
                        .padding(horizontal = gap / 2)
                        .graphicsLayer {
                            // Focus scale effect
                            val s = lerp(0.95f, 1.0f, focusProgress)
                            scaleX = s
                            scaleY = s
                            alpha = lerp(0.75f, 1.0f, focusProgress)
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(NoctSurface.copy(alpha = lerp(0.4f, 0.7f, focusProgress)))
                        .clickable { onColumnFocused(index) }
                        .drawBehind {
                            // Focus ring — gradient border
                            if (focusProgress > 0.01f) {
                                val stroke = 2.dp.toPx()
                                val corner = 16.dp.toPx()
                                drawRoundRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            GradientStart.copy(alpha = focusProgress * 0.8f),
                                            GradientEnd.copy(alpha = focusProgress * 0.6f),
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, size.height),
                                    ),
                                    cornerRadius = CornerRadius(corner),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(stroke),
                                )
                            }
                        },
                ) {
                    // Column header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = if (isFocused) NoctPrimary else NoctTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // App tiles in this column
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 8.dp, end = 8.dp,
                            top = 4.dp, bottom = 60.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        itemsIndexed(column.apps) { _, app ->
                            AppTile(
                                app = app,
                                isColumnFocused = isFocused,
                                onClick = { onAppClick(app) },
                            )
                        }

                        // "+" button at the end of focused column
                        if (isFocused) {
                            item {
                                AddTileButton()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTile(
    app: AppInfo,
    isColumnFocused: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isColumnFocused) NoctSurfaceHi else NoctSurface.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // App icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NoctSurfaceHov),
            contentAlignment = Alignment.Center,
        ) {
            app.icon?.let { drawable ->
                Image(
                    bitmap = drawable.toBitmap(40, 40).asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(32.dp),
                )
            } ?: Text(
                text = app.label.take(1),
                color = NoctPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // App info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                color = NoctText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                color = NoctTextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AddTileButton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(NoctSurface.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "添加",
            tint = NoctTextMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}
