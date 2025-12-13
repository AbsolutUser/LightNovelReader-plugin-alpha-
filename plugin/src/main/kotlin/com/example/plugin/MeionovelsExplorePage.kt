package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Minimal Explore Page to satisfy LNR Explore lifecycle.
 * This page intentionally returns no books.
 */
object MeionovelsExplorePage : ExplorePageDataSource {

    override val id: String
        get() = "meionovels_explore"

    override val title: String
        get() = "Meionovels"

    override fun getBooks(): Flow<List<BookInformation>> {
        // MUST return a valid flow (can be empty list)
        return flowOf(emptyList())
    }
}