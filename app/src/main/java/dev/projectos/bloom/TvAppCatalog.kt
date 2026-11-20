package dev.projectos.bloom

import java.util.Locale

internal const val TV_LAUNCHER_CATEGORY = "android.intent.category.LEANBACK_LAUNCHER"

internal data class TvApp(
    val title: String,
    val packageName: String,
    val activityName: String,
    val launcherCategory: String,
    val detail: String
) {
    val componentKey: String
        get() = launcherComponentKey(packageName, activityName)
}

internal fun launcherComponentKey(packageName: String, activityName: String): String =
    "$packageName/$activityName"

/**
 * Keep Android TV-ready apps at the front of the launcher, then sort each group
 * predictably by display name and package name.
 */
internal fun sortTvApps(apps: Iterable<TvApp>): List<TvApp> =
    apps.sortedWith(
        compareBy<TvApp> { it.launcherCategory != TV_LAUNCHER_CATEGORY }
            .thenBy { it.title.trim().lowercase(Locale.ROOT) }
            .thenBy { it.packageName }
            .thenBy { it.activityName }
    )
