package dev.niri.launcher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import dev.niri.launcher.model.AppInfo
import dev.niri.launcher.ui.theme.*

@Composable
fun Dock(
    favoriteApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onDrawerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Frosted glass background
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(NoctSurface.copy(alpha = 0.85f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Favorite apps
            favoriteApps.take(7).forEach { app ->
                DockAppIcon(app = app, onClick = { onAppClick(app) })
            }

            // Separator
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(NoctBorder)
            )

            // App drawer button
            DockDrawerButton(onClick = onDrawerClick)
        }
    }
}

@Composable
private fun DockAppIcon(
    app: AppInfo,
    onClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "dock_icon_scale",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NoctSurfaceHi),
            contentAlignment = Alignment.Center,
        ) {
            app.icon?.let { drawable ->
                androidx.compose.foundation.Image(
                    bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(36.dp),
                )
            } ?: Text(
                text = app.label.take(1),
                color = NoctPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = app.label,
            color = NoctTextDim,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun DockDrawerButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NoctSurfaceHov)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Six-dot grid icon (app drawer)
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Dot(); Dot(); Dot()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Dot(); Dot(); Dot()
            }
        }
    }
}

@Composable
private fun Dot() {
    Box(
        modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(NoctTextDim)
    )
}
