package dev.niri.launcher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.util.lerp

/**
 * niri-style scrollable column layout.
 * onAppClick now receives (AppInfo, columnIndex) so the caller can launch in the correct column bounds.
 */
@Composable
fun ScrollableColumns(
    columns: List<AppColumn>,
    focusedColumnIndex: Int,
    totalColumns: Int,
    onColumnFocused: (Int) -> Unit,
    onAppClick: (AppInfo, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val config = LocalConfiguration.current
    val columnWidth = (config.screenWidthDp.dp * 0.38f)
    val gap = 12.dp
    val horizontalPadding = 16.dp

    // Focus animation
    val focusProgress = animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "focus",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding),
        ) {
            columns.forEachIndexed { index, column ->
                val isFocused = index == focusedColumnIndex
                val fp by animateFloatAsState(
                    targetValue = if (isFocused) 1f else 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "col_focus_$index",
                )

                Column(
                    modifier = Modifier
                        .width(columnWidth)
                        .fillMaxHeight()
                        .padding(horizontal = gap / 2)
                        .graphicsLayer {
                            val s = lerp(0.95f, 1.0f, fp)
                            scaleX = s; scaleY = s
                            alpha = lerp(0.75f, 1.0f, fp)
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(NoctSurface.copy(alpha = lerp(0.4f, 0.7f, fp)))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onColumnFocused(index) }
                        .drawBehind {
                            if (fp > 0.01f) {
                                val stroke = 2.dp.toPx()
                                drawRoundRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            GradientStart.copy(alpha = fp * 0.8f),
                                            GradientEnd.copy(alpha = fp * 0.6f),
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, size.height),
                                    ),
                                    cornerRadius = CornerRadius(16.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(stroke),
                                )
                            }
                        },
                ) {
                    // Column header
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = "${index + 1}",
                            color = if (isFocused) NoctPrimary else NoctTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // App tiles — vertical scroll within column
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 60.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        itemsIndexed(column.apps) { _, app ->
                            AppTile(
                                app = app,
                                isColumnFocused = isFocused,
                                onClick = { onAppClick(app, index) },
                            )
                        }
                        if (isFocused) {
                            item { AddTileButton() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTile(app: AppInfo, isColumnFocused: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(10.dp))
            .background(if (isColumnFocused) NoctSurfaceHi else NoctSurface.copy(alpha = 0.5f))
            .clickable(onClick = onClick).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(NoctSurfaceHov),
            contentAlignment = Alignment.Center,
        ) {
            app.icon?.let { drawable ->
                Image(bitmap = drawable.toBitmap(40, 40).asImageBitmap(), contentDescription = app.label, modifier = Modifier.size(32.dp))
            } ?: Text(text = app.label.take(1), color = NoctPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(app.label, color = NoctText, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(app.packageName, color = NoctTextMuted, fontSize = 10.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AddTileButton() {
    Box(
        modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(10.dp))
            .background(NoctSurface.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.Add, "添加", tint = NoctTextMuted, modifier = Modifier.size(20.dp))
    }
}
