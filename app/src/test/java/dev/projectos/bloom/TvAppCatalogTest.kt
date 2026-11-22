package dev.projectos.bloom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TvAppCatalogTest {
    @Test
    fun placesTvLauncherAppsBeforeStandardAndroidApps() {
        val apps = listOf(
            TvApp("YouTube", "com.example.youtube", "YouTubeActivity", "android.intent.category.LAUNCHER", "Android app"),
            TvApp("Netflix", "com.example.netflix", "NetflixActivity", TV_LAUNCHER_CATEGORY, "Android TV app")
        )

        assertEquals(
            listOf("Netflix", "YouTube"),
            sortTvApps(apps).map { it.title }
        )
    }

    @Test
    fun sortsNamesCaseInsensitivelyWithinEachLauncherGroup() {
        val apps = listOf(
            TvApp("zeta TV", "com.example.zeta", "ZetaActivity", TV_LAUNCHER_CATEGORY, "Android TV app"),
            TvApp("Alpha mobile", "com.example.alpha", "AlphaActivity", "android.intent.category.LAUNCHER", "Android app"),
            TvApp("Beta TV", "com.example.beta", "BetaActivity", TV_LAUNCHER_CATEGORY, "Android TV app")
        )

        assertEquals(
            listOf("Beta TV", "zeta TV", "Alpha mobile"),
            sortTvApps(apps).map { it.title }
        )
    }

    @Test
    fun usesPackageNameAsStableTieBreakerForMatchingTitles() {
        val apps = listOf(
            TvApp("Player", "com.example.z", "ZActivity", TV_LAUNCHER_CATEGORY, "Android TV app"),
            TvApp("Player", "com.example.a", "AActivity", TV_LAUNCHER_CATEGORY, "Android TV app")
        )

        assertEquals(
            listOf("com.example.a", "com.example.z"),
            sortTvApps(apps).map { it.packageName }
        )
    }
    @Test
    fun prefersTvCategoryWhenOneComponentAppearsInBothQueries() {
        val standard = TvApp("Player", "com.example.player", "com.example.player.MainActivity", "android.intent.category.LAUNCHER", "Android app")
        val tv = standard.copy(launcherCategory = TV_LAUNCHER_CATEGORY, detail = "Android TV app")

        assertEquals(listOf("Android TV app"), sortTvApps(listOf(standard, tv)).map { it.detail })
    }

    @Test
    fun keepsDistinctLauncherActivitiesWithinOnePackage() {
        val apps = listOf(
            TvApp("Player", "com.example.player", "com.example.player.ZActivity", TV_LAUNCHER_CATEGORY, "Android TV app"),
            TvApp("Player", "com.example.player", "com.example.player.AActivity", TV_LAUNCHER_CATEGORY, "Android TV app")
        )

        val sorted = sortTvApps(apps)
        assertEquals(
            listOf("com.example.player.AActivity", "com.example.player.ZActivity"),
            sorted.map { it.activityName }
        )
        assertEquals(
            listOf(
                "com.example.player/com.example.player.AActivity",
                "com.example.player/com.example.player.ZActivity"
            ),
            sorted.map { it.componentKey }
        )
    }

    @Test
    fun launcherQueriesDoNotRequireDefaultCategory() {
        assertEquals(0, LAUNCHER_ACTIVITY_QUERY_FLAGS)
    }

    @Test
    fun excludesDisabledUnexportedAppsAndBlankLabels() {
        assertTrue(isEligibleLauncherActivity(true, true, true, "Player"))
        assertFalse(isEligibleLauncherActivity(false, true, true, "Player"))
        assertFalse(isEligibleLauncherActivity(true, false, true, "Player"))
        assertFalse(isEligibleLauncherActivity(true, true, false, "Player"))
        assertFalse(isEligibleLauncherActivity(true, true, true, "   "))
    }

}
