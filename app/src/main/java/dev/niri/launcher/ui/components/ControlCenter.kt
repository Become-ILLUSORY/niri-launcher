package dev.niri.launcher.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.niri.launcher.model.QuickTile
import dev.niri.launcher.ui.theme.*

@Composable
fun ControlCenter(
    tiles: List<QuickTile>,
    brightness: Float,
    onTileToggle: (Int) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it / 3 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it / 3 }) + fadeOut(),
    ) {
        Box(modifier = modifier.fillMaxSize()
            .background(NoctBg.copy(alpha = 0.6f))
            .clickable(remember { MutableInteractionSource() }, null) { onDismiss() },
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(modifier = Modifier
                .padding(top = 56.dp, start = 16.dp, end = 16.dp)
                .widthIn(max = 400.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NoctSurface.copy(alpha = 0.95f))
                .clickable(remember { MutableInteractionSource() }, null) {}
                .padding(16.dp)
            ) {
                Text("控制中心", color = NoctText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                // Quick tiles 2-col grid
                tiles.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { tile ->
                            val idx = tiles.indexOf(tile)
                            QuickTileCard(tile, { onTileToggle(idx) }, Modifier.weight(1f))
                        }
                        if (row.size < 2) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(8.dp))

                // Brightness
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(NoctSurfaceHi)
                        .padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Icon(Icons.Outlined.LightMode, null, tint = NoctOrange, modifier = Modifier.size(18.dp))
                    Slider(value = brightness, onValueChange = onBrightnessChange, modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = NoctPrimary, activeTrackColor = NoctPrimary, inactiveTrackColor = NoctBorder))
                    Icon(Icons.Outlined.LightMode, null, tint = NoctPrimary, modifier = Modifier.size(22.dp))
                }

                Spacer(Modifier.height(12.dp))

                // Bottom actions
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BottomAction(Icons.Outlined.Lock, "锁屏", Modifier.weight(1f))
                    BottomAction(Icons.Outlined.Settings, "设置", Modifier.weight(1f))
                    BottomAction(Icons.Outlined.PowerSettingsNew, "关机", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickTileCard(tile: QuickTile, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val active = tile.isActive
    Row(modifier = modifier.height(44.dp).clip(RoundedCornerShape(12.dp))
        .background(if (active) NoctPrimary.copy(alpha = 0.15f) else NoctSurfaceHov)
        .clickable(onClick = onClick).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (active) NoctPrimary else NoctTextMuted))
        Text(tile.label, color = if (active) NoctPrimary else NoctText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BottomAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(NoctSurfaceHi)
        .clickable {}.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = NoctTextDim, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = NoctTextDim, fontSize = 11.sp)
    }
}
