package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
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

    // ❗ WAJIB TIDAK KOSONG (API BUG WORKAROUND)
    override val explorePageIdList: List<String> =
        listOf("placeholder")

    // ❗ MAP BOLEH KOSONG — APP TIDAK AKSES JIKA OFFLINE
    override val explorePageDataSourceMap =
        emptyMap<String, Nothing>()

    override val exploreExpandedPageDataSourceMap =
        emptyMap<String, Nothing>()

    override val searchTypeMap = emptyMap<String, String>()
    override val searchTipMap = emptyMap<String, String>()
    override val searchTypeIdList = emptyList<String>()

    override suspend fun isOffLine(): Boolean = false

    override suspend fun getBookInformation(id: String) =
        BookInformation.empty()

    override suspend fun getBookVolumes(id: String) =
        BookVolumes.empty()

    override suspend fun getChapterContent(chapterId: String, bookId: String) =
        ChapterContent.empty()

    override fun search(searchType: String, keyword: String) =
        flowOf(listOf(BookInformation.empty()))

    override fun stopAllSearch() {}
}
