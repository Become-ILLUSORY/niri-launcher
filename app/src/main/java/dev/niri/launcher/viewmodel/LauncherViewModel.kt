package dev.niri.launcher.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.niri.launcher.model.AppColumn
import dev.niri.launcher.model.AppInfo
import dev.niri.launcher.model.QuickTile
import dev.niri.launcher.model.Workspace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    // ── Workspaces ──────────────────────────────────────
    private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
    val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

    private val _currentWorkspace = MutableStateFlow(0)
    val currentWorkspace: StateFlow<Int> = _currentWorkspace.asStateFlow()

    // ── Focus (which column is "active") ────────────────
    private val _focusedColumn = MutableStateFlow(0)
    val focusedColumn: StateFlow<Int> = _focusedColumn.asStateFlow()

    // ── Overlays ────────────────────────────────────────
    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val _isControlCenterOpen = MutableStateFlow(false)
    val isControlCenterOpen: StateFlow<Boolean> = _isControlCenterOpen.asStateFlow()

    private val _isNotificationOpen = MutableStateFlow(false)
    val isNotificationOpen: StateFlow<Boolean> = _isNotificationOpen.asStateFlow()

    private val _isOverviewOpen = MutableStateFlow(false)
    val isOverviewOpen: StateFlow<Boolean> = _isOverviewOpen.asStateFlow()

    // ── All installed apps ──────────────────────────────
    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _queryText = MutableStateFlow("")
    val queryText: StateFlow<String> = _queryText.asStateFlow()

    // ── Quick settings ──────────────────────────────────
    private val _quickTiles = MutableStateFlow(
        listOf(
            QuickTile("Wi-Fi", "wifi", true),
            QuickTile("蓝牙", "bluetooth", false),
            QuickTile("手电筒", "flashlight", false),
            QuickTile("自动旋转", "screen_rotation", true),
            QuickTile("飞行模式", "airplanemode", false),
            QuickTile("勿扰", "do_not_disturb", false),
            QuickTile("热点", "hotspot", false),
            QuickTile("省电", "battery_saver", false),
        )
    )
    val quickTiles: StateFlow<List<QuickTile>> = _quickTiles.asStateFlow()

    // ── Brightness ──────────────────────────────────────
    private val _brightness = MutableStateFlow(0.5f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .asSequence()
                .map { resolveInfo ->
                    AppInfo(
                        packageName = resolveInfo.activityInfo.packageName,
                        label = resolveInfo.loadLabel(pm).toString(),
                        icon = resolveInfo.loadIcon(pm),
                        isSystem = (resolveInfo.activityInfo.applicationInfo.flags and
                                android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                    )
                }
                .filter { it.packageName != getApplication<Application>().packageName }
                .sortedBy { it.label.lowercase() }
                .toList()

            _allApps.value = apps

            // Build default workspace: distribute apps into columns of 2-3
            val columns = mutableListOf<AppColumn>()
            var colId = 0
            apps.chunked(3).forEach { chunk ->
                columns.add(AppColumn(id = colId++, apps = chunk))
            }
            _workspaces.value = listOf(
                Workspace(id = 0, columns = columns.take(8)),
                Workspace(id = 1, columns = columns.drop(8).take(8)),
                Workspace(id = 2, columns = columns.drop(16)),
            ).filter { it.columns.isNotEmpty() }

            if (_workspaces.value.isEmpty()) {
                _workspaces.value = listOf(Workspace(0, emptyList()))
            }
        }
    }

    fun setFocusedColumn(index: Int) {
        _focusedColumn.value = index
    }

    fun switchWorkspace(index: Int) {
        val ws = _workspaces.value
        if (index in ws.indices) {
            _currentWorkspace.value = index
            _focusedColumn.value = 0
        }
    }

    fun nextWorkspace() {
        val ws = _workspaces.value
        if (_currentWorkspace.value < ws.lastIndex) {
            switchWorkspace(_currentWorkspace.value + 1)
        }
    }

    fun prevWorkspace() {
        if (_currentWorkspace.value > 0) {
            switchWorkspace(_currentWorkspace.value - 1)
        }
    }

    // ── Overlay toggles ────────────────────────────────
    fun openDrawer() { _isDrawerOpen.value = true }
    fun closeDrawer() { _isDrawerOpen.value = false }
    fun toggleDrawer() { _isDrawerOpen.value = !_isDrawerOpen.value }

    fun openControlCenter() { _isControlCenterOpen.value = true }
    fun closeControlCenter() { _isControlCenterOpen.value = false }

    fun openNotifications() { _isNotificationOpen.value = true }
    fun closeNotifications() { _isNotificationOpen.value = false }

    fun openOverview() { _isOverviewOpen.value = true }
    fun closeOverview() { _isOverviewOpen.value = false }

    fun updateQuery(text: String) { _queryText.value = text }

    fun toggleQuickTile(index: Int) {
        val tiles = _quickTiles.value.toMutableList()
        if (index in tiles.indices) {
            tiles[index] = tiles[index].copy(isActive = !tiles[index].isActive)
            _quickTiles.value = tiles
        }
    }

    fun setBrightness(value: Float) {
        _brightness.value = value.coerceIn(0f, 1f)
    }

    fun launchApp(appInfo: AppInfo) {
        val ctx = getApplication<Application>()
        val intent = ctx.packageManager.getLaunchIntentForPackage(appInfo.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }
    }
}
