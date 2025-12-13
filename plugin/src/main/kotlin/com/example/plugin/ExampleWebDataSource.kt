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
 * Temporary safe datasource for Meionovels.
 *
 * This version is intentionally minimal and safe:
 * - No network operations
 * - No explore pages registered
 * - All API functions return empty() objects or empty flows
 *
 * Use this to avoid crashes while developing the full parser.
 */

@WebDataSource(
    name = "MeioNovels (safe)",
    provider = "meionovels.com"
)
class ExampleWebDataSource : WebBookDataSource {

    override val id: Int
        get() = "meionovels".hashCode()

    // mark offline so host app may skip network-dependent UI if it respects this flag
    override val offLine: Boolean
        get() = true

    // stable offline flow
    override val isOffLineFlow: Flow<Boolean>
        get() = flowOf(true)

    // no explore pages (prevents Explore from calling data-providers in many implementations)
    override val explorePageIdList: List<String>
    get() = listOf("meionovels")

override val explorePageDataSourceMap: Map<String, ExplorePageDataSource>
    get() = mapOf(
        "meionovels" to MeionovelsExplorePage
    )

override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource>
    get() = emptyMap()

    // search types - keep minimal
    override val searchTypeMap: Map<String, String>
        get() = mapOf("all" to "All")
    override val searchTipMap: Map<String, String>
        get() = mapOf("all" to "Use this to search titles on MeioNovels (disabled in safe mode)")
    override val searchTypeIdList: List<String>
        get() = listOf("all")

    override suspend fun isOffLine(): Boolean {
        return true
    }

    // ---------- Safe implementations that never throw / never network ----------

    override suspend fun getBookInformation(id: String): BookInformation {
        // Return empty metadata safely
        return BookInformation.Companion.empty()
    }

    override suspend fun getBookVolumes(id: String): BookVolumes {
        // Return empty volumes safely
        return BookVolumes.Companion.empty()
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent {
        // Return empty chapter content safely
        return ChapterContent.Companion.empty()
    }

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        // Return an immediate empty result list + an empty terminator to indicate end
        return flowOf(listOf(BookInformation.Companion.empty()))
    }

    override fun stopAllSearch() {
        // no-op
    }
}