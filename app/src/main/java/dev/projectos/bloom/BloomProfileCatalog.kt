package dev.projectos.bloom

import java.util.Locale

/** Normalizes local profile names into a stable, case-insensitive display order. */
internal fun sortBloomProfileNames(names: Iterable<String>): List<String> {
    val unique = linkedMapOf<String, String>()
    for (name in names) {
        val normalized = name.trim()
        if (normalized.isNotEmpty()) {
            unique.putIfAbsent(normalized.lowercase(Locale.ROOT), normalized)
        }
    }
    return unique.values.sortedWith(
        compareBy<String> { it.lowercase(Locale.ROOT) }.thenBy { it }
    )
}

/** Returns the stored spelling when a profile name matches without regard to case. */
internal fun findBloomProfileName(names: Iterable<String>, candidate: String): String? {
    val normalized = candidate.trim()
    if (normalized.isEmpty()) return null
    return names.firstOrNull { it.trim().equals(normalized, ignoreCase = true) }
}
