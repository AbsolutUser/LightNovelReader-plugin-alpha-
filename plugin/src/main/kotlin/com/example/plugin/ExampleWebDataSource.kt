package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import io.nightfish.lightnovelreader.api.web.explore.model.ExplorePage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@WebDataSource(
    name = "Meionovels",
    provider = "meionovels.com"
)
class ExampleWebDataSource : WebBookDataSource {

    override val id: Int = "meionovels".hashCode()

    // 🔑 SEMUA KONSISTEN: ONLINE
    override val offLine: Boolean = false
    override val isOffLineFlow: Flow<Boolean> = flowOf(false)
    override suspend fun isOffLine(): Boolean = false

    // 🔑 WAJIB ADA & HARUS ADA DATASOURCE-NYA
    override val explorePageIdList: List<String> =
        listOf("meionovels_home")

    override val explorePageDataSourceMap: Map<String, ExplorePageDataSource> =
        mapOf(
            "meionovels_home" to object : ExplorePageDataSource {
                override val title: String = "Meionovels"

                override fun getExplorePage(): ExplorePage {
                    // 🔑 TIDAK BOLEH EMPTY LIST
                    return ExplorePage(
                        sections = listOf(
                            ExplorePage.Section(
                                title = "Placeholder",
                                books = listOf(
                                    BookInformation(
                                        id = "dummy",
                                        title = "Dummy Novel (API OK)",
                                        author = "System",
                                        cover = "",
                                        description = "This confirms Explore works"
                                    )
                                )
                            )
                        )
                    )
                }
            }
        )

    override val exploreExpandedPageDataSourceMap = emptyMap<String, Nothing>()

    // --- SEARCH ---
    override val searchTypeMap = mapOf("all" to "All")
    override val searchTipMap = mapOf("all" to "Test search")
    override val searchTypeIdList = listOf("all")

    override fun search(
        searchType: String,
        keyword: String
    ): Flow<List<BookInformation>> {
        return flowOf(
            listOf(
                BookInformation(
                    id = "search_dummy",
                    title = "Search Result OK",
                    author = "System",
                    cover = "",
                    description = "Search pipeline works"
                ),
                BookInformation.empty() // end marker
            )
        )
    }

    // --- SAFE STUBS ---
    override suspend fun getBookInformation(id: String) =
        BookInformation.empty()

    override suspend fun getBookVolumes(id: String) =
        BookVolumes.empty()

    override suspend fun getChapterContent(chapterId: String, bookId: String) =
        ChapterContent.empty()

    override fun stopAllSearch() {}
}
