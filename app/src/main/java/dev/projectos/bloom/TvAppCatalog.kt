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
 * Deduplicate launcher results by component, preferring the Android TV category
 * when the same activity is returned by both TV and standard launcher queries.
 */
internal fun uniqueLauncherActivities(apps: Iterable<TvApp>): List<TvApp> {
    val unique = linkedMapOf<String, TvApp>()
    for (app in apps) {
        val existing = unique[app.componentKey]
        if (existing == null || (
            existing.launcherCategory != TV_LAUNCHER_CATEGORY &&
                app.launcherCategory == TV_LAUNCHER_CATEGORY
            )
        ) {
            unique[app.componentKey] = app
        }
    }
    return unique.values.toList()
}

/**
 * Keep Android TV-ready apps at the front of the launcher, then sort each group
 * predictably by display name, package name, and activity name.
 */
internal fun sortTvApps(apps: Iterable<TvApp>): List<TvApp> =
    uniqueLauncherActivities(apps).sortedWith(
        compareBy<TvApp> { it.launcherCategory != TV_LAUNCHER_CATEGORY }
            .thenBy { it.title.trim().lowercase(Locale.ROOT) }
            .thenBy { it.packageName }
            .thenBy { it.activityName }
    )
