package dev.niri.launcher.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.niri.launcher.model.NotificationEntry
import dev.niri.launcher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationCenter(
    notifications: List<NotificationEntry>,
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
                Text("通知", color = NoctText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                if (notifications.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text("没有通知", color = NoctTextMuted, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(notifications, key = { "${it.packageName}-${it.timestamp}" }) { entry ->
                            NotificationCard(entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(entry: NotificationEntry) {
    val timeStr = remember(entry.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
    }

    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        .background(NoctSurfaceHi).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(entry.appName, color = NoctPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(timeStr, color = NoctTextMuted, fontSize = 10.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(entry.title, color = NoctText, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            if (entry.body.isNotEmpty()) {
                Text(entry.body, color = NoctTextDim, fontSize = 12.sp, maxLines = 2)
            }
        }
    }
}
