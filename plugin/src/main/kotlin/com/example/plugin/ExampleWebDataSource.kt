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

@WebDataSource(
    name = "Meionovels",
    provider = "meionovels.com"
)
class ExampleWebDataSource : WebBookDataSource {

    override val id: Int = "meionovels".hashCode()

    override val offLine: Boolean = true
    override val isOffLineFlow: Flow<Boolean> = flowOf(true)

    // 🔥 INI YANG MENCEGAH FORCE CLOSE
    override val explorePageIdList: List<String>
    get() = listOf("meionovels_home")

override val explorePageDataSourceMap: Map<String, ExplorePageDataSource>
    get() = mapOf(
        "meionovels_home"
    )
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> = emptyMap()

    override val searchTypeMap: Map<String, String> = mapOf("all" to "All")
    override val searchTipMap: Map<String, String> = mapOf("all" to "Search disabled")
    override val searchTypeIdList: List<String> = listOf("all")

    override suspend fun isOffLine(): Boolean = true

    override suspend fun getBookInformation(id: String): BookInformation =
        BookInformation.empty()

    override suspend fun getBookVolumes(id: String): BookVolumes =
        BookVolumes.empty()

    override suspend fun getChapterContent(
        chapterId: String,
        bookId: String
    ): ChapterContent =
        ChapterContent.empty()

    override fun search(
        searchType: String,
        keyword: String
    ): Flow<List<BookInformation>> =
        flowOf(listOf(BookInformation.empty()))

    override fun stopAllSearch() {}
}