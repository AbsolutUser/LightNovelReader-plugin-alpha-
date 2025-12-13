package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource

/**
 * Minimal ExplorePageDataSource
 * REQUIRED so Explore tab does not crash
 */
object MeionovelsExplorePage : ExplorePageDataSource {

    override val id: String
        get() = "meionovels_explore"

    override val title: String
        get() = "Meionovels"

    override fun getExplorePage(): List<BookInformation> {
        // MUST return a list (can be empty)
        return emptyList()
    }
}