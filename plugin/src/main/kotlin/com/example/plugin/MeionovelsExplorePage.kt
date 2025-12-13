package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.web.explore.ExplorePage
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageSection

/**
 * Minimal Explore Page implementation
 * REQUIRED to prevent Explore tab crash
 */
object MeionovelsExplorePage : ExplorePageDataSource {

    override fun getExplorePage(): ExplorePage {
        return ExplorePage(
            pageId = "meionovels_explore",
            title = "Meionovels",
            sections = listOf(
                ExplorePageSection(
                    sectionId = "empty_section",
                    title = "Coming Soon",
                    books = emptyList<BookInformation>()
                )
            )
        )
    }
}