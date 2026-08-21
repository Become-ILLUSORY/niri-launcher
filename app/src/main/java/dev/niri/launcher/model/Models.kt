package dev.niri.launcher.model

import android.graphics.drawable.Drawable

/** An app that can be placed in a column. */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isSystem: Boolean = false,
)

/** A column holds one or more app tiles — the fundamental niri concept. */
data class AppColumn(
    val id: Int,
    val apps: List<AppInfo>,
)

/** A workspace is a collection of columns, swiped vertically. */
data class Workspace(
    val id: Int,
    val columns: List<AppColumn>,
)

/** Mutable state for a running/visible app window placeholder. */
data class WindowTile(
    val app: AppInfo,
    val previewColor: Long, // placeholder dominant color
    val isActive: Boolean = false,
)

/** Quick settings tile for control center. */
data class QuickTile(
    val label: String,
    val icon: String, // material icon name
    val isActive: Boolean = false,
)

/** Notification entry. */
data class NotificationEntry(
    val packageName: String,
    val appName: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val icon: Drawable? = null,
)
