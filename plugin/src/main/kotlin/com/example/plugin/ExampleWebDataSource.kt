package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.buildBookInformation
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.jsoup.Jsoup
import java.net.URLEncoder

@WebDataSource(
    name = "Meionovels",
    provider = "meionovels.com"
)
class ExampleWebDataSource : WebBookDataSource {

    override val id: Int = "meionovels".hashCode()

    override val offLine = false
    override val isOffLineFlow = flowOf(false)
    override suspend fun isOffLine() = false

    // 🔴 EXPLORE DIMATIKAN TOTAL
    override val explorePageIdList = emptyList<String>()
    override val explorePageDataSourceMap =
        emptyMap<String, ExplorePageDataSource>()
    override val exploreExpandedPageDataSourceMap =
        emptyMap<String, ExploreExpandedPageDataSource>()

    // ✅ SEARCH AKTIF
    override val searchTypeMap = mapOf("all" to "All")
    override val searchTipMap = mapOf("all" to "Search Meionovels")
    override val searchTypeIdList = listOf("all")

    override fun search(
        searchType: String,
        keyword: String
    ): Flow<List<BookInformation>> = flow {

        val url =
            "https://meionovels.com/?s=" +
                URLEncoder.encode(keyword, "UTF-8")

        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(15_000)
            .get()

        val list = mutableListOf<BookInformation>()

        for (a in doc.select("article h2.entry-title a")) {
            val title = a.text().trim()
            val link = a.absUrl("href")

            if (title.isEmpty() || link.isEmpty()) continue

            // ✅ SATU-SATUNYA CARA YANG BENAR
            list.add(
                buildBookInformation {
                    id = link
                    this.title = title
                    detailUrl = link
                }
            )
        }

        emit(list)
    }

    // DUMMY (BELUM DIPAKAI)
    override suspend fun getBookInformation(id: String) =
        BookInformation.empty()

    override suspend fun getBookVolumes(id: String) =
        BookVolumes.empty()

    override suspend fun getChapterContent(
        chapterId: String,
        bookId: String
    ) = ChapterContent.empty()

    override fun stopAllSearch() {}
}
