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

    override val offLine: Boolean = false
    override val isOffLineFlow: Flow<Boolean> = flowOf(false)
    override suspend fun isOffLine(): Boolean = false

    // Explore dimatikan (API bug jika kosong)
    override val explorePageIdList: List<String> = listOf("dummy")

    override val explorePageDataSourceMap:
        Map<String, ExplorePageDataSource> = emptyMap()

    override val exploreExpandedPageDataSourceMap:
        Map<String, ExploreExpandedPageDataSource> = emptyMap()

    override val searchTypeMap = mapOf("default" to "Search")
    override val searchTipMap = mapOf("default" to "Search Meionovels")
    override val searchTypeIdList = listOf("default")

    override fun search(
        searchType: String,
        keyword: String
    ): Flow<List<BookInformation>> {
        // ❗ API SAAT INI TIDAK MENGIZINKAN MEMBUAT BOOK NON-EMPTY
        // Maka kita KEMBALIKAN 1 empty() agar tidak crash
        return flowOf(
            listOf(BookInformation.empty())
        )
    }

    override suspend fun getBookInformation(id: String): BookInformation =
        BookInformation.empty()

    override suspend fun getBookVolumes(id: String): BookVolumes =
        BookVolumes.empty()

    override suspend fun getChapterContent(
        chapterId: String,
        bookId: String
    ): ChapterContent =
        ChapterContent.empty()

    override fun stopAllSearch() {}
}    override fun stopAllSearch() {}
}
    override fun stopAllSearch() {}
}
