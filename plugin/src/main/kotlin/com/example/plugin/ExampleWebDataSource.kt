package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * SAFE / STABLE WebBookDataSource
 *
 * This implementation is intentionally minimal and crash-proof.
 * It guarantees:
 * - No Explore crash
 * - No network usage
 * - No TODO()
 * - No unimplemented API
 */

@WebDataSource(
    name = "Meionovels",
    provider = "meionovels.com"
)
class ExampleWebDataSource : WebBookDataSource {

    override val id: Int
        get() = "meionovels".hashCode()

    override val offLine: Boolean
        get() = true

    override val isOffLineFlow: Flow<Boolean>
        get() = flowOf(true)

    // 🔴 CRITICAL PART — MUST NOT THROW
    override val explorePageIdList: List<String>
        get() = emptyList()

    override val explorePageDataSourceMap: Map<String, ExplorePageDataSource>
        get() = emptyMap()

    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource>
        get() = emptyMap()

    override val searchTypeMap: Map<String, String>
        get() = mapOf("all" to "All")

    override val searchTipMap: Map<String, String>
        get() = mapOf("all" to "Search is disabled (safe mode)")

    override val searchTypeIdList: List<String>
        get() = listOf("all")

    override suspend fun isOffLine(): Boolean = true

    override suspend fun getBookInformation(id: String): BookInformation {
        return BookInformation.Companion.empty()
    }

    override suspend fun getBookVolumes(id: String): BookVolumes {
        return BookVolumes.Companion.empty()
    }

    override suspend fun getChapterContent(
        chapterId: String,
        bookId: String
    ): ChapterContent {
        return ChapterContent.Companion.empty()
    }

    override fun search(
        searchType: String,
        keyword: String
    ): Flow<List<BookInformation>> {
        // end-of-search marker
        return flowOf(listOf(BookInformation.Companion.empty()))
    }

    override fun stopAllSearch() {
        // no-op
    }
}
