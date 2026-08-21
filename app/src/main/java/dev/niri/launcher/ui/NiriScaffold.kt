package dev.niri.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val anyOverlay = isDrawerOpen || isCCOpen || isNotifOpen || isOverviewOpen

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NoctBg),
    ) {
        // Main content: columns + dock
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar — workspace dots are clickable to switch
            TopBar(
                workspaceCount = workspaces.size,
                currentWorkspace = currentWs,
                onWorkspaceClick = { vm.switchWorkspace(it) },
                onNotificationClick = { vm.openNotifications() },
                onControlCenterClick = { vm.openControlCenter() },
                onOverviewClick = { vm.openOverview() },
            )

            // Scrollable columns
            ScrollableColumns(
                columns = currentColumns,
                focusedColumnIndex = focusedCol,
                totalColumns = currentColumns.size,
                onColumnFocused = { vm.setFocusedColumn(it) },
                onAppClick = { app, col ->
                    vm.launchAppSplit(app, col, currentColumns.size)
                },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = if (anyOverlay) 0.3f else 1f },
            )

            // Dock — also has workspace switcher
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
                notifications = emptyList(),
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
