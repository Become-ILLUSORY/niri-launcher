package dev.niri.launcher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.niri.launcher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TopBar(
    workspaceCount: Int,
    currentWorkspace: Int,
    onNotificationClick: () -> Unit,
    onControlCenterClick: () -> Unit,
    onOverviewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var time by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
            date = SimpleDateFormat("M/d E", Locale.getDefault()).format(now.time)
            kotlinx.coroutines.delay(1000L)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left: workspace dots + time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Workspace indicator pills
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(workspaceCount) { i ->
                    Box(
                        modifier = Modifier
                            .width(if (i == currentWorkspace) 16.dp else 6.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (i == currentWorkspace) NoctPrimary else NoctTextMuted
                            )
                    )
                }
            }

            Text(
                text = time,
                color = NoctText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp),
            )
            Text(
                text = date,
                color = NoctTextDim,
                fontSize = 12.sp,
            )
        }

        // Right: status icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BarIcon(Icons.Outlined.Wifi, "Wi-Fi", true)
            BarIcon(Icons.Outlined.Bluetooth, "BT", false)
            BarIcon(Icons.Outlined.Battery5Bar, "80%", true)

            Spacer(Modifier.width(4.dp))

            // Notification bell with dot
            Box {
                BarIcon(
                    Icons.Outlined.NotificationsNone,
                    "通知",
                    false,
                    onClick = onNotificationClick,
                )
                // Unread dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(NoctRed)
                )
            }

            BarIcon(
                Icons.Outlined.Tune,
                "控制",
                false,
                onClick = onControlCenterClick,
            )

            BarIcon(
                Icons.Outlined.GridView,
                "概览",
                false,
                onClick = onOverviewClick,
            )
        }
    }
}

@Composable
private fun BarIcon(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) NoctPrimary else NoctTextDim,
            modifier = Modifier.size(18.dp),
        )
    }
}
