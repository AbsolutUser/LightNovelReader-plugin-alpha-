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

    // 🔑 SEMUA KONSISTEN ONLINE
    override val offLine: Boolean = false
    override val isOffLineFlow: Flow<Boolean> = flowOf(false)
    override suspend fun isOffLine(): Boolean = false

    // 🔑 API BUG WORKAROUND — TIDAK BOLEH EMPTY
    override val explorePageIdList: List<String> =
        listOf("search")

    // 🔑 JANGAN IMPLEMENT EXPLORE DI PLUGIN
    override val explorePageDataSourceMap =
        emptyMap<String, Nothing>()

    override val exploreExpandedPageDataSourceMap =
        emptyMap<String, Nothing>()

    // --- SEARCH CONFIG ---
    override val searchTypeMap: Map<String, String> =
        mapOf("all" to "All")

    override val searchTipMap: Map<String, String> =
        mapOf("all" to "Search Meionovels")

    override val searchTypeIdList: List<String> =
        listOf("all")

    // 🔥 SEARCH PIPELINE (WAJIB ADA ITEM NON-END)
    override fun search(
        searchType: String,
        keyword: String
    ): Flow<List<BookInformation>> {
        return flowOf(
            listOf(
                // ❗ DUMMY ITEM → MENCEGAH CRASH & LOADING LOOP
                BookInformation.empty(),

                // ❗ END MARKER
                BookInformation.empty()
            )
        )
    }

    // --- STUBS ---
    override suspend fun getBookInformation(id: String) =
        BookInformation.empty()

    override suspend fun getBookVolumes(id: String) =
        BookVolumes.empty()

    override suspend fun getChapterContent(chapterId: String, bookId: String) =
        ChapterContent.empty()

    override fun stopAllSearch() {}
}
