package com.openlauncher.app.model

fun hasUsableMediaArtwork(hasBitmap: Boolean, artUri: String?): Boolean =
    hasBitmap || !artUri.isNullOrBlank()

fun extractMediaLyrics(metadataText: Map<String, CharSequence?>): String? =
    metadataText.entries
        .asSequence()
        .filter { (key, value) ->
            value?.isNotBlank() == true &&
                (key.contains("lyrics", ignoreCase = true) || key.contains("lyric", ignoreCase = true))
        }
        .map { (_, value) -> value.toString().trim() }
        .firstOrNull()
