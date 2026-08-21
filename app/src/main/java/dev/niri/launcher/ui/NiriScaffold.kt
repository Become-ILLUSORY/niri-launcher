package dev.niri.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.niri.launcher.model.AppInfo
import dev.niri.launcher.ui.components.*
import dev.niri.launcher.ui.theme.*
import dev.niri.launcher.viewmodel.LauncherViewModel

@Composable
fun NiriScaffold(vm: LauncherViewModel = viewModel()) {
    val workspaces by vm.workspaces.collectAsState()
    val currentWs by vm.currentWorkspace.collectAsState()
    val focusedCol by vm.focusedColumn.collectAsState()
    val isDrawerOpen by vm.isDrawerOpen.collectAsState()
    val isCCOpen by vm.isControlCenterOpen.collectAsState()
    val isNotifOpen by vm.isNotificationOpen.collectAsState()
    val isOverviewOpen by vm.isOverviewOpen.collectAsState()
    val allApps by vm.allApps.collectAsState()
    val query by vm.queryText.collectAsState()
    val tiles by vm.quickTiles.collectAsState()
    val brightness by vm.brightness.collectAsState()

    val currentColumns = workspaces.getOrNull(currentWs)?.columns ?: emptyList()
    val favApps = allApps.take(7)

    // Overlay visibility (any overlay open = dim the background)
    val anyOverlay = isDrawerOpen || isCCOpen || isNotifOpen || isOverviewOpen

    // ── Vertical drag: workspace switch ──
    val config = LocalConfiguration.current
    val dragThreshold = config.screenHeightDp * 0.15f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NoctBg)
            .pointerInput(currentWs, anyOverlay) {
                if (anyOverlay) return@pointerInput
                var totalDragY = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDragY = 0f },
                    onDragEnd = {
                        if (totalDragY > dragThreshold * 2) vm.prevWorkspace()
                        else if (totalDragY < -dragThreshold * 2) vm.nextWorkspace()
                    },
                    onVerticalDrag = { _, dy -> totalDragY += dy },
                )
            }
    ) {
        // Main content: columns + dock
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            TopBar(
                workspaceCount = workspaces.size,
                currentWorkspace = currentWs,
                onNotificationClick = { vm.openNotifications() },
                onControlCenterClick = { vm.openControlCenter() },
                onOverviewClick = { vm.openOverview() },
            )

            // Scrollable columns (takes remaining space)
            ScrollableColumns(
                columns = currentColumns,
                focusedColumnIndex = focusedCol,
                onColumnFocused = { vm.setFocusedColumn(it) },
                onAppClick = { vm.launchApp(it) },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = if (anyOverlay) 0.3f else 1f },
            )

            // Dock
            Dock(
                favoriteApps = favApps,
                onAppClick = { vm.launchApp(it) },
                onDrawerClick = { vm.openDrawer() },
                modifier = Modifier.graphicsLayer { alpha = if (anyOverlay) 0.3f else 1f },
            )
        }

        // ── Overlays ──
        if (isDrawerOpen) {
            AppDrawer(
                apps = allApps,
                query = query,
                onQueryChange = { vm.updateQuery(it) },
                onAppClick = { vm.launchApp(it); vm.closeDrawer() },
                onDismiss = { vm.closeDrawer() },
            )
        }

        if (isCCOpen) {
            ControlCenter(
                tiles = tiles,
                brightness = brightness,
                onTileToggle = { vm.toggleQuickTile(it) },
                onBrightnessChange = { vm.setBrightness(it) },
                onDismiss = { vm.closeControlCenter() },
            )
        }

        if (isNotifOpen) {
            NotificationCenter(
                notifications = emptyList(), // TODO: hook NotificationListenerService
                onDismiss = { vm.closeNotifications() },
            )
        }

        if (isOverviewOpen) {
            WorkspaceOverview(
                workspaces = workspaces,
                currentWorkspace = currentWs,
                onWorkspaceSelect = { vm.switchWorkspace(it); vm.closeOverview() },
                onDismiss = { vm.closeOverview() },
            )
        }
    }
}
