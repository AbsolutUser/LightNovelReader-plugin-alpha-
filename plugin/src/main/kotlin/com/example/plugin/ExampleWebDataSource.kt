package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
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

    // =========================
    // ONLINE STATUS
    // =========================
    override val offLine: Boolean = false
    override val isOffLineFlow: Flow<Boolean> = flowOf(false)
    override suspend fun isOffLine(): Boolean = false

    // =========================
    // EXPLORE (DISABLED SAFELY)
    // =========================
    override val explorePageIdList: List<String> = emptyList()

    override val explorePageDataSourceMap:
        Map<String, ExplorePageDataSource> = emptyMap()

    override val exploreExpandedPageDataSourceMap:
        Map<String, ExploreExpandedPageDataSource> = emptyMap()

    // =========================
    // SEARCH CONFIG
    // =========================
    override val searchTypeMap = mapOf("default" to "Search")
    override val searchTipMap = mapOf("default" to "Search Meionovels")
    override val searchTypeIdList = listOf("default")

    // =========================
    // SEARCH IMPLEMENTATION
    // =========================
    override fun search(
        searchType: String,
        keyword: String
    ): Flow<List<BookInformation>> = flow {

        val encoded = URLEncoder.encode(keyword, "UTF-8")
        val url = "https://meionovels.com/?s=$encoded"

        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(15_000)
            .get()

        val result = mutableListOf<BookInformation>()

        val items = doc.select("h2.entry-title a")

        for (el in items) {
            val title = el.text().trim()
            val link = el.absUrl("href")

            if (title.isBlank() || link.isBlank()) continue

            val book = BookInformation.build {
                id = link
                this.title = title
                cover = ""
                author = ""
                description = ""
            }

            result.add(book)
        }

        // ❗ API BUG WORKAROUND
        if (result.isEmpty()) {
            result.add(BookInformation.empty())
        }

        emit(result)
    }

    // =========================
    // NOT IMPLEMENTED YET
    // =========================
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
}
