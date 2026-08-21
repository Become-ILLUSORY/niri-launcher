package dev.niri.launcher.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import android.os.Build
import android.provider.Settings
import android.app.NotificationManager
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
            QuickTile("Wi-Fi", "wifi", isSystemOn()),
            QuickTile("蓝牙", "bluetooth", false),
            QuickTile("手电筒", "flashlight", false),
            QuickTile("自动旋转", "screen_rotation", isRotationOn()),
            QuickTile("飞行模式", "airplanemode", false),
            QuickTile("勿扰", "do_not_disturb", isDndOn()),
            QuickTile("热点", "hotspot", false),
            QuickTile("省电", "battery_saver", false),
        )
    )
    val quickTiles: StateFlow<List<QuickTile>> = _quickTiles.asStateFlow()

    private val _brightness = MutableStateFlow(0.5f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    // Camera for flashlight
    private val cameraManager by lazy {
        getApplication<Application>().getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private var torchOn = false

    init {
        loadApps()
    }

    // ── System query helpers ────────────────────────────
    private fun isRotationOn(): Boolean {
        return try {
            Settings.System.getInt(getApplication<Application>().contentResolver,
                Settings.System.ACCELEROMETER_ROTATION) == 1
        } catch (_: Exception) { true }
    }

    private fun isDndOn(): Boolean {
        val nm = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    private fun isSystemOn(): Boolean = false // Wi-Fi state needs carrier, just default off

    // ── Load apps ───────────────────────────────────────
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

            val columns = mutableListOf<AppColumn>()
            var colId = 0
            apps.chunked(2).forEach { chunk ->
                columns.add(AppColumn(id = colId++, apps = chunk))
            }
            _workspaces.value = listOf(
                Workspace(id = 0, columns = columns.take(6)),
                Workspace(id = 1, columns = columns.drop(6).take(6)),
                Workspace(id = 2, columns = columns.drop(12)),
            ).filter { it.columns.isNotEmpty() }

            if (_workspaces.value.isEmpty()) {
                _workspaces.value = listOf(Workspace(0, emptyList()))
            }
        }
    }

    // ── Workspace navigation ────────────────────────────
    fun setFocusedColumn(index: Int) { _focusedColumn.value = index }

    fun switchWorkspace(index: Int) {
        val ws = _workspaces.value
        if (index in ws.indices) {
            _currentWorkspace.value = index
            _focusedColumn.value = 0
        }
    }

    fun nextWorkspace() {
        if (_currentWorkspace.value < _workspaces.value.lastIndex)
            switchWorkspace(_currentWorkspace.value + 1)
    }

    fun prevWorkspace() {
        if (_currentWorkspace.value > 0)
            switchWorkspace(_currentWorkspace.value - 1)
    }

    // ── Overlay toggles ────────────────────────────────
    fun openDrawer() { _isDrawerOpen.value = true }
    fun closeDrawer() { _isDrawerOpen.value = false }
    fun openControlCenter() { _isControlCenterOpen.value = true }
    fun closeControlCenter() { _isControlCenterOpen.value = false }
    fun openNotifications() { _isNotificationOpen.value = true }
    fun closeNotifications() { _isNotificationOpen.value = false }
    fun openOverview() { _isOverviewOpen.value = true }
    fun closeOverview() { _isOverviewOpen.value = false }
    fun updateQuery(text: String) { _queryText.value = text }

    // ── Quick tile: real system actions ─────────────────
    fun toggleQuickTile(index: Int) {
        val tiles = _quickTiles.value.toMutableList()
        if (index !in tiles.indices) return
        val tile = tiles[index]

        when (tile.label) {
            "手电筒" -> toggleFlashlight()
            "自动旋转" -> toggleRotation()
            "勿扰" -> toggleDnd()
            else -> {} // Other tiles: just toggle visual state for now
        }

        // Re-read actual state after toggle
        tiles[index] = tile.copy(isActive = when (tile.label) {
            "手电筒" -> torchOn
            "自动旋转" -> isRotationOn()
            "勿扰" -> isDndOn()
            else -> !tile.isActive
        })
        _quickTiles.value = tiles
    }

    private fun toggleFlashlight() {
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return
            torchOn = !torchOn
            cameraManager.setTorchMode(cameraId, torchOn)
        } catch (_: Exception) {}
    }

    private fun toggleRotation() {
        try {
            val current = Settings.System.getInt(
                getApplication<Application>().contentResolver,
                Settings.System.ACCELEROMETER_ROTATION
            )
            Settings.System.putInt(
                getApplication<Application>().contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                if (current == 1) 0 else 1
            )
        } catch (_: Exception) {}
    }

    private fun toggleDnd() {
        try {
            val nm = getApplication<Application>()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.isNotificationPolicyAccessGranted) {
                nm.setInterruptionFilter(
                    if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL)
                        NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    else
                        NotificationManager.INTERRUPTION_FILTER_ALL
                )
            }
        } catch (_: Exception) {}
    }

    fun setBrightness(value: Float) {
        _brightness.value = value.coerceIn(0f, 1f)
        try {
            Settings.System.putInt(
                getApplication<Application>().contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                0 // manual
            )
            Settings.System.putInt(
                getApplication<Application>().contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                (value * 255).toInt()
            )
        } catch (_: Exception) {}
    }

    // ── Launch app: freeform half-screen ────────────────
    fun launchApp(appInfo: AppInfo, columnBounds: android.graphics.Rect? = null) {
        val ctx = getApplication<Application>()
        val intent = ctx.packageManager.getLaunchIntentForPackage(appInfo.packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        try {
            val activityOptions = android.app.ActivityOptions.makeBasic()

            if (columnBounds != null) {
                // Try freeform mode for half-screen tiling
                try {
                    // setLaunchBounds is public API (API 24+)
                    activityOptions.setLaunchBounds(columnBounds)

                    // setLaunchWindowingMode is hidden — use reflection
                    val method = activityOptions.javaClass.getDeclaredMethod(
                        "setLaunchWindowingMode", Int::class.javaPrimitiveType
                    )
                    method.isAccessible = true
                    method.invoke(activityOptions, 5) // WINDOWING_MODE_FREEFORM = 5
                } catch (_: Exception) {
                    // Fallback: just launch normally
                }
            }

            ctx.startActivity(intent, activityOptions.toBundle())
        } catch (_: Exception) {
            ctx.startActivity(intent)
        }
    }

    fun launchAppSplit(appInfo: AppInfo, column: Int, totalColumns: Int) {
        val ctx = getApplication<Application>()
        val dm = ctx.resources.displayMetrics
        val screenW = dm.widthPixels
        val screenH = dm.heightPixels

        // Reserve 48dp for dock at bottom
        val dockHeight = (48 * dm.density).toInt()
        val topBarHeight = (40 * dm.density).toInt()

        // Divide screen into columns
        val colWidth = screenW / totalColumns.coerceAtLeast(1)
        val left = colWidth * column
        val right = left + colWidth
        val top = topBarHeight
        val bottom = screenH - dockHeight

        val bounds = android.graphics.Rect(left, top, right, bottom)
        launchApp(appInfo, bounds)
    }
}
