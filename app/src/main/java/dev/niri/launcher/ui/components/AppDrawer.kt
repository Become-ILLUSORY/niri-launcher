package dev.niri.launcher.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import dev.niri.launcher.model.AppInfo
import dev.niri.launcher.ui.theme.*

@Composable
fun AppDrawer(
    apps: List<AppInfo>,
    query: String,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filteredApps = remember(apps, query) {
        if (query.isBlank()) apps
        else apps.filter {
            it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
        }
    }

    // Slide up animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = spring(stiffness = Spring.StiffnessHigh),
        ) + fadeOut(),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(NoctBg.copy(alpha = 0.95f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
            ) {
                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NoctSurface)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = NoctTextDim,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        textStyle = TextStyle(
                            color = NoctText,
                            fontSize = 15.sp,
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(NoctPrimary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box {
                                if (query.isEmpty()) {
                                    Text(
                                        text = "搜索应用…",
                                        color = NoctTextMuted,
                                        fontSize = 15.sp,
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // App count
                Text(
                    text = "${filteredApps.size} 个应用",
                    color = NoctTextDim,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                )

                Spacer(Modifier.height(8.dp))

                // App grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(72.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        DrawerAppItem(app = app, onClick = { onAppClick(app) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerAppItem(
    app: AppInfo,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(NoctSurface),
            contentAlignment = Alignment.Center,
        ) {
            app.icon?.let { drawable ->
                Image(
                    bitmap = drawable.toBitmap(56, 56).asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(44.dp),
                )
            } ?: Text(
                text = app.label.take(1),
                color = NoctPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = app.label,
            color = NoctText,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 72.dp),
        )
    }
}
